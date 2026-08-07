package com.visiontwin.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.visiontwin.app.ui.theme.*

@Composable
fun ProfileDialog(onDismiss: () -> Unit) {
    var activeTab by remember { mutableStateOf("personal") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VTPrimaryContainer.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nil Yeager Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VTOnSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = VTOnSurfaceVariant)
                    }
                }

                // Tab Row
                ScrollableTabRow(
                    selectedTabIndex = when (activeTab) {
                        "personal" -> 0
                        "security" -> 1
                        "shift" -> 2
                        else -> 3
                    },
                    containerColor = Color.White,
                    contentColor = VTPrimary,
                    edgePadding = 8.dp,
                    modifier = Modifier.height(44.dp)
                ) {
                    Tab(
                        selected = activeTab == "personal",
                        onClick = { activeTab = "personal" },
                        text = { Text("Personal", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == "security",
                        onClick = { activeTab = "security" },
                        text = { Text("Security", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == "shift",
                        onClick = { activeTab = "shift" },
                        text = { Text("Shift", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == "expertise",
                        onClick = { activeTab = "expertise" },
                        text = { Text("Expertise", fontSize = 12.sp) }
                    )
                }

                Divider(color = VTOutlineVariant.copy(alpha = 0.4f))

                // Body content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (activeTab) {
                        "personal" -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(VTPrimaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("NY", fontWeight = FontWeight.Bold, color = VTOnPrimaryContainer)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Nil Yeager", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VTOnSurface)
                                    Text("System Operator", fontSize = 12.sp, color = VTPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            ProfileField("Employee ID", "VT-90214")
                            ProfileField("Department", "Production Line B")
                            ProfileField("Assigned Line", "Molding Operations")
                        }
                        "security" -> {
                            Text("Credentials & Security", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VTOnSurface)
                            Button(
                                onClick = {},
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VTPrimary)
                            ) {
                                Text("Update Password", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Two-Factor Auth (2FA)", fontSize = 12.sp, color = VTOnSurfaceVariant)
                                Text("Enabled", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                            Divider()
                            Text("Active Sessions", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VTOutline)
                            Text("Chrome PC • Current Session", fontSize = 12.sp, color = VTOnSurfaceVariant)
                            Text("iOS Mobile App • Active 2h ago", fontSize = 12.sp, color = VTOnSurfaceVariant)
                        }
                        "shift" -> {
                            ProfileField("Current Shift", "Morning Shift (06:00 - 14:00 UTC)")
                            ProfileField("Availability Status", "Online & Active")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Emergency Contact Preferences", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = true, onCheckedChange = {})
                                Text("Alert via push notifications for severity > High", fontSize = 11.sp)
                            }
                        }
                        "expertise" -> {
                            Text("Machine Specialties", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VTOnSurface)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                BadgeChip("CNC Spindles")
                                BadgeChip("Hydraulics")
                                BadgeChip("Lubrication")
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            ProfileField("Technician Level", "Level 3 Senior Specialist")
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Certified Machinery Safety Inspector", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VTPrimary)
                    ) {
                        Text("Close", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    var activeTab by remember { mutableStateOf("diagnostic") }

    // States for local controls
    var acousticSens by remember { mutableStateOf(70f) }
    var thermalSens by remember { mutableStateOf(85f) }
    var isDarkMode by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VTPrimaryContainer.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "System Settings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VTOnSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = VTOnSurfaceVariant)
                    }
                }

                // Tab Row
                ScrollableTabRow(
                    selectedTabIndex = when (activeTab) {
                        "diagnostic" -> 0
                        "notifications" -> 1
                        "data" -> 2
                        else -> 3
                    },
                    containerColor = Color.White,
                    contentColor = VTPrimary,
                    edgePadding = 8.dp,
                    modifier = Modifier.height(44.dp)
                ) {
                    Tab(
                        selected = activeTab == "diagnostic",
                        onClick = { activeTab = "diagnostic" },
                        text = { Text("Diagnostics", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == "notifications",
                        onClick = { activeTab = "notifications" },
                        text = { Text("Alerts", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == "data",
                        onClick = { activeTab = "data" },
                        text = { Text("Data", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == "interface",
                        onClick = { activeTab = "interface" },
                        text = { Text("Display", fontSize = 12.sp) }
                    )
                }

                Divider(color = VTOutlineVariant.copy(alpha = 0.4f))

                // Body content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (activeTab) {
                        "diagnostic" -> {
                            Text("Sensitivity Thresholds", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VTOnSurface)
                            Column {
                                Text("Acoustic Sensitivity: ${acousticSens.toInt()}%", fontSize = 11.sp, color = VTOnSurfaceVariant)
                                Slider(value = acousticSens, onValueChange = { acousticSens = it }, valueRange = 10f..100f)
                            }
                            Column {
                                Text("Thermal Sensitivity: ${thermalSens.toInt()}%", fontSize = 11.sp, color = VTOnSurfaceVariant)
                                Slider(value = thermalSens, onValueChange = { thermalSens = it }, valueRange = 10f..100f)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Active AI Reasoner Version", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("VisionTwin Core v2.4 (Latest)", fontSize = 12.sp, color = VTOnSurfaceVariant)
                        }
                        "notifications" -> {
                            Text("Alert Severity Triggers", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VTOnSurface)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Critical / Emergency Anomaly (SMS & Push)", fontSize = 12.sp)
                                Switch(checked = true, onCheckedChange = {})
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Calibration Warnings (Push)", fontSize = 12.sp)
                                Switch(checked = true, onCheckedChange = {})
                            }
                            Divider()
                            Text("Line & Machine Subscriptions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = true, onCheckedChange = {})
                                Text("Subscribe to Production Line B", fontSize = 12.sp)
                            }
                        }
                        "data" -> {
                            Text("Telemetry Pipelines", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VTOnSurface)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pinecone Vector DB Connection", fontSize = 12.sp)
                                Text("Connected", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("IoT Sensors Feed (Modbus/OPC UA)", fontSize = 12.sp)
                                Text("Streaming", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                            Divider()
                            Text("Default Export Format", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("PDF (Detailed diagnosis log)", fontSize = 12.sp, color = VTOnSurfaceVariant)
                        }
                        "interface" -> {
                            Text("Display preferences", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VTOnSurface)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Dark Mode Toggle", fontSize = 12.sp)
                                Switch(checked = isDarkMode, onCheckedChange = { isDarkMode = it })
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Regional Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Language: English (US)", fontSize = 12.sp, color = VTOnSurfaceVariant)
                            Text("Timezone: UTC (Universal Time)", fontSize = 12.sp, color = VTOnSurfaceVariant)
                        }
                    }
                }

                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VTPrimary)
                    ) {
                        Text("Save & Close", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label.uppercase(), fontSize = 10.sp, color = VTOutline, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, color = VTOnSurface, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = VTOutlineVariant.copy(alpha = 0.4f))
    }
}

@Composable
private fun BadgeChip(text: String) {
    Box(
        modifier = Modifier
            .background(VTPrimaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 11.sp, color = VTOnPrimaryContainer, fontWeight = FontWeight.Bold)
    }
}
