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
fun LoginPage(navController: NavController, viewModel: ChatViewModel) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("9630570036") }
    var otp by remember { mutableStateOf("") }

    val chatResult = viewModel.chatResult.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Login", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        when (val result = chatResult.value) {
            is NetworkResponse.Error -> {
                Text(text = result.message, color = MaterialTheme.colorScheme.error)
            }

            NetworkResponse.Loading -> {
                CircularProgressIndicator()
            }

            is NetworkResponse.Success<*> -> {
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
//                            viewModel.verifyOTP(otp)
//                            navController.navigate(route = Screens.Home.route + "?text=Welcome")
                            navController.navigate(route = Screens.Chat.route)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = chatResult.value !is NetworkResponse.Loading
                ) {
                    if (chatResult.value is NetworkResponse.Loading) {
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

            null -> {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        if (it.all { it.isDigit() } && it.length <= 10) {
                            phoneNumber = it
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
                        if (!phoneNumber.isEmpty()) {
                            viewModel.sendOTP(phoneNumber)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = chatResult.value !is NetworkResponse.Loading
                ) {
                    if (chatResult.value is NetworkResponse.Loading) {
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
