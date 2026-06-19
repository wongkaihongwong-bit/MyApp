package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import kotlinx.coroutines.launch
import com.example.data.LogEntry
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
fun CompanionApp(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val visitorName by viewModel.visitorName.collectAsState()
    val greetingStyle by viewModel.greetingStyle.collectAsState()
    val logs by viewModel.logsState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val chatError by viewModel.chatError.collectAsState()
    val chatSessions by viewModel.chatSessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val routingStatus by viewModel.routingStatus.collectAsState()
    val selectedVersionName by viewModel.selectedVersionName.collectAsState()
    val aiVersions = viewModel.aiVersions

    // Live telemetry states
    val liveTime by viewModel.liveTime.collectAsState()
    val liveMemory by viewModel.liveMemory.collectAsState()
    val liveFps by viewModel.liveFps.collectAsState()
    val liveCount by viewModel.liveDatabaseCount.collectAsState()
    val sharedConversation by viewModel.sharedConversation.collectAsState()

    // Map style names to aesthetic color themes
    val themePalette = when (greetingStyle) {
        "Cyberpunk Sunset" -> ThemePalette(
            accent = Color(0xFFFF5252),
            primary = Color(0xFFFF7043),
            secondary = Color(0xFFFFB74D),
            bgColor = Color(0xFF1E1415)
        )
        "Mint Minimal" -> ThemePalette(
            accent = Color(0xFF00E676),
            primary = Color(0xFF26A69A),
            secondary = Color(0xFF80CBC4),
            bgColor = Color(0xFF111716)
        )
        "Aurora Gold" -> ThemePalette(
            accent = Color(0xFFFFD700),
            primary = Color(0xFF00B0FF),
            secondary = Color(0xFFB2FF59),
            bgColor = Color(0xFF12151B)
        )
        "Nebula Violet" -> ThemePalette(
            accent = Color(0xFFE040FB),
            primary = Color(0xFF9C27B0),
            secondary = Color(0xFFCE93D8),
            bgColor = Color(0xFF140D1F)
        )
        "Solar Flare" -> ThemePalette(
            accent = Color(0xFFFF9100),
            primary = Color(0xFFFF6D00),
            secondary = Color(0xFFFFCC80),
            bgColor = Color(0xFF1B1411)
        )
        else -> ThemePalette( // Cosmic Slate
            accent = Color(0xFFC084FC),
            primary = Color(0xFF818CF8),
            secondary = Color(0xFFF472B6),
            bgColor = Color(0xFF0F172A)
        )
    }

    if (sharedConversation != null) {
        SharedConversationViewer(
            sharedConversation = sharedConversation!!,
            themePalette = themePalette,
            onClose = { viewModel.clearSharedConversation() }
        )
        return
    }

    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    androidx.compose.material3.ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            androidx.compose.material3.ModalDrawerSheet(
                drawerContainerColor = themePalette.bgColor,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier
                    .width(320.dp)
                    .border(
                        width = 1.dp,
                        color = themePalette.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                    )
            ) {
                SidebarContent(
                    chatSessions = chatSessions,
                    currentSessionId = currentSessionId,
                    onSelectSession = { viewModel.selectSession(it) },
                    onDeleteSession = { viewModel.deleteSession(it) },
                    onCreateNewChat = { viewModel.createNewChat() },
                    onClearHistory = { viewModel.clearChatHistory() },
                    themePalette = themePalette,
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(themePalette.bgColor),
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("open_sidebar_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Chat History Sidebar",
                            tint = themePalette.accent
                        )
                    }

                    Text(
                        text = "STUDIO WORKSPACE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    // Active indicator dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                    )
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 32.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Title & Info
                item {
                    HeaderSection(
                        liveTime = liveTime,
                        liveMemory = liveMemory,
                        liveFps = liveFps,
                        activeLogsCount = liveCount,
                        themePalette = themePalette
                    )
                }

                // AI Chat Companion Hub
                item {
                    AiChatCompanionHub(
                        chatMessages = chatMessages,
                        isChatLoading = isChatLoading,
                        chatError = chatError,
                        routingStatus = routingStatus,
                        onSendMessage = { viewModel.sendMessage(it) },
                        onOpenSidebar = {
                            scope.launch { drawerState.open() }
                        },
                        onCreateNewChat = { viewModel.createNewChat() },
                        themePalette = themePalette,
                        selectedVersionName = selectedVersionName,
                        aiVersions = aiVersions,
                        onVersionSelected = { viewModel.selectModelVersion(it) },
                        onShareMessage = { prompt, response, model, onSuccess ->
                            viewModel.shareConversation(prompt, response, model, onSuccess)
                        }
                    )
                }

                // Interactive Greeting Card Preview Generator
                item {
                    GreetingCardGenerator(
                        visitorName = visitorName,
                        selectedStyle = greetingStyle,
                        onNameChanged = { viewModel.setVisitorName(it) },
                        onStyleSelected = { viewModel.setGreetingStyle(it) },
                        themePalette = themePalette
                    )
                }

                // Local Database Log Room Form & List Section
                item {
                    DatabaseLogBookSection(
                        logs = logs,
                        viewModel = viewModel,
                        themePalette = themePalette
                    )
                }

                // Developer Playbook / Info Card
                item {
                    DevPlaybookCard(themePalette = themePalette)
                }
            }
        }
    }
}

// Custom theme configuration helper
data class ThemePalette(
    val accent: Color,
    val primary: Color,
    val secondary: Color,
    val bgColor: Color
)

@Composable
fun HeaderSection(
    liveTime: String,
    liveMemory: String,
    liveFps: Float,
    activeLogsCount: Int,
    themePalette: ThemePalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AI Studio Companion",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Active Workspace Playground",
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 2.sp,
                color = themePalette.primary.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Diagnostic Live Metrics Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetricPill(
                label = "TIME",
                value = if (liveTime.isEmpty()) "SYNCHRONIZING" else liveTime,
                accentColor = themePalette.secondary,
                modifier = Modifier.weight(1.2f)
            )
            MetricPill(
                label = "HEAP",
                value = liveMemory,
                accentColor = themePalette.primary,
                modifier = Modifier.weight(0.8f)
            )
            MetricPill(
                label = "FPS",
                value = "$liveFps T",
                accentColor = themePalette.accent,
                modifier = Modifier.weight(0.7f)
            )
            MetricPill(
                label = "DB ROWS",
                value = "$activeLogsCount",
                accentColor = Color.White,
                modifier = Modifier.weight(0.7f)
            )
        }
    }
}

@Composable
fun MetricPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f)
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontFamily = FontFamily.Monospace
                ),
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GreetingCardGenerator(
    visitorName: String,
    selectedStyle: String,
    onNameChanged: (String) -> Unit,
    onStyleSelected: (String) -> Unit,
    themePalette: ThemePalette
) {
    val styles = listOf("Cosmic Slate", "Cyberpunk Sunset", "Mint Minimal", "Aurora Gold", "Nebula Violet", "Solar Flare")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.02f)
        ),
        border = borderStrokeHelper(themePalette.accent.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "GREETING GENERATOR",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Text Input field
            OutlinedTextField(
                value = visitorName,
                onValueChange = onNameChanged,
                placeholder = { Text("Enter your name...", color = Color.White.copy(alpha = 0.35f)) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("visitor_name_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themePalette.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedContainerColor = Color.Black.copy(alpha = 0.20f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.10f)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Theme selectors
            Text(
                text = "Select Visuality Style Preset",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                styles.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        pair.forEach { style ->
                            val isSelected = style == selectedStyle
                            StyleChip(
                                name = style,
                                isSelected = isSelected,
                                themePalette = themePalette,
                                onSelected = { onStyleSelected(style) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic card canvas badge output
            Text(
                text = "REAL-TIME CREATIVE OUTPUT",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Visual Card Preview
            WelcomeCardBadge(
                visitorName = visitorName,
                selectedStyle = selectedStyle,
                themePalette = themePalette
            )
        }
    }
}

@Composable
fun StyleChip(
    name: String,
    isSelected: Boolean,
    themePalette: ThemePalette,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val outlineColor by animateColorAsState(
        targetValue = if (isSelected) themePalette.primary else Color.White.copy(alpha = 0.1f),
        label = "outline"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) themePalette.primary.copy(alpha = 0.15f) else Color.Transparent,
        label = "bg"
    )

    Box(
        modifier = modifier
            .testTag("style_chip_$name")
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, outlineColor, RoundedCornerShape(10.dp))
            .clickable { onSelected() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = themePalette.accent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WelcomeCardBadge(
    visitorName: String,
    selectedStyle: String,
    themePalette: ThemePalette
) {
    val displayName = if (visitorName.trim().isEmpty()) "VALUED VISITOR" else visitorName.trim()
    val strokeColor = themePalette.primary.copy(alpha = 0.4f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(RoundedCornerShape(16.dp))
            .drawBehind {
                val brush = Brush.linearGradient(
                    colors = listOf(
                        themePalette.bgColor,
                        themePalette.primary.copy(alpha = 0.35f),
                        themePalette.accent.copy(alpha = 0.2f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
                drawRect(brush = brush)
            }
            .border(1.5.dp, themePalette.primary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Decorative background geometric overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = themePalette.accent.copy(alpha = 0.15f),
                radius = 112f,
                center = Offset(size.width * 0.9f, size.height * 0.2f)
            )

            // Dynamic grid lines
            for (i in 0..12) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(i * 50f, 0f),
                    end = Offset(i * 50f, size.height),
                    strokeWidth = 1f
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(themePalette.accent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "VISITOR PASS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                Text(
                    text = selectedStyle.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = themePalette.accent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            Column(modifier = Modifier.weight(1f, fill = true), verticalArrangement = Arrangement.Center) {
                val nameToRender = displayName
                Text(
                    text = nameToRender,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp,
                        fontSize = 18.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = "Welcome to the AI Studio builder workspace! Tap elements below.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Normal
                    ),
                    maxLines = 2
                )
            }

            // Pseudo credential code display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: #${String.format("%04d", (displayName.hashCode() % 10000).coerceAtLeast(0))}-X",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White.copy(alpha = 0.4f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                )

                Row(
                    modifier = Modifier.height(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val codePattern = listOf(2, 4, 1, 3, 5, 2, 4, 1)
                    codePattern.forEach { barScale ->
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height((barScale * 2).dp)
                                .background(themePalette.accent.copy(alpha = 0.6f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DatabaseLogBookSection(
    logs: List<LogEntry>,
    viewModel: CompanionViewModel,
    themePalette: ThemePalette
) {
    var isFormExpanded by remember { mutableStateOf(false) }

    val newTitle by viewModel.formTitle.collectAsState()
    val newContent by viewModel.formContent.collectAsState()
    val selectedCategory by viewModel.formCategory.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.02f)
        ),
        border = borderStrokeHelper(themePalette.primary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SQLITE LOCAL REGISTRY",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    Text(
                        text = "Powered by Android Room architecture",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp
                        )
                    )
                }

                Row {
                    IconButton(
                        onClick = { isFormExpanded = !isFormExpanded },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("expand_log_form_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Expand note creator",
                            tint = themePalette.accent
                        )
                    }

                    if (logs.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearAllLogs() },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("clear_logs_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Clear all database rows",
                                tint = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expandable form logic
            AnimatedVisibility(
                visible = isFormExpanded,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -20 }),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "NEW DATABASE WORK LOG",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = themePalette.primary,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { viewModel.formTitle.value = it },
                        placeholder = { Text("Log Title (e.g. Test Run completed)", color = Color.White.copy(alpha = 0.35f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("log_title_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themePalette.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
                            focusedContainerColor = Color.Black.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newContent,
                        onValueChange = { viewModel.formContent.value = it },
                        placeholder = { Text("Log description content...", color = Color.White.copy(alpha = 0.35f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("log_content_input"),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themePalette.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
                            focusedContainerColor = Color.Black.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category picker
                    Text(
                        text = "Category classification:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val categories = listOf("Playground", "Note", "Benchmark")
                        categories.forEach { category ->
                            val isSel = selectedCategory == category
                            val categoryColor = when (category) {
                                "Benchmark" -> themePalette.accent
                                "Note" -> themePalette.secondary
                                else -> themePalette.primary
                            }

                            Box(
                                modifier = Modifier
                                    .testTag("category_chip_$category")
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) categoryColor.copy(alpha = 0.20f) else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isSel) categoryColor else Color.White.copy(alpha = 0.10f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.formCategory.value = category }
                                    .padding(vertical = 6.dp, horizontal = 12.dp)
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.addLogEntry()
                            isFormExpanded = false
                        },
                        enabled = newTitle.isNotBlank() && newContent.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themePalette.primary,
                            disabledContainerColor = Color.White.copy(alpha = 0.08f),
                            contentColor = Color.Black,
                            disabledContentColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("add_log_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Save Note",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COMMIT TO SQLITE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            // Logs feed
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No data",
                            tint = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No log database records found",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.35f),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    logs.forEach { log ->
                        val itemColor = when (log.category) {
                            "Benchmark" -> themePalette.accent
                            "Note" -> themePalette.secondary
                            "System" -> Color(0xFF90A4AE)
                            else -> themePalette.primary
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Classification dot
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(itemColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = log.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = itemColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = log.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.dp.valSp()
                                    )
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = log.content,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            // Don't leak delete button for static systems default if user shouldn't remove it, or allow removing anything
                            IconButton(
                                onClick = { viewModel.deleteLogEntry(log.id) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("delete_log_${log.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete entry",
                                    tint = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DevPlaybookCard(themePalette: ThemePalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.01f)
        ),
        border = borderStrokeHelper(Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "COMMUNICATION LOG",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "The AI Studio environment is completely operational. Any future feature prompts, local database modules, image requests, or Gemini integrations you request are instantly compiled and rendered straight to the workspace streaming preview card. Code with freedom!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 20.sp,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "STATUS: LISTENING FOR COMMANDS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676),
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }
    }
}

// Extension function to help scale dp sizing comfortably into sp for modern responsive scaling
@Composable
private fun dpToSp(dp: androidx.compose.ui.unit.Dp) = with(androidx.compose.ui.platform.LocalDensity.current) { dp.toSp() }

@Composable
private fun androidx.compose.ui.unit.Dp.valSp(): androidx.compose.ui.unit.TextUnit {
    return dpToSp(this)
}

@Composable
private fun borderStrokeHelper(color: Color) = androidx.compose.foundation.BorderStroke(0.75.dp, color)

@Composable
fun SidebarContent(
    chatSessions: List<com.example.data.ChatSession>,
    currentSessionId: String?,
    onSelectSession: (String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onCreateNewChat: () -> Unit,
    onClearHistory: () -> Unit,
    themePalette: ThemePalette,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CHAT DIRECTORY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "SQLite Persistent Logs",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = themePalette.accent,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            IconButton(
                onClick = onCloseDrawer,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Sidebar",
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Start fresh session button
        Button(
            onClick = {
                onCreateNewChat()
                onCloseDrawer()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = themePalette.primary,
                contentColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("sidebar_new_chat_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Chat",
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "START FRESH CHAT",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Stats
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "Active Conversations: ${chatSessions.size}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "Engine: gemini-3.5-flash",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = themePalette.primary,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // History listing
        Text(
            text = "PERSISTED SESSIONS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 1.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (chatSessions.isEmpty()) {
                item {
                    Text(
                        text = "History empty. Start a new session to begin.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.3f),
                            fontFamily = FontFamily.SansSerif
                        ),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(chatSessions) { session ->
                    val isActive = session.id == currentSessionId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isActive) Color.White.copy(alpha = 0.12f)
                                else Color.White.copy(alpha = 0.03f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isActive) themePalette.primary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onSelectSession(session.id)
                                onCloseDrawer()
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) themePalette.accent else Color.White.copy(alpha = 0.4f))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = session.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onDeleteSession(session.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Session",
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clear History
        Button(
            onClick = {
                onClearHistory()
                onCloseDrawer()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red.copy(alpha = 0.1f),
                contentColor = Color.Red
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Clear Chat All",
                tint = Color.Red,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("WIPE ALL HISTORY", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun AiChatCompanionHub(
    chatMessages: List<com.example.data.ChatMessage>,
    isChatLoading: Boolean,
    chatError: String?,
    routingStatus: String,
    onSendMessage: (String) -> Unit,
    onOpenSidebar: () -> Unit,
    onCreateNewChat: () -> Unit,
    themePalette: ThemePalette,
    selectedVersionName: String,
    aiVersions: List<AiVersion>,
    onVersionSelected: (String) -> Unit,
    onShareMessage: (String, String, String, (String) -> Unit) -> Unit
) {
    var chatText by remember { mutableStateOf("") }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    val context = LocalContext.current
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var ttsEnabled by remember { mutableStateOf(false) }
    var lastSpokenMessageId by remember { mutableStateOf<Long?>(null) }

    // Initialize TextToSpeech with proper lifecycle management
    DisposableEffect(context) {
        val instance = TextToSpeech(context) { _ -> }
        instance.language = Locale.getDefault()
        tts = instance
        onDispose {
            instance.stop()
            instance.shutdown()
        }
    }

    // Monitor new messages and speak latest response if TTS is enabled
    LaunchedEffect(chatMessages, ttsEnabled) {
        if (ttsEnabled && chatMessages.isNotEmpty()) {
            val lastMsg = chatMessages.last()
            if (lastMsg.role == "model" && lastMsg.timestamp != lastSpokenMessageId) {
                lastSpokenMessageId = lastMsg.timestamp
                tts?.speak(lastMsg.content, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    // Force stop speaking when voice toggled off
    LaunchedEffect(ttsEnabled) {
        if (!ttsEnabled) {
            tts?.stop()
        }
    }

    // Speech-to-Text launcher
    val speechRecognizeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                chatText = spokenText
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.02f)
        ),
        border = borderStrokeHelper(themePalette.accent.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(themePalette.accent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "AI CHAT COMPANION HUB",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isChatLoading) Color.Yellow else Color(0xFF00E676))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Dual Brain: $routingStatus",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Start New Chat Button in Main Card
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(themePalette.accent.copy(alpha = 0.1f))
                            .border(0.5.dp, themePalette.accent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .clickable { onCreateNewChat() }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Start New Chat",
                            tint = themePalette.accent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "NEW CHAT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = themePalette.accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { ttsEnabled = !ttsEnabled },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (ttsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Text-to-Speech",
                            tint = if (ttsEnabled) themePalette.accent else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onOpenSidebar,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Show Chat Database Sidebar",
                            tint = themePalette.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Model Marketplace Selector
            var menuExpanded by remember { mutableStateOf(false) }
            val activeVersion = aiVersions.find { it.name == selectedVersionName } ?: (aiVersions.firstOrNull() ?: aiVersions[0])

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .clickable { menuExpanded = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "SWITCH AI VERSION (MODEL MARKETPLACE)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = themePalette.accent,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeVersion.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeVersion.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Icon(
                            imageVector = if (menuExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown Menu",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color(0xFF1E1E24))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                ) {
                    aiVersions.forEach { version ->
                        DropdownMenuItem(
                            text = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (version.name == selectedVersionName) themePalette.accent else Color.White.copy(alpha = 0.2f))
                                        )
                                        Text(
                                            text = version.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (version.name == selectedVersionName) themePalette.accent else Color.White
                                            )
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (version.modelId.contains("pro")) Color(0x33FF9100) else Color(0x3300E676))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (version.modelId.contains("pro")) "PRO 3.1" else "FLASH 3.5",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (version.modelId.contains("pro")) Color(0xFFFF9100) else Color(0xFF00E676),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = version.description,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            },
                            onClick = {
                                onVersionSelected(version.name)
                                menuExpanded = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (version.name == selectedVersionName) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable speech list
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.15f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

                // Scroll to end whenever size changes
                androidx.compose.runtime.LaunchedEffect(chatMessages.size) {
                    if (chatMessages.isNotEmpty()) {
                        lazyListState.animateScrollToItem(chatMessages.size - 1)
                    }
                }

                androidx.compose.foundation.lazy.LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatMessages) { msg ->
                        val isUser = msg.role == "user"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 240.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isUser) 12.dp else 2.dp,
                                            bottomEnd = if (isUser) 2.dp else 12.dp
                                        )
                                    )
                                    .background(
                                        if (isUser) {
                                            Brush.linearGradient(
                                                colors = listOf(themePalette.primary, themePalette.accent)
                                            )
                                        } else {
                                            Brush.linearGradient(
                                                colors = listOf(Color.White.copy(alpha = 0.04f), Color.White.copy(alpha = 0.08f))
                                            )
                                        }
                                    )
                                    .border(
                                        width = 0.5.dp,
                                        color = if (isUser) Color.Transparent else Color.White.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isUser) 12.dp else 2.dp,
                                            bottomEnd = if (isUser) 2.dp else 12.dp
                                        )
                                    )
                                    .padding(10.dp)
                             ) {
                                Text(
                                    text = msg.content,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            if (!isUser && msg.modelUsed != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp, end = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val isPro = msg.modelUsed.contains("Pro")
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(if (isPro) Color(0xFFA855F7) else themePalette.accent)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = msg.modelUsed,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = (if (isPro) Color(0xFFC084FC) else themePalette.accent).copy(alpha = 0.8f),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }

                                    // Action buttons row: Copy, Like, Share
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        var isLiked by remember { mutableStateOf(false) }
                                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                        val context = androidx.compose.ui.platform.LocalContext.current

                                        // Copy
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(msg.content))
                                                android.widget.Toast.makeText(context, "Copied response to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy Response",
                                                tint = Color.White.copy(alpha = 0.5f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }

                                        // Like
                                        IconButton(
                                            onClick = { isLiked = !isLiked },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Like Response",
                                                tint = if (isLiked) Color(0xFFFF5252) else Color.White.copy(alpha = 0.5f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }

                                        // Share
                                        IconButton(
                                            onClick = {
                                                val msgIndex = chatMessages.indexOf(msg)
                                                val userPromptMsg = if (msgIndex > 0) chatMessages[msgIndex - 1] else null
                                                val promptText = userPromptMsg?.content ?: "Let's explore AI Studio"
                                                
                                                onShareMessage(
                                                    promptText,
                                                    msg.content,
                                                    msg.modelUsed ?: "Aero Core"
                                                ) { shareUrl ->
                                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(shareUrl))
                                                    android.widget.Toast.makeText(context, "Link copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share Link",
                                                tint = themePalette.accent,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isChatLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .padding(vertical = 8.dp, horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = "AI is thinking...",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = themePalette.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Error display
            if (chatError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Red.copy(alpha = 0.1f))
                        .border(0.5.dp, Color.Red.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = chatError,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFFF8A80),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Input field
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatText,
                    onValueChange = { chatText = it },
                    placeholder = { Text("Ask your AI Assistant...", color = Color.White.copy(alpha = 0.35f)) },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val voiceIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to your AI Companion...")
                                }
                                try {
                                    speechRecognizeLauncher.launch(voiceIntent)
                                } catch (e: Exception) {
                                    // Handled if speech recognition is not supported
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = themePalette.accent
                            )
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_prompt_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themePalette.primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = Color.Black.copy(alpha = 0.25f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.12f)
                    )
                )

                Button(
                    onClick = {
                        if (chatText.isNotBlank()) {
                            onSendMessage(chatText)
                            chatText = ""
                            focusManager.clearFocus()
                        }
                    },
                    enabled = chatText.isNotBlank() && !isChatLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themePalette.primary,
                        disabledContainerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = Color.Black,
                        disabledContentColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("send_ai_chat_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SEND", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun SharedConversationViewer(
    sharedConversation: com.example.data.SharedConversation,
    themePalette: ThemePalette,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themePalette.bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AIC COMPANION PORTAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = themePalette.accent,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Secure Shared Thread (Read-Only)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }

                // Close Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close View",
                        tint = Color.White
                    )
                }
            }

            // Shared messages cards inside scrollable container
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Author Metadata Label
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(themePalette.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Shared by: ${sharedConversation.authorLabel ?: "Anonymous Explorer"}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(themePalette.accent.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LIVE DATA",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = themePalette.accent,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                items(sharedConversation.messages) { msg ->
                    val isUser = msg.role == "user"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        // Sender label
                        Text(
                            text = if (isUser) "PROMPT" else (msg.modelUsed ?: "COMPANION ANSWER"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) themePalette.primary.copy(alpha = 0.6f) else themePalette.accent.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(bottom = 4.dp, start = if (isUser) 0.dp else 4.dp, end = if (isUser) 4.dp else 0.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isUser) {
                                        Brush.linearGradient(
                                            colors = listOf(themePalette.primary, themePalette.accent)
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(Color.White.copy(alpha = 0.04f), Color.White.copy(alpha = 0.08f))
                                        )
                                    }
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = if (isUser) Color.Transparent else Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = msg.content,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Button - Exit viewer
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themePalette.primary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "CLOSE VIEW & BACK TO WORKSPACE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}
