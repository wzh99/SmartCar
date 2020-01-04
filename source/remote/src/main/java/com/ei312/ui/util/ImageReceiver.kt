package com.ei312.ui.util

import android.graphics.BitmapFactory
import android.util.Log
import java.io.EOFException
import java.net.ServerSocket

class ImageReceiver(port: Int) : Thread() {

    private val server = ServerSocket(port)
    var listener: ImageListener? = null

    init {
        isDaemon = true
        start()
    }

    override fun run() {
        super.run()

        while (true) {
            // Accept connection request from message client
            val client = server.accept()
            Log.d(javaClass.simpleName, "Client connected")
            val inputStream = client.getInputStream()

            loop@ while (true) {
                // Read image from client
                try {
                    // Read and decode header from socket
                    val headerBuf = ByteArray(8)
                    if (inputStream.read(headerBuf) == -1)
                        throw EOFException()
                    val header = String(headerBuf, 0, 8)
                    val size = header.toInt(10)
                    Log.d(javaClass.simpleName, header)

                    // Read image content
                    val imgBuf = ByteArray(size)
                    var curPos = 0
                    while (curPos < size)
                        curPos += inputStream.read(imgBuf, curPos, size - curPos)
                    val bmp = BitmapFactory.decodeByteArray(imgBuf, 0, size)
                    Log.d(javaClass.simpleName, "Image received with size $size")
                    listener?.onImage(bmp)

                } catch (e: Exception) {
                    e.printStackTrace()
                    client.close() // close socket for next try of connection
                    Log.d(javaClass.simpleName, "Connection closed")
                    break@loop
                }
            }
        }
    }
}