package com.visiontwin.app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visiontwin.app.data.model.DiagnosisReportDto
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.components.*
import com.visiontwin.app.ui.theme.*

@Composable
fun AdminReportsScreen(
    repository: VisionTwinRepository,
    onBack: () -> Unit,
    onReportClick: (String) -> Unit
) {
    var reports by remember { mutableStateOf<List<DiagnosisReportDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repository.getAllReports()
            .onSuccess { reports = it }
        isLoading = false
    }

    Scaffold(
        topBar = { VTTopBar(title = "Diagnosis Reports", onBack = onBack) },
        containerColor = VTBackground
    ) { padding ->
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VTPrimary)
                }
            }
            reports.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    VTEmptyState(
                        text = "No reports yet",
                        subtitle = "Diagnosis reports will appear here once generated."
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reports) { report ->
                        VTCard(onClick = { onReportClick(report.id) }) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        report.machineName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = VTOnSurface
                                    )
                                    VTLabelChip(text = "REPORT")
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    report.problemDescription.ifBlank { report.diagnosisProblem },
                                    fontSize = 14.sp,
                                    color = VTOnSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    report.timestamp?.take(16) ?: "",
                                    fontSize = 12.sp,
                                    color = VTOutline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
