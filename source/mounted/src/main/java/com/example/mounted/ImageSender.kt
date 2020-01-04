package com.example.mounted

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.ByteArrayOutputStream
import java.net.Socket
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

class ImageSender(private val activity: MainActivity, private val port: Int) {

    // Network with socket
    var host = ""
    private var socket: Socket? = null
    private val retryDelay = 1000L

    // Blocking image queue
    private val queueCapacity = 32
    private val queue = ArrayBlockingQueue<Mat>(queueCapacity)

    private val worker = Thread {
        while (true) {
            // Try to connect
            connect()
            if (socket == null) { // not connected on this try
                Thread.sleep(retryDelay) // wait for next try
                continue
            }

            // Try sending images in the queue one by one when server is connected
            loop@ while (true) {
                try {
                    // Take OpenCV Mat from queue and convert to Android Bitmap
                    val mat = queue.take()
                    val bmp = Bitmap.createBitmap(mat.width(), mat.height(),
                        Bitmap.Config.ARGB_8888, false)
                    Utils.matToBitmap(mat, bmp)

                    // Compress to byte stream
                    val byteStream = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 60, byteStream)
                    byteStream.flush()
                    byteStream.close()

                    // Send through socket
                    val size = byteStream.size()
                    val header = String.format(Locale.US, "%08d", size)
                    val outStream = socket!!.getOutputStream()
                    outStream.write(header.toByteArray(Charsets.US_ASCII))
                    outStream.write(byteStream.toByteArray())
                    outStream.flush()
                    Log.d(javaClass.simpleName, "Image sent with size $size")
                } catch (e: Exception) {
                    e.printStackTrace()
                    close() // close socket to reset connection
                    break@loop
                }
            }
        }
    }

    init {
        worker.isDaemon = true
        worker.start()
    }

    fun send(mat: Mat) {
        if (mat.width() == 0 || mat.height() == 0) return
        if (queue.size == queueCapacity)
            queue.poll() // always keep latest frames in the queue
        queue.offer(mat)
    }

    private fun connect() {
        // Try connect to host
        if (socket != null) return
        try {
            socket = Socket(host, port)
            activity.runOnUiThread {
                activity.imgNetworkStatus.text = activity.resources.getText(
                    R.string.img_server_connected)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun close() {
        socket?.close()
        socket = null
        activity.runOnUiThread {
            activity.imgNetworkStatus.text = activity.resources.getText(
                R.string.img_server_unconnected
            )
        }
    }
}