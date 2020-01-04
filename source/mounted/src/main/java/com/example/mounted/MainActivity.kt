package com.example.mounted

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Process
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import org.opencv.android.*
import org.opencv.core.Mat


class MainActivity : Activity(), CameraBridgeViewBase.CvCameraViewListener2 {

    // Network
    // Mounted device serves as message receiver
    private val msgPort = 12345
    private lateinit var msgReceiver: MessageReceiver
    lateinit var msgNetworkStatus: TextView
    // Also serves as image sender
    private var imgHostIp = "192.168.3.9" // temporary for image host IP
    private val imgPort = 54321
    private lateinit var imgSender: ImageSender
    lateinit var imgNetworkStatus: TextView

    // Bluetooth
    companion object {
        const val REC_DATA = 2
        const val CONNECTED_DEVICE_NAME = 4
        const val CONNECTION_TOAST = 5
        const val DEVICE_NAME = "device name"
        const val TOAST = "toast"

        private const val REQUEST_CONNECT_DEVICE = 1
        private const val REQUEST_ENABLE_BT = 2
    }

    private var bluetoothDeviceName: String? = null
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var connectService: BluetoothService? = null
    private lateinit var bluetoothMsg: TextView
    private val writeToBluetooth: (String) -> Unit = { msg ->
        runOnUiThread { bluetoothMsg.text = msg }
        connectService?.write(msg)
    }

    // Image
    // Camera view
    private lateinit var camView: JavaCamera2View
    lateinit var procView: ImageView
    lateinit var ratioText: TextView

    // Driver
    private lateinit var driver: Autodriver
    private lateinit var autodriveSwitch: Switch

    // OpenCV library loader callback
    private val loaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            if (status != LoaderCallbackInterface.SUCCESS)
                showToast("Failed to load OpenCV")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Request camera permission
        when (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_DENIED -> {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA), 1)
            }
        }

        // Setup camera view
        camView = findViewById(R.id.captured)
        camView.setMaxFrameSize(1280, 720) // maximum: 1280 * 720
        camView.setCameraIndex(0) // back camera
        camView.setCameraPermissionGranted() // request permission from system, otherwise black
        camView.setCvCameraViewListener(this) // set action on every frame
        camView.enableFpsMeter() // show FPS on camera view
        camView.enableView() // set view to active, otherwise black
        camView.keepScreenOn = true

        // Get IP address of device
        try {
            val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ip = wifiInfo.ipAddress
            findViewById<TextView>(R.id.ipAddr).text = String.format(
                "IP Address: %d.%d.%d.%d",
                ip and 0xff, (ip shr 8) and 0xff, (ip shr 16) and 0xff, (ip shr 24) and 0xff
            )
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Cannot get IP address")
        }

        // Start message receiver
        msgNetworkStatus = findViewById(R.id.msgNetworkStatus)
        bluetoothMsg = findViewById(R.id.bluetoothMsg)
        msgReceiver = MessageReceiver(this, msgPort)
        msgReceiver.listener = writeToBluetooth

        // Start image sender
        imgNetworkStatus = findViewById(R.id.imgNetworkStatus)
        imgSender = ImageSender(this, imgPort)
        imgSender.host = imgHostIp

        // Setup driver
        procView = findViewById(R.id.procView)
        procView.isVisible = false
        ratioText = findViewById(R.id.ratioText)
        ratioText.isVisible = false
        driver = Autodriver(this, writeToBluetooth)
        autodriveSwitch = findViewById(R.id.autodriveSwitch)
        autodriveSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                procView.isVisible = true
                ratioText.isVisible = true
                msgReceiver.listener = {}
                driver.enabled = true
            } else {
                procView.isVisible = false
                ratioText.isVisible = false
                msgReceiver.listener = writeToBluetooth
                driver.enabled = false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Request enabling bluetooth
        if (!bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
        } // Create connection service
        else if (connectService == null) {
            connectService = BluetoothService(mHandler)
        }
    }

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                CONNECTED_DEVICE_NAME -> {
                    bluetoothDeviceName = msg.data.getString(DEVICE_NAME)
                    showToast("Connected to $bluetoothDeviceName")
                }
                CONNECTION_TOAST -> bluetoothDeviceName = null
            }
        }
    }

    private var targetDeviceName: String? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            REQUEST_CONNECT_DEVICE ->
                if (resultCode == RESULT_OK) { // retrieve bluetooth address data
                    val address = data.extras?.get(DeviceListActivity.DEVICE_ADDRESS)
                            as String
                    val device = bluetoothAdapter.getRemoteDevice(address)
                    targetDeviceName = device.name
                    if (targetDeviceName == bluetoothDeviceName) {
                        showToast("Connected to $bluetoothDeviceName")
                        return
                    }
                    showToast("Connecting to $targetDeviceName")
                    connectService!!.connect(device)
                }
            REQUEST_ENABLE_BT ->  // request rejected by user
                if (resultCode == RESULT_OK)
                    connectService = BluetoothService(mHandler)
                 else
                    showToast("Bluetooth enabling rejected")
        }
    }

    fun onSocketButtonClick(view: View) {
        // Create dialog with text edit
        val edit = EditText(this)
        edit.setText(imgHostIp)
        val storeHost = { imgHostIp = edit.text.toString() }
        AlertDialog.Builder(this)
            .setTitle(R.string.host_edit_title)
            .setView(edit)
            .setPositiveButton("Confirm") { _: DialogInterface, _: Int ->
                storeHost()
                imgSender.host = imgHostIp // pass to image sender
            }.setNegativeButton("Cancel") { _: DialogInterface, _: Int -> storeHost() }
            .setOnDismissListener { storeHost() }
            .show()
    }

    fun onBluetoothButtonClick(view: View) {
        // Request enabling bluetooth
        if (!bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
        }
        // Start activity that shows active bluetooth device list
        val serverIntent = Intent(this, DeviceListActivity::class.java)
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE)
    }

    fun onStopButtonClick(view: View) { connectService?.write("P") }

    override fun onResume() {
        super.onResume()
        if (OpenCVLoader.initDebug())
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        else
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, loaderCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        camView.disableView()
        connectService?.cancelAllBtThread()
        Process.killProcess(Process.myPid())
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}

    override fun onCameraViewStopped() {}

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        // Send RGB image to image server
        val rgba = inputFrame.rgba()
        imgSender.send(rgba)
        // Process image by auto-driver
        driver.update(inputFrame.gray())
        return rgba
    }

    fun showToast(msg: String?) {
        runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
    }
}
