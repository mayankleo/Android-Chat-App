package com.example.chat.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Client::class, Message::class], version = 1)
abstract class ClientDatabase: RoomDatabase() {

    companion object {
        const val NAME = "Client_DB"
    }

    abstract fun getClientDao() : ClientDao

}