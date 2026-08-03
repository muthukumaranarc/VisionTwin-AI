package com.visiontwin.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.visiontwin.app.data.api.RetrofitClient
import com.visiontwin.app.data.model.MachineDto
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.components.*
import com.visiontwin.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MachineListScreen(
    repository: VisionTwinRepository,
    onTabSelected: (VTTab) -> Unit,
    onMachineSelected: (String) -> Unit,
    onMachineLongSelected: (String, String) -> Unit
) {
    var machines by remember { mutableStateOf<List<MachineDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadMachines() {
        scope.launch {
            isLoading = true
            errorMsg = null
            repository.getMachines().onSuccess { machines = it }.onFailure { errorMsg = it.message }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadMachines() }

    Scaffold(
        topBar = {
            VTTopBar(
                title = "VisionTwin AI",
                actions = {
                    IconButton(onClick = { loadMachines() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        containerColor = VTBackground,
        bottomBar = { VTBottomNav(selected = VTTab.Machines, onSelect = onTabSelected) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = VTPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading machines...", color = VTOnSurfaceVariant)
                    }
                }
                errorMsg != null && machines.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Could not load machines", color = VTError, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { loadMachines() }) {
                            Text("Retry", color = VTPrimary)
                        }
                    }
                }
                machines.isEmpty() -> {
                    VTEmptyState(
                        text = "No machines available",
                        subtitle = "Tap Profile → Add Machine to register your first loom."
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(machines) { machine ->
                            MachineCard(
                                machine = machine,
                                onClick = { onMachineSelected(machine.id) },
                                onLongClick = { onMachineLongSelected(machine.id, machine.name) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Long-press a machine to manage reference targets",
                                fontSize = 11.sp,
                                color = VTOutline,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MachineCard(machine: MachineDto, onClick: () -> Unit, onLongClick: (() -> Unit)? = null) {
    VTCard(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = RetrofitClient.fileUrl(machine.thumbnailPath),
                contentDescription = machine.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = machine.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = VTOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    VTLabelChip(text = "SYNCED")
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${machine.manufacturer} · ${machine.model}",
                    fontSize = 13.sp,
                    color = VTOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (machine.updatedAt != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "UPDATED ${machine.updatedAt.take(10)}",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        color = VTOutline
                    )
                }
            }
        }
    }
}
