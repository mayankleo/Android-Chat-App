package com.example.chat.socketApi

import android.util.Log
import com.example.chat.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException


object SocketManager {
    private var socket: Socket? = null

    fun initialize(token: String) {
        try {
            val opts = IO.Options().apply {
                reconnection = true
                auth = mapOf("token" to token)
            }
            socket = IO.socket(BuildConfig.SERVER_URL, opts)
        } catch (e: URISyntaxException) {
            Log.e("SocketIO", "URI Exception: ${e.message}")
        }
    }

    fun connect() = socket?.connect()
    fun disconnect() = socket?.disconnect()
    fun isConnected() = socket?.connected() == true

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
