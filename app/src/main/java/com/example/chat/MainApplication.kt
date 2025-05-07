package com.example.chat

import android.app.Application
import androidx.room.Room
import com.example.chat.db.ClientDatabase

class MainApplication : Application() {

    companion object {
        lateinit var clientDatabase: ClientDatabase
    }

    override fun onCreate() {
        super.onCreate()
        clientDatabase = Room.databaseBuilder(
            applicationContext,
            ClientDatabase::class.java,
            ClientDatabase.NAME
        ).build()
    }

}