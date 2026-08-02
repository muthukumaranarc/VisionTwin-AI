package com.visiontwin.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import com.visiontwin.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MachineListScreen(
    repository: VisionTwinRepository,
    onMachineSelected: (String, String) -> Unit,
    onAdminLogin: () -> Unit
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
            TopAppBar(
                title = {
                    Text(
                        text = "Machines",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = { onAdminLogin() }
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { loadMachines() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = White,
                    actionIconContentColor = White
                )
            )
        },
        containerColor = ScreenBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading machines...", color = SecondaryText)
                    }
                }
                errorMsg != null && machines.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Could not load machines", color = ErrorRed, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { loadMachines() }) {
                            Text("Retry", color = PrimaryBlue)
                        }
                    }
                }
                machines.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No machines available", color = SecondaryText, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Admin can add machines via long-press on title", color = MediumGray, fontSize = 13.sp)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(machines) { machine ->
                            AnimatedVisibility(visible = true, enter = fadeIn()) {
                                MachineCard(machine = machine, onClick = {
                                    onMachineSelected(machine.id, machine.name)
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MachineCard(machine: MachineDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = RetrofitClient.fileUrl(machine.thumbnailPath),
                contentDescription = machine.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CardShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = machine.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = machine.manufacturer,
                    fontSize = 14.sp,
                    color = SecondaryText
                )
                Text(
                    text = "Model: ${machine.model}",
                    fontSize = 13.sp,
                    color = MediumGray
                )
                if (machine.updatedAt != null) {
                    Text(
                        text = "Updated: ${machine.updatedAt.take(10)}",
                        fontSize = 12.sp,
                        color = MediumGray
                    )
                }
            }
        }
    }
}
