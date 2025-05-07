package com.example.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chat.db.Client
import com.example.chat.db.Message
import com.example.chat.socketApi.SocketManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SocketViewModel : ViewModel() {

    val clientDao = MainApplication.clientDatabase.getClientDao()
    val messageList: LiveData<List<Message>> = clientDao.getMessages()

    fun connectSocket() {
        viewModelScope.launch(Dispatchers.IO) {
            val client = clientDao.getClient()
            Log.d("SocketViewModel", "Client value: $client")
            val token = client.token

            SocketManager.initialize(token)
            SocketManager.connect()

            SocketManager.on("msg") { args ->
                val msg = args[0] as String
                val newMsg = Message(message = msg, senderByMe = false)
                viewModelScope.launch(Dispatchers.IO) {
                    clientDao.insertMessage(newMsg)
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val newMsg = Message(message = text, senderByMe = true)
        viewModelScope.launch(Dispatchers.IO) {
            clientDao.insertMessage(newMsg)
        }
        SocketManager.emit("msg", text)
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.disconnect()
    }
}
