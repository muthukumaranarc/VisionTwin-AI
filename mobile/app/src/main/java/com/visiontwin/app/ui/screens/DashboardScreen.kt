package com.visiontwin.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visiontwin.app.data.model.DashboardStats
import com.visiontwin.app.data.model.DiagnosisReportDto
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.components.*
import com.visiontwin.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    repository: VisionTwinRepository,
    onTabSelected: (VTTab) -> Unit,
    onReportClick: (String) -> Unit,
    onDiagnose: () -> Unit,
    onAddMachine: () -> Unit,
    onReports: () -> Unit
) {
    var stats by remember { mutableStateOf(DashboardStats()) }
    var reports by remember { mutableStateOf<List<DiagnosisReportDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            isLoading = true
            repository.getDashboardStats().onSuccess { stats = it }
            repository.getAllReports().onSuccess { reports = it.take(3) }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = {
            VTTopBar(
                title = "VisionTwin AI",
                actions = { }
            )
        },
        containerColor = VTBackground,
        bottomBar = { VTBottomNav(selected = VTTab.Dashboard, onSelect = onTabSelected) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Good morning, Operator", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Your factory at a glance.",
                fontSize = 14.sp,
                color = VTOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Primary CTA
            Card(
                onClick = onDiagnose,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = VTPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Psychology,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Start AI Diagnosis", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("Point camera at the machine component.", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Machines", stats.totalMachines.toString(), Modifier.weight(1f))
                StatCard("Reports", stats.totalReports.toString(), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Knowledge", stats.totalLayer1Datastores.toString(), Modifier.weight(1f))
                StatCard("Vectors", stats.totalLayer2Vectors.toString(), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Reports", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = VTOnSurface)
                TextButton(onClick = onReports) {
                    Text("View all", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = VTPrimary)
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VTPrimary, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                }
            } else if (reports.isEmpty()) {
                VTCard {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No diagnosis reports yet.", fontSize = 13.sp, color = VTOnSurfaceVariant)
                    }
                }
            } else {
                reports.forEach { report ->
                    Spacer(modifier = Modifier.height(8.dp))
                    VTCard(onClick = { onReportClick(report.id) }) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(report.machineName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = VTOnSurface)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                report.diagnosisProblem,
                                fontSize = 13.sp,
                                color = VTOnSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                report.timestamp?.take(16) ?: "",
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = VTOutline
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                VTCard(onClick = onAddMachine, modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = VTPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Add Machine", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = VTOnSurface)
                    }
                }
                VTCard(onClick = onReports, modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Assessment, contentDescription = null, tint = VTPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Reports", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = VTOnSurface)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    VTCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = VTOnSurface)
            Text(title, fontSize = 12.sp, color = VTOnSurfaceVariant)
        }
    }
}
