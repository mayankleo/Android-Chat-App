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
import com.example.chat.api.VerifyOTPRequestModel
import com.example.chat.api.VerifyOTPResponseModel
import com.example.chat.db.Client
import com.example.chat.db.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel: ViewModel(){

    val clientDao = MainApplication.clientDatabase.getClientDao()

    val client: LiveData<Client> = clientDao.getClient()

    private val sendOTPApi = RetrofitInstance.sendOTPApi
    private val _sendOTPResult = MutableLiveData<NetworkResponse<SendOTPResponseModel>>()
    val sendOTPResult: LiveData<NetworkResponse<SendOTPResponseModel>> = _sendOTPResult
    fun sendOTP(phone: String) {
        _sendOTPResult.value = NetworkResponse.Loading
        val request = SendOTPRequestModel(phone = phone)
        viewModelScope.launch {
            try {
                val response = sendOTPApi.sendOTP(request)
                if (response.isSuccessful) {
                    response.body()?.let{
                        _sendOTPResult.value = NetworkResponse.Success(it)
                    }
                } else {
                    Log.e("OTP Send", "Failed to send OTP:${response.code()} ${response.errorBody()?.string()}")
                    _sendOTPResult.value = NetworkResponse.Error("Failed to send OTP")
                }
            } catch (e: Exception) {
                Log.e("OTP Send", "Exception: ${e.localizedMessage}")
                _sendOTPResult.value = NetworkResponse.Error("Failed to send OTP")
            }
        }
    }

    private val verifyOTPApi = RetrofitInstance.verifyOTPApi
    private val _verifyOTPResult = MutableLiveData<NetworkResponse<VerifyOTPResponseModel>>()
    val verifyOTPResult: LiveData<NetworkResponse<VerifyOTPResponseModel>> = _verifyOTPResult
    fun verifyOTP(phone: String, otp: String) {
        _verifyOTPResult.value = NetworkResponse.Loading
        val request = VerifyOTPRequestModel(phone = phone, otp = otp)
        viewModelScope.launch {
            try {
                val response = verifyOTPApi.sendOTP(request)
                if (response.isSuccessful) {
                    viewModelScope.launch(Dispatchers.IO) {
                        clientDao.insertClient(Client(token = response.body()!!.token, roomName = response.body()!!.code))
                    }
                    response.body()?.let{
                        _verifyOTPResult.value = NetworkResponse.Success(it)
                    }
                } else {
                    Log.e("OTP Verify", "Failed to verify OTP:${response.code()} ${response.errorBody()?.string()}")
                    _verifyOTPResult.value = NetworkResponse.Error("Failed to send OTP")
                }
            } catch (e: Exception) {
                Log.e("OTP Verify", "Exception: ${e.localizedMessage}")
                _verifyOTPResult.value = NetworkResponse.Error("Failed to send OTP")
            }
        }
    }

}
