package com.example.mounted

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket

class MessageReceiver(private val activity: MainActivity, port: Int)
    : Thread() {

    var listener: (String) -> Unit = {}
    private val server = ServerSocket(port)

    init {
        isDaemon = true
        start()
    }

    override fun run() {
        super.run()

        // Serve one client at a time
        while (true) {
            // Accept connection request from message client
            val client = server.accept()
            Log.d(javaClass.name, "Connection accepted.")
            activity.runOnUiThread {
                // update message network status on UI
                activity.msgNetworkStatus.text = activity.resources.getText(
                    R.string.msg_client_connected
                )
            }

            // Read messages from client
            val reader = BufferedReader(InputStreamReader(client.getInputStream()))
            loop@ while (true) {
                when (val line = reader.readLine()) {
                    null -> break@loop
                    else -> listener(line)
                }
            }

            // Exit when client socket is closed
            client.close()
            Log.d(javaClass.simpleName, "Exit")
            activity.runOnUiThread {
                activity.msgNetworkStatus.text = activity.resources.getText(
                    R.string.msg_client_unconnected
                )
            }
        }
    }
}