package com.visiontwin.app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.visiontwin.app.data.api.RetrofitClient
import com.visiontwin.app.data.model.ChatMessageDto
import com.visiontwin.app.data.model.DiagnosisReportDto
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportDetailScreen(
    reportId: String,
    repository: VisionTwinRepository,
    onBack: () -> Unit
) {
    var report by remember { mutableStateOf<DiagnosisReportDto?>(null) }
    var chatHistory by remember { mutableStateOf<List<ChatMessageDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(reportId) {
        repository.getReportDetail(reportId).onSuccess { report = it }
        repository.getChatHistory(reportId).onSuccess { chatHistory = it }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Detail", fontWeight = FontWeight.Bold) },
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (report == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Report not found", color = ErrorRed)
            }
        } else {
            val r = report!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Machine info
                Text(r.machineName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PrimaryBlue)
                Text(r.timestamp?.take(16) ?: "", fontSize = 12.sp, color = MediumGray)

                Spacer(modifier = Modifier.height(12.dp))

                // Image
                AsyncImage(
                    model = RetrofitClient.fileUrl(r.uploadedImagePath),
                    contentDescription = "Uploaded image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(CardShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Problem
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("User Description", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SecondaryText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(r.problemDescription, fontSize = 15.sp, color = DarkText)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("AI Diagnosis", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ErrorRed)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(r.diagnosisProblem, fontSize = 15.sp, color = DarkText)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Solution", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SuccessGreen)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(r.diagnosisSolution, fontSize = 15.sp, color = DarkText)
                    }
                }

                // Highlight coordinates
                if (r.highlightX != null && r.highlightY != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Highlight: (${String.format("%.2f", r.highlightX)}, ${String.format("%.2f", r.highlightY)}) r=${String.format("%.2f", r.highlightRadius ?: 0f)}",
                        fontSize = 12.sp, color = MediumGray
                    )
                }

                // Chat history
                if (chatHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Chat History", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkText)
                    Spacer(modifier = Modifier.height(8.dp))

                    chatHistory.forEach { msg ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            shape = CardShape,
                            colors = CardDefaults.cardColors(
                                containerColor = if (msg.sender.equals("USER", true)) PrimaryBlueLight else NeutralGray
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    msg.sender,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SecondaryText
                                )
                                Text(msg.messageText, fontSize = 14.sp, color = DarkText)
                            }
                        }
                    }
                }
            }
        }
    }
}
