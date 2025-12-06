package com.example.klarity.presentation.screen.home

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.theme.KlarityColors

/**
 * Sync status indicator
 */
enum class SyncStatus {
    SYNCED,
    SYNCING,
    OFFLINE,
    ERROR
}

/**
 * Theme options
 */
enum class ThemeMode {
    DARK,
    OLED,
    LIGHT
}

/**
 * Top Command Bar - Compact Global Navigation
 * 
 * A compact, unified bar containing:
 * - Left: App identifier (Klarity), Breadcrumbs  
 * - Center: Omnibar (Search + Commands)
 * - Right: Status indicators
 */
@Composable
fun TopCommandBar(
    currentPath: List<String> = listOf("Home"),
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onCommandPaletteOpen: () -> Unit = {},
    syncStatus: SyncStatus = SyncStatus.SYNCED,
    currentTheme: ThemeMode = ThemeMode.DARK,
    onThemeChange: (ThemeMode) -> Unit = {},
    aiModelName: String = "GPT-4",
    aiTemperature: Float = 0.7f,
    modifier: Modifier = Modifier
) {
    val luminousTeal = Color(0xFF1FDBC8)
    val electricMint = Color(0xFF3DD68C)
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = KlarityColors.BgSecondary.copy(alpha = 0.95f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Section - App identifier + Breadcrumbs
            LeftSection(
                currentPath = currentPath,
                luminousTeal = luminousTeal
            )
            
            // Center Section - Command Palette + Search
            CenterSection(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onCommandPaletteOpen = onCommandPaletteOpen,
                luminousTeal = luminousTeal
            )
            
            // Right Section - Status indicators + Controls
            RightSection(
                syncStatus = syncStatus,
                currentTheme = currentTheme,
                onThemeChange = onThemeChange,
                aiModelName = aiModelName,
                aiTemperature = aiTemperature,
                luminousTeal = luminousTeal,
                electricMint = electricMint
            )
        }
    }
}

@Composable
private fun LeftSection(
    currentPath: List<String>,
    luminousTeal: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App Logo - Klarity with soft glow
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            luminousTeal.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🍃",
                fontSize = 16.sp
            )
        }
        
        // Breadcrumbs
        Breadcrumbs(path = currentPath)
    }
}

@Composable
private fun Breadcrumbs(path: List<String>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        path.forEachIndexed { index, item ->
            if (index > 0) {
                Text(
                    text = "/",
                    color = KlarityColors.TextTertiary,
                    fontSize = 12.sp
                )
            }
            
            val interactionSource = remember { MutableInteractionSource() }
            val isHovered by interactionSource.collectIsHoveredAsState()
            
            Text(
                text = item,
                color = if (index == path.lastIndex) 
                    KlarityColors.TextPrimary 
                else 
                    KlarityColors.TextSecondary,
                fontSize = 13.sp,
                fontWeight = if (index == path.lastIndex) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier
                    .hoverable(interactionSource)
                    .clip(RoundedCornerShape(4.dp))
                    .then(
                        if (isHovered && index < path.lastIndex) {
                            Modifier.background(KlarityColors.BgElevated)
                        } else Modifier
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun CenterSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCommandPaletteOpen: () -> Unit,
    luminousTeal: Color
) {
    // Consolidated Omnibar - Single search/command input
    OmniBar(
        query = searchQuery,
        onQueryChange = onSearchQueryChange,
        onCommandPaletteOpen = onCommandPaletteOpen,
        luminousTeal = luminousTeal
    )
}

@Composable
private fun OmniBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onCommandPaletteOpen: () -> Unit,
    luminousTeal: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isFocused by remember { mutableStateOf(false) }
    
    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> KlarityColors.BgElevated
            isHovered -> KlarityColors.BgTertiary.copy(alpha = 0.9f)
            else -> KlarityColors.BgTertiary.copy(alpha = 0.7f)
        },
        animationSpec = tween(150),
        label = "bgColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> luminousTeal.copy(alpha = 0.5f)
            isHovered -> KlarityColors.BorderPrimary
            else -> Color.Transparent
        },
        animationSpec = tween(150),
        label = "borderColor"
    )
    
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(420.dp)
            .hoverable(interactionSource)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Clicking anywhere on bar opens command palette if not focused
                if (!isFocused && query.isEmpty()) {
                    onCommandPaletteOpen()
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = if (isFocused) luminousTeal else KlarityColors.TextTertiary,
                modifier = Modifier.size(18.dp)
            )
            
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = KlarityColors.TextPrimary,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(luminousTeal),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { isFocused = it.isFocused },
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search or type a command...",
                                color = KlarityColors.TextTertiary,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
            // Keyboard shortcut badge
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                KeyBadge("⌘")
                KeyBadge("K")
            }
        }
    }
}

@Composable
private fun KeyBadge(key: String) {
    Surface(
        color = KlarityColors.BgElevated,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = key,
            color = KlarityColors.TextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun RightSection(
    syncStatus: SyncStatus,
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    aiModelName: String,
    aiTemperature: Float,
    luminousTeal: Color,
    electricMint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // AI Model indicator
        AIModelIndicator(
            modelName = aiModelName,
            temperature = aiTemperature,
            electricMint = electricMint
        )
        
        // Sync status
        SyncStatusIndicator(
            status = syncStatus,
            luminousTeal = luminousTeal
        )
        
        // Theme toggle
        ThemeToggle(
            currentTheme = currentTheme,
            onThemeChange = onThemeChange
        )
        
        // Profile menu
        ProfileButton()
    }
}

@Composable
private fun AIModelIndicator(
    modelName: String,
    temperature: Float,
    electricMint: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Surface(
        color = if (isHovered) KlarityColors.BgElevated else Color.Transparent,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.hoverable(interactionSource)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // AI indicator dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(electricMint, CircleShape)
            )
            
            Text(
                text = modelName,
                color = KlarityColors.TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "T:${String.format("%.1f", temperature)}",
                color = KlarityColors.TextTertiary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun SyncStatusIndicator(
    status: SyncStatus,
    luminousTeal: Color
) {
    val statusColor = when (status) {
        SyncStatus.SYNCED -> luminousTeal
        SyncStatus.SYNCING -> Color(0xFFFFA500) // Orange
        SyncStatus.OFFLINE -> KlarityColors.TextTertiary
        SyncStatus.ERROR -> Color(0xFFFF4444) // Red
    }
    
    val statusIcon = when (status) {
        SyncStatus.SYNCED -> Icons.Default.Check
        SyncStatus.SYNCING -> Icons.Default.Refresh
        SyncStatus.OFFLINE -> Icons.Default.Close
        SyncStatus.ERROR -> Icons.Default.Warning
    }
    
    val scale by animateFloatAsState(
        targetValue = if (status == SyncStatus.SYNCING) 1.1f else 1f,
        animationSpec = tween(300),
        label = "syncScale"
    )
    
    Icon(
        statusIcon,
        contentDescription = status.name,
        tint = statusColor,
        modifier = Modifier
            .size(18.dp)
            .scale(scale)
    )
}

@Composable
private fun ThemeToggle(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val icon = when (currentTheme) {
        ThemeMode.DARK -> Icons.Default.Star
        ThemeMode.OLED -> Icons.Default.Menu
        ThemeMode.LIGHT -> Icons.Default.Face
    }
    
    val nextTheme = when (currentTheme) {
        ThemeMode.DARK -> ThemeMode.OLED
        ThemeMode.OLED -> ThemeMode.LIGHT
        ThemeMode.LIGHT -> ThemeMode.DARK
    }
    
    Surface(
        onClick = { onThemeChange(nextTheme) },
        color = if (isHovered) KlarityColors.BgElevated else Color.Transparent,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.hoverable(interactionSource)
    ) {
        Box(
            modifier = Modifier.padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = "Toggle theme",
                tint = KlarityColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ProfileButton() {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Surface(
        onClick = { /* Open profile menu */ },
        color = if (isHovered) KlarityColors.BgElevated else KlarityColors.BgTertiary,
        shape = CircleShape,
        modifier = Modifier.hoverable(interactionSource)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👤",
                fontSize = 14.sp
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// COMMAND PALETTE DIALOG
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Command types for the palette
 */
sealed class CommandItem(
    val icon: String,
    val title: String,
    val shortcut: String? = null,
    val category: String = "General"
) {
    // Navigation commands
    object GoToHome : CommandItem("🏠", "Go to Home", "Ctrl+H", "Navigation")
    object GoToNotes : CommandItem("📝", "Go to Notes", "Ctrl+N", "Navigation")
    object GoToTasks : CommandItem("🧩", "Go to Tasks", "Ctrl+T", "Navigation")
    object GoToSettings : CommandItem("⚙️", "Settings", "Ctrl+,", "Navigation")
    
    // Note commands
    object CreateNote : CommandItem("➕", "Create New Note", "Ctrl+Shift+N", "Notes")
    object QuickCapture : CommandItem("⚡", "Quick Capture", "Ctrl+Shift+C", "Notes")
    object SearchNotes : CommandItem("🔍", "Search All Notes", "Ctrl+F", "Notes")
    object RecentNotes : CommandItem("🕐", "Recent Notes", null, "Notes")
    
    // View commands
    object ToggleSidebar : CommandItem("◧", "Toggle Left Panel", "Ctrl+B", "View")
    object ToggleRightPanel : CommandItem("◨", "Toggle Right Panel", "Ctrl+Shift+B", "View")
    object ZenMode : CommandItem("🧘", "Enter Zen Mode", "Ctrl+Shift+Z", "View")
    object SplitView : CommandItem("⊟", "Split View", "Ctrl+\\", "View")
}

/**
 * Command Palette - Keyboard-first command interface
 */
@Composable
fun CommandPalette(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onCommandSelected: (CommandItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val luminousTeal = Color(0xFF1FDBC8)
    
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(tween(100)) + scaleIn(initialScale = 0.95f, animationSpec = tween(100)),
        exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.95f, animationSpec = tween(100))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier
                    .padding(top = 80.dp)
                    .width(560.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Prevent click-through
                    ),
                color = KlarityColors.BgSecondary,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 24.dp
            ) {
                CommandPaletteContent(
                    onCommandSelected = { command ->
                        onCommandSelected(command)
                        onDismiss()
                    },
                    luminousTeal = luminousTeal
                )
            }
        }
    }
}

@Composable
private fun CommandPaletteContent(
    onCommandSelected: (CommandItem) -> Unit,
    luminousTeal: Color
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableStateOf(0) }
    
    val allCommands = listOf(
        CommandItem.GoToHome,
        CommandItem.GoToNotes,
        CommandItem.GoToTasks,
        CommandItem.GoToSettings,
        CommandItem.CreateNote,
        CommandItem.QuickCapture,
        CommandItem.SearchNotes,
        CommandItem.RecentNotes,
        CommandItem.ToggleSidebar,
        CommandItem.ToggleRightPanel,
        CommandItem.ZenMode,
        CommandItem.SplitView
    )
    
    val filteredCommands = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            allCommands
        } else {
            allCommands.filter { 
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    // Reset selection when results change
    LaunchedEffect(filteredCommands.size) {
        selectedIndex = 0
    }
    
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        // Search input
        Surface(
            color = KlarityColors.BgTertiary,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = luminousTeal,
                    modifier = Modifier.size(20.dp)
                )
                
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(
                        color = KlarityColors.TextPrimary,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(luminousTeal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .onKeyEvent { event ->
                            when {
                                event.key == Key.DirectionDown && event.type == KeyEventType.KeyDown -> {
                                    selectedIndex = (selectedIndex + 1).coerceAtMost(filteredCommands.lastIndex)
                                    true
                                }
                                event.key == Key.DirectionUp && event.type == KeyEventType.KeyDown -> {
                                    selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
                                    true
                                }
                                event.key == Key.Enter && event.type == KeyEventType.KeyDown -> {
                                    if (filteredCommands.isNotEmpty()) {
                                        onCommandSelected(filteredCommands[selectedIndex])
                                    }
                                    true
                                }
                                else -> false
                            }
                        },
                    decorationBox = { innerTextField ->
                        Box {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Type a command or search…",
                                    color = KlarityColors.TextTertiary,
                                    fontSize = 15.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Command list grouped by category
        val groupedCommands = filteredCommands.groupBy { it.category }
        var currentIndex = 0
        
        Column(
            modifier = Modifier
                .heightIn(max = 400.dp)
        ) {
            groupedCommands.forEach { (category, commands) ->
                // Category header
                Text(
                    text = category,
                    color = KlarityColors.TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
                
                // Commands in category
                commands.forEach { command ->
                    val itemIndex = currentIndex++
                    val isSelected = itemIndex == selectedIndex
                    
                    CommandItemRow(
                        command = command,
                        isSelected = isSelected,
                        onClick = { onCommandSelected(command) },
                        luminousTeal = luminousTeal
                    )
                }
            }
        }
    }
}

@Composable
private fun CommandItemRow(
    command: CommandItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    luminousTeal: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> luminousTeal.copy(alpha = 0.15f)
            isHovered -> KlarityColors.BgElevated
            else -> Color.Transparent
        },
        animationSpec = tween(100),
        label = "bgColor"
    )
    
    Surface(
        onClick = onClick,
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = command.icon,
                    fontSize = 16.sp
                )
                
                Text(
                    text = command.title,
                    color = if (isSelected) luminousTeal else KlarityColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
            
            // Shortcut badge
            command.shortcut?.let { shortcut ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    shortcut.split("+").forEach { key ->
                        KeyBadge(key)
                    }
                }
            }
        }
    }
}
