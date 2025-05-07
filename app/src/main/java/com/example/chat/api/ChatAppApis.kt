package com.example.chat.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


data class SendOTPRequestModel(
    val phone: String
)

data class SendOTPResponseModel(
    val message: String
)

interface SendOTPApi {

    @POST("/sendOTP")
    suspend fun sendOTP(@Body request: SendOTPRequestModel): Response<SendOTPResponseModel>

}

data class VerifyOTPRequestModel(
    val phone: String,
    val otp: String
)

data class VerifyOTPResponseModel(
    val message: String,
    val token: String,
    val code: String
)

interface VerifyOTPApi {

    @POST("/verifyOTP")
    suspend fun sendOTP(@Body request: VerifyOTPRequestModel): Response<VerifyOTPResponseModel>

}

