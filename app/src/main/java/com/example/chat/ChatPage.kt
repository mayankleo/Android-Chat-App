package com.example.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chat.db.Message
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun ChatPage(socketViewModel: SocketViewModel) {



    var textMessage by remember { mutableStateOf("") }
    val messageList by socketViewModel.messageList.observeAsState()

    LaunchedEffect(Unit) {
        socketViewModel.connectSocket()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            ChatScreen(
                textMessage = textMessage,
                messageList = messageList ?: emptyList(),
                onSendMessage = { socketViewModel.sendMessage(textMessage.trim()) },
                onMessageTextChange = { textMessage = it },
            )
        }
    }
}

fun longToTime(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val formatter = DateTimeFormatter.ofPattern("hh:mm a").withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

@Composable
fun ChatBubble(message: Message) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = if (message.senderByMe) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.senderByMe) 16.dp else 0.dp,
                        bottomEnd = if (message.senderByMe) 0.dp else 16.dp
                    )
                )
                .background(
                    if (message.senderByMe) Color.Blue else Color.Gray,
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.message,
                color = if (message.senderByMe) Color.White else Color.Black
            )
        }

        Text(
            text = longToTime(message.timestamp),
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ChatScreen(
    textMessage: String,
    messageList: List<Message>,
    onSendMessage: (String) -> Unit,
    onMessageTextChange: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    Column {


        Row(
            modifier = Modifier
                .background(Color.Blue)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(text = "Your Conversation", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
        }

        LaunchedEffect(messageList.size) {
            if (messageList.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true
        ) {
            itemsIndexed(messageList) { index: Int, message: Message ->
                ChatBubble(message = message)
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
            LaunchedEffect(textMessage) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 64.dp, max = 160.dp)
                    .verticalScroll(scrollState)
                    .background(Color.LightGray, RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = textMessage,
                    onValueChange = { onMessageTextChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(Color.Blue),
                    decorationBox = { innerTextField ->
                        if (textMessage.isEmpty()) {
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
                    if (textMessage.isNotBlank()) {
                        onSendMessage(textMessage.trim())
                        onMessageTextChange("")
                    }
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Blue)
                    .size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChatScreen() {
    var textMessage by remember { mutableStateOf("") }
    val dummyMessages = listOf(
        Message(message = "All good here!", senderByMe = false),
        Message(message = "I'm good, you?", senderByMe = true),
        Message(message = "Hi! How are you?", senderByMe = false),
        Message(message = "Hey!", senderByMe = true),
    )
    ChatScreen(
        textMessage = textMessage,
        messageList = dummyMessages,
        onSendMessage = { /* do nothing for preview */ },
        onMessageTextChange = { /* do nothing for preview */ },
    )
}