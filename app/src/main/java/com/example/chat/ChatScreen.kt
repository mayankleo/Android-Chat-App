package com.example.chat

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import com.example.chat.ui.theme.ChatTheme
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
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
fun ChatScreen(navController: NavController? = null) {
    val context = LocalContext.current

    var socket: Socket? = remember { null }
//    val SERVER_URL = "http://10.0.2.2:3000"
    val SERVER_URL = "https://socketio-server2525.onrender.com"
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
                    Toast.makeText(context, "Error Connecting to Server", Toast.LENGTH_LONG).show()
                }
                socket?.on("reply") { args ->
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
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                reverseLayout = true
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatBubble(message)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .drawBehind {
                        drawLine(
                            color = MidGrayBubble,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Type a message...", color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = LightGrayBg,
                        unfocusedContainerColor = LightGrayBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = BlackText,
                        unfocusedTextColor = BlackText,
                        cursorColor = TealPrimary
                    )
                )
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            messages.add(
                                0,
                                ChatMessage(
                                    text.trim(),
                                    isSent = true,
                                    id = (messages.maxOfOrNull { it.id } ?: -1) + 1))
                            socket?.emit("message", text.trim())
                            text = "" // Clear input after sending
                            // Consider scrolling to the new message if needed (might require CoroutineScope)
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        }
                    },
                    modifier = Modifier
                        // .padding(horizontal = 4.dp, vertical = 4.dp) // Padding handled by Row
                        .clip(RoundedCornerShape(50)) // Make it circular
                        .background(TealPrimary) // Teal background for button
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = WhiteText, // White icon
                        modifier = Modifier.rotate(-40f), // Keep rotation
                    )
                }
            }
        }
    }
}

fun getCurrentTime(): String {
    val currentTime = LocalTime.now() // Get the current time
    val formatter = DateTimeFormatter.ofPattern("hh:mm a") // Define the desired format
    return currentTime.format(formatter) // Format the time and return it as a string
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp), // Consistent padding
        horizontalAlignment = if (message.isSent) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape( // Custom rounding for chat bubble effect
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isSent) 16.dp else 0.dp,
                        bottomEnd = if (message.isSent) 0.dp else 16.dp
                    )
                )
                .background(
                    if (message.isSent) TealPrimary else MidGrayBubble, // Use theme colors
                )
                .padding(horizontal = 12.dp, vertical = 8.dp) // Adjust padding inside bubble
        ) {
            Text(
                text = message.text,
                color = if (message.isSent) WhiteText else BlackText // Adjust text color based on bubble
            )
        }

        // Time below the bubble
        Text(
            text = getCurrentTime(), // Replace with actual time if available
            fontSize = 10.sp, // Smaller font size for time
            color = DarkGrayText, // Use dark gray for timestamp
            modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp) // Adjust padding
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSimpleScreen() {
    ChatTheme { // Wrap preview in theme if you have one defined
        ChatScreen()
    }
}

