package com.example.chat


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.room.util.TableInfo
import coil.compose.AsyncImage
import com.example.chat.api.ApiUtils
import com.example.chat.db.Message
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ChatPage(socketViewModel: SocketViewModel) {


    val context = LocalContext.current
    var textMessage by remember { mutableStateOf("") }

    val messageList by socketViewModel.messageList.observeAsState()

//    val uploadFileResult = socketViewModel.uploadFileResult.observeAsState()
//    val getFileResult = socketViewModel.getFileResult.observeAsState()

    LaunchedEffect(Unit) {
        socketViewModel.connectSocket()
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val file = ApiUtils.uriToFile(context, it)
                if (file != null) {
                    socketViewModel.uploadFile(file)
                } else {
                    Log.e("Upload", "File conversion failed")
                }
            }
        }
    )

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
            Row(
                modifier = Modifier
                    .background(Color.Blue)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Your Conversation",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
            ChatScreen(
                textMessage = textMessage,
                messageList = messageList ?: emptyList(),
                onSendMessage = { socketViewModel.sendMessage(textMessage.trim()) },
                onMessageTextChange = { textMessage = it },
                selectFile = { filePickerLauncher.launch("*/*") },
                socketViewModel = socketViewModel,
                context = context,
                onDeleteButtonClick = {socketViewModel.deleteAllMessages()}
            )
        }
    }
}


@Composable
fun ChatScreen(
    textMessage: String,
    messageList: List<Message>,
    onSendMessage: (String) -> Unit,
    onMessageTextChange: (String) -> Unit,
    selectFile: () -> Unit,
    onDeleteButtonClick: () -> Unit,
    socketViewModel: SocketViewModel,
    context: Context                                     ////////////////////look here
) {
    val listState = rememberLazyListState()
    Column {

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
                ChatBubble(message = message, socketViewModel = socketViewModel, context = context)
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
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                IconButton(
                    onClick = {
                        selectFile()
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Blue)
                        .size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(
                    onClick = {
                        onDeleteButtonClick()
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Blue)
                        .size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

        }
    }
}


fun longToTime(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val formatter = DateTimeFormatter.ofPattern("hh:mm a").withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

@Composable
fun ChatBubble(message: Message, socketViewModel: SocketViewModel, context: Context) {

    val getFileResult = socketViewModel.getFileResult.observeAsState()
    val filePath = remember(message.fileName) { mutableStateOf<File?>(null) }

    LaunchedEffect(message.fileName) {
        val fileName = message.fileName
        if (!fileName.isNullOrBlank()) {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                filePath.value = file
            } else {
                socketViewModel.getFile(context, fileName)
            }
        }
    }

    LaunchedEffect(getFileResult.value) {
        val fileName = message.fileName
        if (!fileName.isNullOrBlank()) {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                filePath.value = file
            }
        }
    }

   LaunchedEffect(filePath.value) {

   }
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
            when {
                message.message != null -> {
                    Text(
                        text = message.message!!,
                        color = if (message.senderByMe) Color.White else Color.Black
                    )
                }

                filePath.value != null -> {
                    val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", filePath.value!!)
                    val fileType = context.contentResolver.getType(fileUri)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "*/*"
                        putExtra(Intent.EXTRA_STREAM, fileUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val saveLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.CreateDocument("*/*"),
                        onResult = { destinationUri ->
                            destinationUri?.let {
                                context.contentResolver.openOutputStream(it)?.use { output ->
                                    filePath.value!!.inputStream().use { input ->
                                        input.copyTo(output)
                                    }
                                }
                            }
                        }
                    )
                    Column{
                        when {
                            fileType?.startsWith("image/") == true -> {
                                AsyncImage(
                                    model = fileUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            fileType?.startsWith("video/") == true -> {
                                ExoVideoPlayer(fileUri)
                            }

                            else -> {
                                Text(text = "📁 ${message.originalFileName!!}", color = Color.White)
                            }
                        }
                        Row{
                            Button(
                                onClick = {
                                    context.startActivity(Intent.createChooser(shareIntent, "Share file"))
                                }
                            ) {
                                Text(text="share")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(
                                onClick = {
                                    saveLauncher.launch(filePath.value!!.name)
                                }
                            ) {
                                Text(text="save")
                            }
                        }
                    }
                }

                else -> {
                    CircularProgressIndicator(
                        color = if (message.senderByMe) Color.White else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Text(
            text = longToTime(message.timestamp),
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ExoVideoPlayer(videoUri: Uri) {
    val context = LocalContext.current

    // Create and remember ExoPlayer
    val exoPlayer = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = false
        }
    }

    // Dispose ExoPlayer when no longer needed
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Use AndroidView to show PlayerView
    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}


//@Preview(showBackground = true)
//@Composable
//fun PreviewChatScreen() {
//    var textMessage by remember { mutableStateOf("") }
//    val dummyMessages = listOf(
//        Message(message = "All good here!", senderByMe = false),
//        Message(message = "I'm good, you?", senderByMe = true),
//        Message(message = "Hi! How are you?", senderByMe = false),
//        Message(message = "Hey!", senderByMe = true),
//    )
//
//    ChatScreen(
//        textMessage = textMessage,
//        messageList = dummyMessages,
//        onSendMessage = { /* do nothing for preview */ },
//        onMessageTextChange = { /* do nothing for preview */ },
//        selectFile = { /* do nothing for preview */ }
//    )
//}