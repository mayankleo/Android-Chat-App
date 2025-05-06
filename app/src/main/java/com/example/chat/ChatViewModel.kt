package com.example.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chat.api.NetworkResponse
import com.example.chat.api.RetrofitInstance
import com.example.chat.api.SendOTPRequestModel
import com.example.chat.api.SendOTPResponseModel
import kotlinx.coroutines.launch

class ChatViewModel: ViewModel(){

    private val chatApi = RetrofitInstance.chatApi

    private val _chatResult = MutableLiveData<NetworkResponse<SendOTPResponseModel>>()
    val chatResult: LiveData<NetworkResponse<SendOTPResponseModel>> = _chatResult

    fun sendOTP(phone: String) {
        _chatResult.value = NetworkResponse.Loading
        Log.i("otp", "Sending OTP to phone number: $phone")
        val request = SendOTPRequestModel(phone = phone)
        viewModelScope.launch {
            try {
                val response = chatApi.sendOTP(request)
                if (response.isSuccessful) {
                    response.body()?.let{
                        _chatResult.value = NetworkResponse.Success(it)
                    }
                } else {
                    Log.e("otp", "Failed to send OTP:${response.code()} ${response.errorBody()?.string()}")
                    _chatResult.value = NetworkResponse.Error("Failed to send OTP")
                }
            } catch (e: Exception) {
                Log.e("OTP", "Exception: ${e.localizedMessage}")
                _chatResult.value = NetworkResponse.Error("Failed to send OTP")
            }
        }
    }
}
