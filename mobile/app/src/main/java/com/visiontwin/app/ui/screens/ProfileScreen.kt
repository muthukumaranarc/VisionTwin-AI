package com.visiontwin.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visiontwin.app.data.model.DashboardStats
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.components.*
import com.visiontwin.app.ui.theme.*

@Composable
fun ProfileScreen(
    repository: VisionTwinRepository,
    onTabSelected: (VTTab) -> Unit,
    onAddMachine: () -> Unit,
    onReports: () -> Unit
) {
    var stats by remember { mutableStateOf(DashboardStats()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repository.getDashboardStats().onSuccess { stats = it }
        isLoading = false
    }

    Scaffold(
        topBar = { VTTopBar(title = "Profile") },
        containerColor = VTBackground,
        bottomBar = { VTBottomNav(selected = VTTab.Dashboard, onSelect = onTabSelected) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Brand header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(VTPrimaryContainer, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Psychology,
                        contentDescription = null,
                        tint = VTOnPrimaryContainer,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text("VisionTwin AI", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = VTOnSurface)
                    Text(
                        "PRECISION DIAGNOSTICS",
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        letterSpacing = 0.6.sp,
                        color = VTOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Workspace stats
            VTSectionTitle(text = "Workspace")
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile("Machines", stats.totalMachines.toString(), Icons.Filled.Build, Modifier.weight(1f))
                StatTile("Reports", stats.totalReports.toString(), Icons.Filled.Assessment, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile("Knowledge", stats.totalLayer1Datastores.toString(), Icons.Filled.Settings, Modifier.weight(1f))
                StatTile("Vectors", stats.totalLayer2Vectors.toString(), Icons.Filled.Psychology, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Admin tools
            VTSectionTitle(text = "Administration")
            Spacer(modifier = Modifier.height(4.dp))

            ProfileActionCard(
                title = "Add Machine",
                subtitle = "Register equipment, upload manuals and generate knowledge",
                icon = Icons.Filled.Build,
                onClick = onAddMachine
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProfileActionCard(
                title = "Diagnosis Reports",
                subtitle = "View all AI diagnosis history",
                icon = Icons.Filled.Assessment,
                onClick = onReports
            )

            Spacer(modifier = Modifier.height(20.dp))

            // App info
            VTSectionTitle(text = "About")
            Spacer(modifier = Modifier.height(4.dp))
            VTCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Application", "VisionTwin AI")
                    InfoRow("Version", "1.0.0")
                    InfoRow(
                        "Server",
                        com.visiontwin.app.data.api.RetrofitClient.BASE_URL
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatTile(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    VTCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(VTPrimaryFixed, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = VTOnPrimaryFixedVariant, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VTOnSurface)
                Text(title, fontSize = 12.sp, color = VTOnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProfileActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    VTCard(onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(VTPrimaryContainer, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = VTOnPrimaryContainer, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = VTOnSurface)
                Text(subtitle, fontSize = 12.sp, color = VTOnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = VTOnSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = VTOnSurface)
    }
}
