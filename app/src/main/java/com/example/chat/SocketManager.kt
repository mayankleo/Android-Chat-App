//package com.example.chat
//
//import android.util.Log
//import io.socket.client.IO
//import io.socket.client.Socket
//import java.net.URISyntaxException
//
//class SocketManager {
//
//    private lateinit var socket: Socket
//    private val SERVER_URL = "http://10.0.2.2:3000" // Replace with your server URL
//
//    fun connect() {
//        try {
//            val opts = IO.Options()
//            opts.reconnection = true
//            socket = IO.socket(SERVER_URL, opts)
//
//            // Listen for connection
//            socket.on(Socket.EVENT_CONNECT) {
//                Log.i("SocketIO", "Connected")
//            }
//
//            // Listen for disconnection
//            socket.on(Socket.EVENT_DISCONNECT) {
//                Log.i("SocketIO", "Disconnected")
//            }
//
//            // Listen for error
//            socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
//                Log.e("SocketIO", "Error: ${args.joinToString()}")
//            }
//
//            // Listen for messages
//            socket.on("new message") { args ->
//                val msg = args[0] as String
//                Log.i("SocketIO", "New message: $msg")
//            }
//
//            socket.connect()
//        } catch (e: URISyntaxException) {
//            Log.e("SocketIO", "URI Error: ${e.message}")
//        }
//    }
//
//    fun sendMessage(message: String) {
//        socket.emit("chat message", message)
//    }
//
//    fun disconnect() {
//        socket.disconnect()
//    }
//}