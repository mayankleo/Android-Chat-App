package com.example.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chat.api.NetworkResponse
import com.example.chat.api.VerifyOTPResponseModel

@Composable
fun LoginPage(navController: NavController, chatViewModel: ChatViewModel) {
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }

    val sendOTPResult = chatViewModel.sendOTPResult.observeAsState()
    val verifyOTPResult = chatViewModel.verifyOTPResult.observeAsState()
    val joinWithCodeResult = chatViewModel.joinWithCodeResult.observeAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White),
            verticalArrangement = Arrangement.Center
        ) {
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

                            when (val joinWithCodeResponse = joinWithCodeResult.value) {
                                is NetworkResponse.Error -> Text(
                                    text = joinWithCodeResponse.message,
                                    color = MaterialTheme.colorScheme.error
                                )

                                NetworkResponse.Loading -> {
                                    CircularProgressIndicator()
                                }

                                is NetworkResponse.Success<*> -> {
                                    navController.navigate(route = Screens.Chat.route)
                                }

                                null -> {
                                    val responseJson = verifyOTPResponse.data as VerifyOTPResponseModel
                                    RoomCodeScreen(
                                        roomCode = roomCode,
                                        onRoomCodeChange = { roomCode = it },
                                        responseJson = responseJson,
                                        navigateTo = { navController.navigate(route = it) },
                                        onJoinWithCode = { chatViewModel.joinWithCode(roomCode) }
                                    )

                                }
                            }


                        }

                        null -> {
                            OTPInputScreen(
                                otp = otp,
                                onOTPChange = { phone = it },
                                onVerifyOTP = { chatViewModel.verifyOTP(phone, otp) },
                                isLoading = verifyOTPResult.value is NetworkResponse.Loading
                            )
                        }
                    }

                }

                null -> {
                    PhoneInputScreen(
                        phone = phone,
                        onPhoneChange = { phone = it },
                        onSendOTP = { chatViewModel.sendOTP(phone) },
                        isLoading = sendOTPResult.value is NetworkResponse.Loading
                    )
                }
            }
        }
    }
}

@Composable
fun PhoneInputScreen(
    phone: String,
    onPhoneChange: (String) -> Unit,
    onSendOTP: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Login",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = {
                if (it.all { it.isDigit() } && it.length <= 10) {
                    onPhoneChange(it)
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
                if (!phone.isEmpty() && phone.length == 10) {
                    onSendOTP()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
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

@Composable
fun OTPInputScreen(
    otp: String,
    onOTPChange: (String) -> Unit,
    onVerifyOTP: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Login",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = otp,
            onValueChange = {
                if (it.all { it.isDigit() } && it.length <= 4) {
                    onOTPChange(it)
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
                if (!otp.isEmpty() && otp.length == 4) {
                    onVerifyOTP()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
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


@Composable
fun RoomCodeScreen(
    responseJson: VerifyOTPResponseModel,
    roomCode: String,
    onRoomCodeChange: (String) -> Unit,
    onJoinWithCode: () -> Unit,
    navigateTo: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Room Code",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "JWT Token: ${responseJson.token}",
            fontSize = 12.sp,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Share Room Code To Friend:  ${responseJson.code}",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    navigateTo(Screens.Chat.route)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Then Click Here", fontSize = 18.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "OR",
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = roomCode,
            onValueChange = {
                if (it.length <= 4) {
                    onRoomCodeChange(it)
                }
            },
            label = { Text("Enter Room Code") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Gray
            ),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (!roomCode.isEmpty() && roomCode.length == 4) {
                    onJoinWithCode()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Join Room", fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPhoneInputScreen() {
    var phone by remember { mutableStateOf("") }

    PhoneInputScreen(
        phone = phone,
        onPhoneChange = { phone = it },
        onSendOTP = { /* do nothing for preview */ },
        isLoading = false
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewOTPInputScreen() {
    var otp by remember { mutableStateOf("") }

    OTPInputScreen(
        otp = otp,
        onOTPChange = { otp = it },
        onVerifyOTP = { /* do nothing for preview */ },
        isLoading = false
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewRoomCodeScreen() {
    var roomCode by remember { mutableStateOf("") }

    RoomCodeScreen(
        roomCode = roomCode,
        onRoomCodeChange = { roomCode = it },
        responseJson = VerifyOTPResponseModel(message = "hi", token = "mock_jwt_token_12345", code = "ABCD") ,
        navigateTo = { /* do nothing for preview */ },
        onJoinWithCode = { /* do nothing for preview */ }
    )
}