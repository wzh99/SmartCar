package com.ei312.ui.util

import java.io.PrintWriter
import java.io.Serializable
import java.net.Socket
import java.util.concurrent.ArrayBlockingQueue

class MessageSender(private val port: Int) : Serializable {

    var host = ""
    set(value) {
        close()
        field = value
    }

    private var socket: Socket? = null
    private val connectTimeout = 1000L

    private val queueCapacity = 4
    private val queue = ArrayBlockingQueue<String>(queueCapacity)

    private val worker = Thread {
        while (true) {
            // Try to connect
            connect()
            if (socket == null) {
                Thread.sleep(connectTimeout)
                continue
            }

            // Take a string from queue and send it to host
            try {
                val writer = PrintWriter(socket!!.getOutputStream(), true)
                val str = queue.take()
                writer.println(str)
            } catch (e: Exception) {
                close() // try to reset socket
            }
        }
    }

    init {
        worker.isDaemon = true
        worker.start()
    }

    private fun connect() {
        if (socket != null) return
        try {
            socket = Socket(host, port)
        } catch (e: Exception) { }
    }

    fun send(msg: String) { queue.offer(msg) }

    private fun close() {
        socket?.close()
        socket = null
    }
}