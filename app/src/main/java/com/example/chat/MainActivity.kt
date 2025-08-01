package com.example.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.chat.ui.theme.ChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        val socketViewModel = ViewModelProvider(this)[SocketViewModel::class.java]
        enableEdgeToEdge()
        setContent {
            ChatTheme {
                NavigationStack(chatViewModel, socketViewModel)
            }
        }
    }
}
