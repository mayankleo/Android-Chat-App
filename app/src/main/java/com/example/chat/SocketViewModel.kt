package com.example.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chat.api.ApiUtils
import com.example.chat.api.NetworkResponse
import com.example.chat.api.RetrofitInstance
import com.example.chat.api.UploadFileResponseModel
import com.example.chat.api.UploadFileSocketResponseModel
import com.example.chat.db.Message
import com.example.chat.socketApi.SocketManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class SocketViewModel : ViewModel() {

    val clientDao = MainApplication.clientDatabase.getClientDao()
    val messageList: LiveData<List<Message>> = clientDao.getMessages()
    private val chatAppApis = RetrofitInstance.chatAppApis

    fun connectSocket() {
        viewModelScope.launch(Dispatchers.IO) {
            val client = clientDao.getClient()
            Log.d("SocketViewModel", "Client value: $client")
            val token = client?.token

            SocketManager.initialize(token.toString())
            SocketManager.connect()

            SocketManager.on("msg") { args ->
                val msg = args[0] as String
                val newMsg = Message(message = msg, senderByMe = false)
                viewModelScope.launch(Dispatchers.IO) {
                    clientDao.insertMessage(newMsg)
                }
            }

            SocketManager.on("uploadFile") { args ->
                val jsonObject = args[0] as JSONObject
                val res = Gson().fromJson(jsonObject.toString(), UploadFileSocketResponseModel::class.java)

                Log.d("SocketViewModel", "Received upload file: ${res.fileName} ${res.originalFileName}")
                val newFile = Message(fileName = res.fileName, originalFileName = res.originalFileName, senderByMe = false)
                viewModelScope.launch(Dispatchers.IO) {
                    clientDao.insertMessage(newFile)
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val newMsg = Message(message = text, senderByMe = true)
        viewModelScope.launch(Dispatchers.IO) {
            clientDao.insertMessage(newMsg)
        }
        SocketManager.emit("msg", text)
    }

    private val _uploadFileResult = MutableLiveData<NetworkResponse<UploadFileResponseModel>>()
    val uploadFileResult: LiveData<NetworkResponse<UploadFileResponseModel>> = _uploadFileResult
    fun uploadFile(file: File) {
        _uploadFileResult.value = NetworkResponse.Loading
        viewModelScope.launch {
            try {
                val client = withContext(Dispatchers.IO) {
                    clientDao.getClient()
                }
                Log.d("SocketViewModel", "Client value: $client")
                val token = client?.token
                if (token.isNullOrEmpty()) {
                    _uploadFileResult.value = NetworkResponse.Error("Token is missing")
                    return@launch
                }

                val multipart = ApiUtils.createMultipart(file)

                val response = withContext(Dispatchers.IO) {
                    chatAppApis.uploadFile("Bearer $token", multipart)
                }

                if (response.isSuccessful) {
                    response.body()?.let{
                        _uploadFileResult.value = NetworkResponse.Success(it)
                    }
                } else {
                    Log.e("Upload File", "Failed to Upload File: ${response.code()} ${response.errorBody()?.string()}")
                    _uploadFileResult.value = NetworkResponse.Error("Failed to upload file")
                }
            } catch (e: Exception) {
                Log.e("Upload File", "Exception: ${e.localizedMessage}")
                _uploadFileResult.value = NetworkResponse.Error("Failed to upload file")
            }
        }
    }

    private val _getFileResult = MutableLiveData<NetworkResponse<File>>()
    val getFileResult: LiveData<NetworkResponse<File>> = _getFileResult

    fun getFile(context: Context, fileName: String) {
        _getFileResult.value = NetworkResponse.Loading
        viewModelScope.launch {
            try {
                val client = withContext(Dispatchers.IO) {
                    clientDao.getClient()
                }
                Log.d("SocketViewModel", "Client value: $client")
                val token = client?.token
                if (token.isNullOrEmpty()) {
                    _getFileResult.value = NetworkResponse.Error("Token is missing")
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    chatAppApis.getFile("Bearer $token", fileName)
                }

                if (response.isSuccessful) {
                    response.body()?.let{
                        val file = File(context.filesDir, fileName)
                        withContext(Dispatchers.IO) {
                            it.byteStream().use { inputStream ->
                                file.outputStream().use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                        }
                        _getFileResult.value = NetworkResponse.Success(file)
                    }
                } else {
                    Log.e("Get File", "Failed to get File: ${response.code()} ${response.errorBody()?.string()}")
                    _getFileResult.value = NetworkResponse.Error("Failed to upload file")
                }
            } catch (e: Exception) {
                Log.e("Get File", "Exception: ${e.localizedMessage}")
                _getFileResult.value = NetworkResponse.Error("Failed to get file")
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        SocketManager.disconnect()
    }
}
