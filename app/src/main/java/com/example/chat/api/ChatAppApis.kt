package com.example.chat.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.io.File

// send OTP
data class SendOTPRequestModel(
    val phone: String
)
data class SendOTPResponseModel(
    val message: String
)

// verify OTP
data class VerifyOTPRequestModel(
    val phone: String,
    val otp: String
)
data class VerifyOTPResponseModel(
    val message: String,
    val token: String,
    val roomCode: String
)

// join with code
data class JoinWithCodeRequestModel(
    val roomCode: String
)
data class JoinWithCodeResponseModel(
    val message: String,
    val room: Room
)
data class Room(
    val roomName: String,
    val roomID: String
)

// upload file
data class UploadFileResponseModel(
    val message: String? = "",
    val fileUrl: String,
    val fileName: String,
    val originalFileName: String
)
data class UploadFileSocketResponseModel(
    val fileName: String,
    val originalFileName: String
)


interface ChatAppApis {

    @POST("/sendOTP")
    suspend fun sendOTP(@Body request: SendOTPRequestModel): Response<SendOTPResponseModel>

    @POST("/verifyOTP")
    suspend fun verifyOTP(@Body request: VerifyOTPRequestModel): Response<VerifyOTPResponseModel>

    @POST("/joinWithCode")
    suspend fun joinWithCode(@Header("authorization") token: String, @Body request: JoinWithCodeRequestModel): Response<JoinWithCodeResponseModel>

    @Multipart
    @POST("/uploadFile")
    suspend fun uploadFile(@Header("authorization") token: String, @Part file: MultipartBody.Part): Response<UploadFileResponseModel>

    @GET("/file/{filename}")
    suspend fun getFile(@Header("Authorization") token: String, @Path("filename") filename: String): Response<ResponseBody>

}
