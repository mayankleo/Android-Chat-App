package com.example.chat.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApi {

    @POST("/sendOTP")
    suspend fun sendOTP(@Body request: SendOTPRequestModel): Response<SendOTPResponseModel>

}
