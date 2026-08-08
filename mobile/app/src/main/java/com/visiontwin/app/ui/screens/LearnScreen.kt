package com.visiontwin.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visiontwin.app.data.model.LearnMessageDto
import com.visiontwin.app.data.model.MachineDto
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.data.api.RetrofitClient
import com.visiontwin.app.ui.components.*
import com.visiontwin.app.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    repository: VisionTwinRepository,
    onTabSelected: (VTTab) -> Unit
) {
    var machines by remember { mutableStateOf<List<MachineDto>>(emptyList()) }
    var selectedMachine by remember { mutableStateOf<MachineDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(RetrofitClient.urlVersion.value) {
        isLoading = true
        repository.getMachines().onSuccess { machines = it }
        isLoading = false
    }

    if (selectedMachine == null) {
        Scaffold(
            topBar = { VTTopBar(title = "AI Interactive Learning") },
            containerColor = VTBackground,
            bottomBar = { VTBottomNav(selected = VTTab.Learn, onSelect = onTabSelected) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    "Select a Machine Twin",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = VTOnSurface
                )
                Text(
                    "Choose an industrial system to access its manual twin and start interactive learning.",
                    fontSize = 13.sp,
                    color = VTOnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VTPrimary)
                    }
                } else if (machines.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No machine twins registered yet.", color = VTOnSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(machines) { machine ->
                            VTCard(onClick = { selectedMachine = machine }) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(machine.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("${machine.manufacturer} • ${machine.model}", fontSize = 12.sp, color = VTOutline)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Launch Study Guide", fontSize = 12.sp, color = VTPrimary, fontWeight = FontWeight.Bold)
                                        Icon(Icons.Filled.Book, contentDescription = null, tint = VTPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        ActiveLearnWorkspace(
            machine = selectedMachine!!,
            repository = repository,
            onBack = { selectedMachine = null },
            onTabSelected = onTabSelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveLearnWorkspace(
    machine: MachineDto,
    repository: VisionTwinRepository,
    onBack: () -> Unit,
    onTabSelected: (VTTab) -> Unit
) {
    var activeSubTab by remember { mutableStateOf("chat") } // "chat" or "manual"
    var messages by remember { mutableStateOf<List<LearnMessageDto>>(emptyList()) }
    var sessionId by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var inputText by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var models by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedModel by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    fun loadHistory() {
        scope.launch {
            repository.getLearnHistory(machine.id, sessionId).onSuccess {
                messages = it
            }
        }
    }

    LaunchedEffect(machine.id, sessionId) {
        loadHistory()
        repository.getDiagnosisModels().onSuccess { res ->
            models = res.models
            selectedModel = res.models.find { it.contains("flash") } ?: res.default
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(machine.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Interactive Study Buddy", fontSize = 11.sp, color = VTOnSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VTSurface)
            )
        },
        containerColor = VTBackground,
        bottomBar = { VTBottomNav(selected = VTTab.Learn, onSelect = onTabSelected) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Sub-tabs
            TabRow(
                selectedTabIndex = if (activeSubTab == "chat") 0 else 1,
                containerColor = Color.White,
                contentColor = VTPrimary
            ) {
                Tab(
                    selected = activeSubTab == "chat",
                    onClick = { activeSubTab = "chat" },
                    text = { Text("Study Guide Chat", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == "manual",
                    onClick = { activeSubTab = "manual" },
                    text = { Text("Manual Twin", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
            }

            if (activeSubTab == "chat") {
                // AI Chat Companion
                Column(modifier = Modifier.weight(1f)) {
                    // Model Picker Row
                    if (models.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Reasoner Model: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VTOutline)
                            Box(modifier = Modifier.weight(1f)) {
                                Text(selectedModel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VTPrimary)
                            }
                        }
                        Divider(color = VTOutlineVariant.copy(alpha = 0.3f))
                    }

                    // Chat messages list
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(messages) { msg ->
                            val isUser = msg.sender == "USER"
                            val bubbleColor = if (isUser) VTPrimary else VTSurfaceContainerHighest
                            val textColor = if (isUser) Color.White else VTOnSurface
                            val alignment = if (isUser) Alignment.End else Alignment.Start

                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (isUser) 12.dp else 0.dp,
                                                bottomEnd = if (isUser) 0.dp else 12.dp
                                            )
                                        )
                                        .background(bubbleColor)
                                        .padding(12.dp)
                                ) {
                                    if (isUser) {
                                        Text(
                                            text = msg.messageText,
                                            color = textColor,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    } else {
                                        MarkdownText(
                                            markdown = msg.messageText,
                                            textColor = textColor
                                        )
                                    }
                                }
                                Text(
                                    text = msg.timestamp?.takeLast(11)?.take(5) ?: "",
                                    fontSize = 9.sp,
                                    color = VTOutline,
                                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                                )
                            }
                        }
                    }

                    // Input row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Ask study buddy...", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = VTBackground,
                                unfocusedContainerColor = VTBackground,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank() && !sending) {
                                    sending = true
                                    val userText = inputText.trim()
                                    inputText = ""
                                    scope.launch {
                                        repository.sendLearnMessage(
                                            machineId = machine.id,
                                            message = userText,
                                            sessionId = sessionId,
                                            model = selectedModel.takeIf { it.isNotBlank() }
                                        ).onSuccess {
                                            loadHistory()
                                        }
                                        sending = false
                                    }
                                }
                            },
                            enabled = inputText.isNotBlank() && !sending,
                            modifier = Modifier
                                .background(VTPrimary, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            } else {
                // Manual Twin document viewer
                var selectedDoc by remember { mutableStateOf("manual") } // "manual" or "guide"
                val manualPath = machine.manualPdfPath
                val userGuidePath = machine.userGuidePdfPath

                var docContent by remember { mutableStateOf<String?>(null) }
                var docLoading by remember { mutableStateOf(false) }
                var docError by remember { mutableStateOf<String?>(null) }

                val currentPath = if (selectedDoc == "manual") manualPath else userGuidePath

                LaunchedEffect(selectedDoc, currentPath) {
                    if (currentPath.isNullOrBlank()) {
                        docContent = null
                        docError = null
                        return@LaunchedEffect
                    }
                    docLoading = true
                    docError = null
                    repository.getFileContent(currentPath)
                        .onSuccess {
                            docContent = it
                            docLoading = false
                        }
                        .onFailure {
                            docError = "Failed to load document: ${it.message}"
                            docLoading = false
                        }
                }

                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // Document selector tabs if both are available, or simple label
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedDoc = "manual" },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedDoc == "manual") VTPrimaryContainer.copy(alpha = 0.1f) else Color.Transparent,
                                contentColor = if (selectedDoc == "manual") VTPrimary else VTOnSurfaceVariant
                            ),
                            border = BorderStroke(1.dp, if (selectedDoc == "manual") VTPrimary else VTOutlineVariant)
                        ) {
                            Text("Manual", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VTOnSurface)
                        }
                        OutlinedButton(
                            onClick = { selectedDoc = "guide" },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedDoc == "guide") VTPrimaryContainer.copy(alpha = 0.1f) else Color.Transparent,
                                contentColor = if (selectedDoc == "guide") VTPrimary else VTOnSurfaceVariant
                            ),
                            border = BorderStroke(1.dp, if (selectedDoc == "guide") VTPrimary else VTOutlineVariant)
                        ) {
                            Text("User Guide", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VTOnSurface)
                        }
                    }

                    Divider(color = VTOutlineVariant.copy(alpha = 0.4f), modifier = Modifier.padding(bottom = 12.dp))

                    if (currentPath.isNullOrBlank()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (selectedDoc == "manual") "No manual uploaded for this machine." else "No user guide uploaded for this machine.",
                                color = VTOutline,
                                fontSize = 13.sp
                            )
                        }
                    } else if (docLoading) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = VTPrimary)
                        }
                    } else if (docError != null) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(docError!!, color = VTError, fontSize = 13.sp)
                        }
                    } else {
                        // Display the markdown text!
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            MarkdownText(markdown = docContent ?: "")
                        }
                    }
                }
            }
        }
    }
}
