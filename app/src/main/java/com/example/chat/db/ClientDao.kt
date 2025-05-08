package com.example.chat.db

import android.R
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ClientDao {

    @Query("SELECT * FROM Message ORDER BY id DESC")
    fun getMessages(): LiveData<List<Message>>

    @Insert
    fun insertMessage(message: Message)

    @Query("SELECT * FROM Client LIMIT 1")
    fun getClient(): Client?

    @Query("DELETE FROM Client")
    fun deleteClient()

    @Insert
    fun insertClient(client: Client)

    @Query("UPDATE Client SET roomName = :roomName")
    fun updateClientRoomName(roomName: String)

}