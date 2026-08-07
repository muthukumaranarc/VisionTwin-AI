package com.visiontwin.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visiontwin.app.ui.theme.*

enum class VTTab(val label: String, val icon: ImageVector, val filledIcon: ImageVector, val route: String) {
    Dashboard("Dashboard", Icons.Filled.Dashboard, Icons.Filled.Dashboard, "dashboard"),
    Announcement("Broadcasts", Icons.Filled.Notifications, Icons.Filled.Notifications, "announcements"),
    Diagnose("AI Diagnose", Icons.Filled.Psychology, Icons.Filled.Psychology, "diagnose"),
    Learn("AI Learn", Icons.Filled.Book, Icons.Filled.Book, "learn"),
    CallExperts("Experts", Icons.Filled.People, Icons.Filled.People, "call-experts"),
}

/**
 * App bar matching the VisionTwin mobile design: surface background,
 * centered brand title on main tabs, back arrow + title on detail screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VTTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val backAction = onBack
    val showBack = backAction != null
    var isProfileOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    if (isProfileOpen) {
        ProfileDialog(onDismiss = { isProfileOpen = false })
    }
    if (isSettingsOpen) {
        SettingsDialog(onDismiss = { isSettingsOpen = false })
    }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (showBack) VTOnSurface else VTPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = { backAction!!() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (onBack == null) {
                // Settings button
                IconButton(onClick = { isSettingsOpen = true }) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = VTPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Profile avatar button
                IconButton(onClick = { isProfileOpen = true }) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(VTPrimaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "NY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = VTOnPrimaryContainer
                        )
                    }
                }
            } else {
                actions()
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = VTSurface,
            titleContentColor = VTPrimary,
            navigationIconContentColor = VTOnSurfaceVariant,
            actionIconContentColor = VTOnSurfaceVariant
        )
    )
}

@Composable
fun VTBottomNav(selected: VTTab, onSelect: (VTTab) -> Unit) {
    Surface(
        color = VTSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VTOutlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            VTTab.entries.forEach { tab ->
                val isActive = tab == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(
                            color = if (isActive) VTPrimaryContainer else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickableNoRipple { onSelect(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = if (isActive) tab.filledIcon else tab.icon,
                            contentDescription = tab.label,
                            tint = if (isActive) VTOnPrimaryContainer else VTOnSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        if (tab == VTTab.Diagnose || tab == VTTab.Learn) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 10.dp, y = (-4).dp)
                                    .background(VTError, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    "AI",
                                    fontSize = 7.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        fontSize = 10.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) VTOnPrimaryContainer else VTOnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.clickable(
            interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    )

/**
 * Content card per design: white surface, 1px border, soft shadow.
 */
@Composable
fun VTCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    val base = Modifier
        .fillMaxWidth()
        .border(1.dp, VTCardBorder, shape)

    if (onClick != null) {
        Card(
            modifier = base.then(modifier),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            onClick = onClick
        ) {
            Column(content = content)
        }
    } else {
        Card(
            modifier = base.then(modifier),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(content = content)
        }
    }
}

/**
 * Uppercase mono label chip (JetBrains Mono / label-caps token).
 */
@Composable
fun VTLabelChip(
    text: String,
    container: Color = VTPrimaryFixed,
    contentColor: Color = VTOnPrimaryFixedVariant
) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.6.sp,
        color = contentColor,
        maxLines = 1,
        modifier = Modifier
            .background(container, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

/**
 * Primary action button per design: full-width, 48dp, primary blue, soft shadow.
 */
@Composable
fun VTButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = VTPrimary,
    contentColor: Color = VTOnPrimary,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = VTSurfaceContainerHighest,
            disabledContentColor = VTOnSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        enabled = enabled
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = contentColor,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun VTSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = VTOnSurface,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

/**
 * Circular progress ring used in the "Detected Issue" card.
 */
@Composable
fun VTProgressRing(
    progress: Float,
    size: androidx.compose.ui.unit.Dp = 56.dp,
    trackColor: Color = VTSurfaceContainerHighest,
    progressColor: Color = VTError,
    label: String? = null
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 4.dp.toPx()
            val inset = stroke / 2
            val diameter = size.toPx() - stroke
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        if (label != null) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = VTOnSurface
            )
        }
    }
}

@Composable
fun VTEmptyState(text: String, subtitle: String? = null) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = VTOnSurface)
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 13.sp, color = VTOnSurfaceVariant)
        }
    }
}
