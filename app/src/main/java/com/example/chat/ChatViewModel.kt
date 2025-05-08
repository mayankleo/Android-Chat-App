package com.example.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chat.api.JoinWithCodeRequestModel
import com.example.chat.api.JoinWithCodeResponseModel
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
import kotlinx.coroutines.withContext

class ChatViewModel: ViewModel(){

    val clientDao = MainApplication.clientDatabase.getClientDao()
    private val chatAppApis = RetrofitInstance.chatAppApis

    private val _tokenExists = MutableLiveData<Boolean>()
    val tokenExists: LiveData<Boolean> = _tokenExists
    init {
        viewModelScope.launch(Dispatchers.IO) {
            val client = clientDao.getClient()
            _tokenExists.postValue(client?.token?.isNotEmpty())
        }
    }

    private val _sendOTPResult = MutableLiveData<NetworkResponse<SendOTPResponseModel>>()
    val sendOTPResult: LiveData<NetworkResponse<SendOTPResponseModel>> = _sendOTPResult
    fun sendOTP(phone: String) {
        _sendOTPResult.value = NetworkResponse.Loading
        val request = SendOTPRequestModel(phone = phone)
        viewModelScope.launch {
            try {
                val response = chatAppApis.sendOTP(request)
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


    private val _verifyOTPResult = MutableLiveData<NetworkResponse<VerifyOTPResponseModel>>()
    val verifyOTPResult: LiveData<NetworkResponse<VerifyOTPResponseModel>> = _verifyOTPResult
    fun verifyOTP(phone: String, otp: String) {
        _verifyOTPResult.value = NetworkResponse.Loading
        val request = VerifyOTPRequestModel(phone = phone, otp = otp)
        viewModelScope.launch {
            try {
                val response = chatAppApis.verifyOTP(request)
                if (response.isSuccessful) {
                    viewModelScope.launch(Dispatchers.IO) {
                        clientDao.deleteClient()
                        clientDao.insertClient(Client(token = response.body()!!.token, roomName = response.body()!!.code))
                    }
                    response.body()?.let{
                        _verifyOTPResult.value = NetworkResponse.Success(it)
                    }
                } else {
                    Log.e("OTP Verify", "Failed to verify OTP:${response.code()} ${response.errorBody()?.string()}")
                    _verifyOTPResult.value = NetworkResponse.Error("Failed to verify OTP")
                }
            } catch (e: Exception) {
                Log.e("OTP Verify", "Exception: ${e.localizedMessage}")
                _verifyOTPResult.value = NetworkResponse.Error("Failed to verify OTP")
            }
        }
    }


    private val _joinWithCodeResult = MutableLiveData<NetworkResponse<JoinWithCodeResponseModel>>()
    val joinWithCodeResult: LiveData<NetworkResponse<JoinWithCodeResponseModel>> = _joinWithCodeResult
    fun joinWithCode(code: String) {
        _joinWithCodeResult.value = NetworkResponse.Loading
        val request = JoinWithCodeRequestModel(code = code)
        viewModelScope.launch {
            try {
                val client = withContext(Dispatchers.IO) {
                    clientDao.getClient()
                }
                Log.d("SocketViewModel", "Client value: $client")
                val token = client?.token
                val response = chatAppApis.joinWithCode("Bearer $token",request)
                if (response.isSuccessful) {
                    withContext(Dispatchers.IO) {
                        clientDao.updateClientRoomName(response.body()!!.room.name)
                    }
                    response.body()?.let{
                        _joinWithCodeResult.value = NetworkResponse.Success(it)
                    }
                } else {
                    Log.e("Join Room", "Failed to Join Room:${response.code()} ${response.errorBody()?.string()}")
                    _joinWithCodeResult.value = NetworkResponse.Error("Failed to Join Room")
                }
            } catch (e: Exception) {
                Log.e("Join Room", "Exception: ${e.localizedMessage}")
                _joinWithCodeResult.value = NetworkResponse.Error("Failed to Join Room")
            }
        }
    }

}
