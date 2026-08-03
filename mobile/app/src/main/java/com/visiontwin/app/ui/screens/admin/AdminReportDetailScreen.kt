package com.visiontwin.app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Build
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
import com.visiontwin.app.ui.components.*
import com.visiontwin.app.ui.theme.*

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
        topBar = { VTTopBar(title = "Report Detail", onBack = onBack) },
        containerColor = VTBackground
    ) { padding ->
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VTPrimary)
                }
            }
            report == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    VTEmptyState(text = "Report not found")
                }
            }
            else -> {
                val r = report!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(r.machineName, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = VTOnSurface)
                            Text(r.timestamp?.take(16) ?: "", fontSize = 12.sp, color = VTOutline)
                        }
                        VTLabelChip(text = "REPORT")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AsyncImage(
                        model = RetrofitClient.fileUrl(r.uploadedImagePath),
                        contentDescription = "Uploaded image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DetailBlock(
                        title = "USER DESCRIPTION",
                        text = r.problemDescription.ifBlank { "—" },
                        icon = { Icon(Icons.Filled.Person, contentDescription = null, tint = VTOnSurfaceVariant, modifier = Modifier.size(16.dp)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DetailBlock(
                        title = "AI DIAGNOSIS",
                        text = r.diagnosisProblem,
                        icon = { Icon(Icons.Filled.Psychology, contentDescription = null, tint = VTError, modifier = Modifier.size(16.dp)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DetailBlock(
                        title = "SOLUTION",
                        text = r.diagnosisSolution,
                        icon = { Icon(Icons.Filled.Build, contentDescription = null, tint = VTSuccess, modifier = Modifier.size(16.dp)) }
                    )

                    if (r.highlightX != null && r.highlightY != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Highlight: (${String.format("%.2f", r.highlightX)}, ${String.format("%.2f", r.highlightY)}) r=${String.format("%.2f", r.highlightRadius ?: 0f)}",
                            fontSize = 12.sp, color = VTOutline
                        )
                    }

                    if (chatHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        VTSectionTitle(text = "Chat History")
                        chatHistory.forEach { msg ->
                            val isUser = msg.sender.equals("USER", true)
                            Surface(
                                color = if (isUser) VTPrimaryFixed else VTSurfaceContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        msg.sender.uppercase(),
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        letterSpacing = 0.6.sp,
                                        color = VTOnSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(msg.messageText, fontSize = 14.sp, color = VTOnSurface)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailBlock(
    title: String,
    text: String,
    icon: @Composable () -> Unit = {}
) {
    VTCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    title,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    letterSpacing = 0.6.sp,
                    color = VTOnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text, fontSize = 15.sp, color = VTOnSurface, lineHeight = 21.sp)
        }
    }
}
