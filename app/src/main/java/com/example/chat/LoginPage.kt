package com.example.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chat.api.NetworkResponse

@Composable
fun LoginPage(navController: NavController, chatViewModel: ChatViewModel) {
    var phone by remember { mutableStateOf("9630570036") }
    var otp by remember { mutableStateOf("") }

    val sendOTPResult = chatViewModel.sendOTPResult.observeAsState()
    val verifyOTPResult = chatViewModel.verifyOTPResult.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Login", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        when (val sendOTPResponse = sendOTPResult.value) {
            is NetworkResponse.Error -> {
                Text(text = sendOTPResponse.message, color = MaterialTheme.colorScheme.error)
            }

            NetworkResponse.Loading -> {
                CircularProgressIndicator()
            }

            is NetworkResponse.Success<*> -> {

                when (val verifyOTPResponse = verifyOTPResult.value) {
                    is NetworkResponse.Error -> {
                        Text(
                            text = verifyOTPResponse.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    NetworkResponse.Loading -> {
                        CircularProgressIndicator()
                    }

                    is NetworkResponse.Success<*> -> {
                        Text(text = verifyOTPResponse.data.toString(), color = MaterialTheme.colorScheme.onPrimary)
                        Button(
                            onClick = {
                                navController.navigate(route = Screens.Chat.route)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Move To Chat Page", fontSize = 18.sp)
                        }
                    }

                    null -> {
                        OutlinedTextField(
                            value = otp,
                            onValueChange = {
                                if (it.all { it.isDigit() } && it.length <= 6) {
                                    otp = it
                                }
                            },
                            label = { Text("Enter OTP") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Gray
                            ),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (!otp.isEmpty()) {
                                    chatViewModel.verifyOTP(phone, otp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = verifyOTPResult.value !is NetworkResponse.Loading
                        ) {
                            if (verifyOTPResult.value is NetworkResponse.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.Black,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text(text = "Verify OTP", fontSize = 18.sp)
                            }
                        }
                    }
                }

            }

            null -> {
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        if (it.all { it.isDigit() } && it.length <= 10) {
                            phone = it
                        }
                    },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Gray
                    ),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (!phone.isEmpty()) {
                            chatViewModel.sendOTP(phone)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = sendOTPResult.value !is NetworkResponse.Loading
                ) {
                    if (sendOTPResult.value is NetworkResponse.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text(text = "Send OTP", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
