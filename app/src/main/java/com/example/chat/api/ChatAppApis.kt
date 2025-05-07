package com.example.chat.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


data class SendOTPRequestModel(
    val phone: String
)

data class SendOTPResponseModel(
    val message: String
)

data class VerifyOTPRequestModel(
    val phone: String,
    val otp: String
)

data class VerifyOTPResponseModel(
    val message: String,
    val token: String,
    val code: String
)

data class JoinWithCodeRequestModel(
    val code: String
)

data class JoinWithCodeResponseModel(
    val message: String,
    val room: Room
)
data class Room(
    val name: String,
    val roomID: String
)

interface ChatAppApis {

    @POST("/sendOTP")
    suspend fun sendOTP(@Body request: SendOTPRequestModel): Response<SendOTPResponseModel>

    @POST("/verifyOTP")
    suspend fun verifyOTP(@Body request: VerifyOTPRequestModel): Response<VerifyOTPResponseModel>

    @POST("/joinWithCode")
    suspend fun joinWithCode(@Header("authorization") token: String, @Body request: JoinWithCodeRequestModel): Response<JoinWithCodeResponseModel>

}
