package com.example.chat

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
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateListOf

data class ChatMessage(val text: String, val isSent: Boolean, val id: Int)

@Composable
fun ChatScreen(navController: NavController? = null) {
    val messages = remember { mutableStateListOf(
        ChatMessage("Hello!", isSent = false, id = 0),
        ChatMessage("Hi! How are you?", isSent = true, id = 1),
        ChatMessage("I'm good! What about you?", isSent = false, id = 2),
        ChatMessage("Same here 😊", isSent = true, id = 3)
    )}
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.hsv(300f, 0.03f, 1f))
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .background(Color.hsv(300f, 0.1f, 1f))
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = "Your Conversation", fontSize = 24.sp, color = Color.Black)
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                reverseLayout = true
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatBubble(message)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawLine(
                            color = Color.hsv(300f, 0.1f, 0.5f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    },
                verticalAlignment = Alignment.Bottom,
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier
                        .weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            messages.add(0, ChatMessage(text, isSent = true, id = messages.size))
                            text = "" // Clear input after sending
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.hsv(300f, 0.03f, 1f))
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.Black,
                        modifier = Modifier.rotate(-40f),
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalAlignment = if (message.isSent) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (message.isSent) Color.hsv(300f, 0.1f, 1f) else Color.Gray,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(8.dp)
        ) {
            Text(text = message.text, color = if (message.isSent) Color.Black else Color.White)
        }

        // Time below the bubble
        Text(
            text = "12:30 PM", // Replace with actual time if available
            fontSize = 12.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(top = 2.dp, start = 8.dp, end = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSimpleScreen() {
    ChatScreen()
}
