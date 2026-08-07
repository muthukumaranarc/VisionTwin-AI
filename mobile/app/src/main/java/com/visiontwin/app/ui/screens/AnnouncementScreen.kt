package com.visiontwin.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visiontwin.app.ui.components.*
import com.visiontwin.app.ui.theme.*

interface AnnouncementItem {
    val id: String
    val title: String
    val category: String
    val date: String
    val content: String
    val important: Boolean
}

data class AnnouncementModel(
    override val id: String,
    override val title: String,
    override val category: String,
    override val date: String,
    override val content: String,
    override val important: Boolean
) : AnnouncementItem

@Composable
fun AnnouncementScreen(
    onTabSelected: (VTTab) -> Unit
) {
    val announcements = listOf(
        AnnouncementModel(
            "1",
            "AI Diagnostic Engine Upgraded to v2.4",
            "System",
            "2026-08-05",
            "We have updated our core neural networks for acoustic and thermal anomaly detection. Accuracy on rotating shafts, gearboxes, and hydraulic systems has been increased by 14%. Please upload clear audio clips or thermal images when diagnosing.",
            true
        ),
        AnnouncementModel(
            "2",
            "Scheduled Maintenance Shutdown: Line 4 and 5",
            "Maintenance",
            "2026-08-04",
            "Line 4 (Injection Molding) and Line 5 (Robotic Arm Assembly) will undergo scheduled calibration and diagnostic verification on Sunday, August 9th, from 06:00 to 14:00 UTC. Direct dashboard monitoring will be temporarily disabled.",
            false
        ),
        AnnouncementModel(
            "3",
            "Updated Lockout-Tagout (LOTO) Procedures",
            "Safety",
            "2026-08-01",
            "New safety compliance protocols require dual-verification on all high-voltage electrical enclosures and pneumatic pumps. Read the reference guides in the Interactive Learning section before initiating troubleshooting.",
            true
        ),
        AnnouncementModel(
            "4",
            "Interactive Learning Modules Added for CNC Diagnostics",
            "General",
            "2026-07-28",
            "Three new training modules have been added covering common CNC spindle defects, vibration alignment patterns, and tool-wear diagnostic signatures. Access them directly via the Interactive Learning page.",
            false
        )
    )

    Scaffold(
        topBar = { VTTopBar(title = "Announcements") },
        containerColor = VTBackground,
        bottomBar = { VTBottomNav(selected = VTTab.Announcement, onSelect = onTabSelected) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Broadcast Hub",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = VTOnSurface
            )
            Text(
                "Stay updated with system releases, safety guidelines, and active maintenance logs.",
                fontSize = 13.sp,
                color = VTOnSurfaceVariant
            )

            announcements.forEach { item ->
                val borderStroke = if (item.important) 1.5.dp else 1.dp
                val borderColor = if (item.important) VTError else VTOutlineVariant.copy(alpha = 0.5f)
                val cardBackground = if (item.important) VTErrorContainer.copy(alpha = 0.1f) else Color.White

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(borderStroke, borderColor, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val catBg = when (item.category) {
                                    "System" -> VTPrimaryContainer
                                    "Maintenance" -> VTSecondaryContainer
                                    "Safety" -> VTErrorContainer
                                    else -> VTSurfaceContainerHighest
                                }
                                val catText = when (item.category) {
                                    "System" -> VTOnPrimaryContainer
                                    "Maintenance" -> VTOnSecondaryContainer
                                    "Safety" -> VTOnErrorContainer
                                    else -> VTOnSurfaceVariant
                                }
                                Box(
                                    modifier = Modifier
                                        .background(catBg, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(item.category, fontSize = 10.sp, color = catText, fontWeight = FontWeight.Bold)
                                }

                                if (item.important) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = VTError,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("CRITICAL", fontSize = 10.sp, color = VTError, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(item.date, fontSize = 11.sp, color = VTOutline)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = VTOnSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.content,
                            fontSize = 13.sp,
                            color = VTOnSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
