package com.example.chat.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://chatappserver-ti56.onrender.com";
//    private const val BASE_URL = "http://10.0.2.2:3000";

    private fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val chatAppApis: ChatAppApis = getInstance().create(ChatAppApis::class.java)
}
