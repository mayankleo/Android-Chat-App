package com.example.chat.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Client (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var clientName: String = "Me",
    var token: String,
    var roomName: String,
)

@Entity
data class Message(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var message: String,
    var senderByMe: Boolean,
    var timestamp: Long = System.currentTimeMillis()
)