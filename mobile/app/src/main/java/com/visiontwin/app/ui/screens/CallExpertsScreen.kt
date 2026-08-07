package com.visiontwin.app.ui.screens

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
import androidx.compose.material.icons.filled.People
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
import com.visiontwin.app.data.model.MachineDto
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.components.*
import com.visiontwin.app.ui.theme.*
import kotlinx.coroutines.launch

interface MobileExpert {
    val id: String
    val name: String
    val role: String
    val status: String // "available" or "busy"
    val nextFreeTime: String?
    val specialties: List<String>
}

data class MobileExpertModel(
    override val id: String,
    override val name: String,
    override val role: String,
    override val status: String,
    override val nextFreeTime: String?,
    override val specialties: List<String>
) : MobileExpert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallExpertsScreen(
    repository: VisionTwinRepository,
    onTabSelected: (VTTab) -> Unit
) {
    var machines by remember { mutableStateOf<List<MachineDto>>(emptyList()) }
    var selectedExpert by remember { mutableStateOf<MobileExpert?>(null) }
    var isModalOpen by remember { mutableStateOf(false) }
    var targetMachineId by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf("Medium") }
    var notes by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val experts = listOf(
        MobileExpertModel(
            "1",
            "Nil Yeager",
            "Chief Vibration Analyst",
            "available",
            null,
            listOf("CNC Spindles", "High-speed Gearboxes")
        ),
        MobileExpertModel(
            "2",
            "Theron Trump",
            "Lead Predictive Maintenance",
            "available",
            null,
            listOf("Pneumatics", "Acoustic Detection")
        ),
        MobileExpertModel(
            "3",
            "Tyler Mark",
            "Senior Automation Specialist",
            "busy",
            "Free in 45 mins (at 11:30 AM)",
            listOf("PLC Programming", "Robotic Arms")
        ),
        MobileExpertModel(
            "4",
            "Johen Mark",
            "Hydraulic Systems Supervisor",
            "busy",
            "Free at 2:00 PM today",
            listOf("High-Pressure Pumps", "Fluid Dynamics")
        )
    )

    LaunchedEffect(Unit) {
        repository.getMachines().onSuccess {
            machines = it
            if (it.isNotEmpty()) {
                targetMachineId = it[0].id
            }
        }
    }

    Scaffold(
        topBar = { VTTopBar(title = "Call Experts") },
        containerColor = VTBackground,
        bottomBar = { VTBottomNav(selected = VTTab.CallExperts, onSelect = onTabSelected) }
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
                "Human Escalation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = VTOnSurface
            )
            Text(
                "Page senior engineers directly to your location or schedule a manual diagnostic inspection.",
                fontSize = 13.sp,
                color = VTOnSurfaceVariant
            )

            if (successMsg != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = successMsg!!,
                        fontSize = 13.sp,
                        color = Color(0xFF065F46),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Available section
            Text("Available Now (Free)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
            experts.filter { it.status == "available" }.forEach { expert ->
                VTCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(VTPrimaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    expert.name.split(" ").map { it.take(1) }.joinToString(""),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = VTOnPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(expert.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(expert.role, fontSize = 12.sp, color = VTOutline)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            expert.specialties.forEach { spec ->
                                Box(
                                    modifier = Modifier
                                        .background(VTPrimaryContainer.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(spec, fontSize = 9.sp, color = VTOnPrimaryContainer, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                selectedExpert = expert
                                isModalOpen = true
                            },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Dispatch to My Site", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }

            // Busy section
            Text("Busy (Available Later)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
            experts.filter { it.status == "busy" }.forEach { expert ->
                VTCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(VTSurfaceContainerHighest, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    expert.name.split(" ").map { it.take(1) }.joinToString(""),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = VTOnSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(expert.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(expert.role, fontSize = 12.sp, color = VTOutline)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = expert.nextFreeTime ?: "",
                            fontSize = 11.sp,
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                selectedExpert = expert
                                isModalOpen = true
                            },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VTPrimary)
                        ) {
                            Text("Schedule Appointment", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (isModalOpen && selectedExpert != null) {
        Dialog(onDismissRequest = { isModalOpen = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedExpert!!.status == "available") "Page Expert" else "Schedule Appointment",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { isModalOpen = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text("Expert: ${selectedExpert!!.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VTPrimary)

                    Column {
                        Text("Target Machine", fontSize = 11.sp, color = VTOutline, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        // Dropdown selection (Simplified for mobile preview)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(VTBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, VTOutlineVariant, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = machines.find { it.id == targetMachineId }?.name ?: "Select Machine",
                                fontSize = 13.sp
                            )
                        }
                    }

                    Column {
                        Text("Urgency Level", fontSize = 11.sp, color = VTOutline, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Low", "Medium", "High").forEach { level ->
                                val active = level == urgency
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) VTPrimaryContainer else VTBackground)
                                        .border(1.dp, if (active) VTPrimary else VTOutlineVariant, RoundedCornerShape(6.dp))
                                        .clickable { urgency = level }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        level,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) VTOnPrimaryContainer else VTOnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Column {
                        Text("Describe Issues", fontSize = 11.sp, color = VTOutline, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("Details for the specialist...", fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            maxLines = 3
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isModalOpen = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                if (selectedExpert!!.status == "available") {
                                    successMsg = "Dispatch Alert! ${selectedExpert!!.name} has been paged to manual diagnostic zone."
                                } else {
                                    successMsg = "Appointment request logged! ${selectedExpert!!.name} is scheduled for follow-up."
                                }
                                isModalOpen = false
                                notes = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedExpert!!.status == "available") Color(0xFF10B981) else VTPrimary
                            )
                        ) {
                            Text("Submit", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
