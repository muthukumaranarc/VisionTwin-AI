package com.visiontwin.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visiontwin.app.data.model.ChatMessageDto
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    reportId: String,
    repository: VisionTwinRepository,
    onBack: () -> Unit
) {
    var messages by remember { mutableStateOf<List<ChatMessageDto>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(reportId) {
        repository.getChatHistory(reportId).onSuccess { messages = it }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Assistant", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue, titleContentColor = White,
                    navigationIconContentColor = White
                )
            )
        },
        containerColor = ScreenBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Chat messages
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(msg)
                }
                if (isSending) {
                    item { TypingIndicator() }
                }
            }

            // Input bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask a question...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        enabled = !isSending
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isBlank() || isSending) return@IconButton
                            val userMsg = inputText.trim()
                            inputText = ""
                            // Add user message locally immediately
                            messages = messages + ChatMessageDto(
                                id = "local_${System.currentTimeMillis()}",
                                sender = "USER",
                                messageText = userMsg
                            )
                            isSending = true
                            scope.launch {
                                repository.sendChat(reportId, userMsg)
                                    .onSuccess { aiMsg ->
                                        messages = messages + aiMsg
                                    }
                                    .onFailure {
                                        messages = messages + ChatMessageDto(
                                            id = "error",
                                            sender = "AI",
                                            messageText = "Sorry, I couldn't respond. Please try again."
                                        )
                                    }
                                isSending = false
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = White)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageDto) {
    val isUser = message.sender.equals("USER", ignoreCase = true)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) PrimaryBlue else LightGray,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.messageText,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) White else DarkText,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dot1 by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "d1")
    val dot2 by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(600, delayMillis = 200), RepeatMode.Reverse), label = "d2")
    val dot3 by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(600, delayMillis = 400), RepeatMode.Reverse), label = "d3")

    Row(horizontalArrangement = Arrangement.Start) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = LightGray,
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(dot1, dot2, dot3).forEach { alpha ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MediumGray.copy(alpha = 0.4f + alpha * 0.6f))
                    )
                }
            }
        }
    }
}
