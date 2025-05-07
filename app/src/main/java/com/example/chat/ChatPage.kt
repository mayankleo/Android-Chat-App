package com.example.chat

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chat.ui.theme.ChatTheme
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.launch
import java.net.URISyntaxException
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Define colors for the new theme
val TealPrimary = Color(0xFF00897B) // Teal color for primary elements
val LightGrayBg = Color(0xFFF5F5F5) // Light gray for background
val MidGrayBubble = Color(0xFFE0E0E0) // Mid gray for received bubbles
val DarkGrayText = Color(0xFF616161) // Dark gray for timestamp text
val WhiteText = Color.White
val BlackText = Color.Black

data class ChatMessage(val text: String, val isSent: Boolean, val id: Int)

@Composable
fun ChatPage(navController: NavController? = null) {

    var socket: Socket? = remember { null }
    val SERVER_URL = "http://10.0.2.2:3000"
//    val SERVER_URL = "https://socketio-server2525.onrender.com"
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            ChatMessage("Same here 😊", isSent = true, id = 4),
            ChatMessage("I'm good! What about you?", isSent = false, id = 3),
            ChatMessage("Hi! How are you?", isSent = true, id = 2),
            ChatMessage("Hello!", isSent = false, id = 1)
        )
    }

    LaunchedEffect(key1 = true) {
        coroutineScope.launch {
            try {
                val opts = IO.Options()
                opts.reconnection = true
                opts.auth = mapOf(
                    "token" to "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjbGllbnRJRCI6IjdjNmVjM2U2LTRjY2ItNDdmNS05OGYzLWMxOWNiOWFiZDE1MyIsInBob25lIjoiOTYzMDU3MDAzOCIsImlhdCI6MTc0NjYxNjQxMCwiZXhwIjoxNzQ2NzAyODEwfQ.1Ggk5Y9Ljz0EDd0DRJ-lcPsB2AU7Fe8FE8WbayoDXxQ"
                )
                socket = IO.socket(SERVER_URL, opts)
                socket?.connect()

                socket?.on(Socket.EVENT_CONNECT) {
                    Log.i("SocketIO", "Connected")

                }

                socket?.on(Socket.EVENT_DISCONNECT) {
                    Log.i("SocketIO", "Disconnected")
                }

                socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Log.i("SocketIO", "Error: ${args.joinToString()}")

                }
                socket?.on("msg") { args ->
                    val msg = args[0] as String
                    val nextId = messages.first().id + 1

                    Log.i("SocketIO", "Next ID: $nextId")
                    messages.add(0, ChatMessage(msg.trim(), isSent = false, id = nextId))
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            } catch (e: URISyntaxException) {
                Log.e("SocketIO", "URI Error: ${e.message}")
            }
        }
    }

    DisposableEffect(key1 = socket) {
        onDispose {
            socket?.disconnect()
        }
    }


    var text by remember { mutableStateOf("") }
    Scaffold(modifier = Modifier.fillMaxSize().imePadding()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LightGrayBg)
        ) {

            Row(
                modifier = Modifier
                    .background(TealPrimary)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(text = "Your Conversation", fontSize = 20.sp, color = WhiteText)
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatBubble(message)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val scrollState = rememberScrollState()
                LaunchedEffect(text) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
                Box(
                    modifier = Modifier.weight(1f)
                        .heightIn(min = 64.dp, max = 160.dp)
                        .verticalScroll(scrollState)
                        .background(LightGrayBg, RoundedCornerShape(24.dp))
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = BlackText,
                            fontSize = 16.sp
                        ),
                        cursorBrush = SolidColor(TealPrimary),
                        decorationBox = { innerTextField ->
                            if (text.isEmpty()) {
                                Text(
                                    text = "Type a message...",
                                    color = Color.Gray
                                )
                            }
                            innerTextField()
                        }
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            messages.add(
                                0,
                                ChatMessage(
                                    text.trim(),
                                    isSent = true,
                                    id = (messages.maxOfOrNull { it.id } ?: -1) + 1))
                            socket?.emit("msg", text.trim())
                            text = ""
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        }
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(TealPrimary)
                        .size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = WhiteText,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

fun getCurrentTime(): String {
    val currentTime = LocalTime.now()
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    return currentTime.format(formatter)
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = if (message.isSent) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isSent) 16.dp else 0.dp,
                        bottomEnd = if (message.isSent) 0.dp else 16.dp
                    )
                )
                .background(
                    if (message.isSent) TealPrimary else MidGrayBubble,
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isSent) WhiteText else BlackText
            )
        }

        Text(
            text = getCurrentTime(),
            fontSize = 10.sp,
            color = DarkGrayText
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSimpleScreen() {
    ChatTheme {
        ChatPage()
    }
}
