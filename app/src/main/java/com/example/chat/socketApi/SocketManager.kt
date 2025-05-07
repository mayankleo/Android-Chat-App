package com.example.chat.socketApi

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketManager {
    private const val SERVER_URL = "http://10.0.2.2:3000"
    private var socket: Socket? = null

    fun initialize(token: String) {
        try {
            val opts = IO.Options().apply {
                reconnection = true
                auth = mapOf("token" to token)
            }
            socket = IO.socket(SERVER_URL, opts)
        } catch (e: URISyntaxException) {
            Log.e("SocketIO", "URI Exception: ${e.message}")
        }
    }

    fun connect() = socket?.connect()
    fun disconnect() = socket?.disconnect()
    fun isConnected() = socket?.connected() ?: false

    fun on(event: String, callback: (Array<Any>) -> Unit) {
        socket?.on(event) { args -> callback(args) }
    }

    fun emit(event: String, data: Any) {
        socket?.emit(event, data)
    }

    fun off(event: String) {
        socket?.off(event)
    }
}
