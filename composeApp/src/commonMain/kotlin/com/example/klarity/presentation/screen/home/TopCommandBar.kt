package com.example.klarity.presentation.screen.home

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.klarity.presentation.theme.KlarityMotion
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
 * Top Command Bar - Compact Global Navigation
 * 
 * A compact, unified bar containing:
 * - Left: App identifier (Klarity), Breadcrumbs  
 * - Center: Omnibar (Search + Commands)
 */
@Composable
fun TopCommandBar(
    currentPath: List<String> = listOf("Home"),
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onCommandPaletteOpen: () -> Unit = {},
    hasNotes: Boolean = false,
    modifier: Modifier = Modifier
) {
    val luminousTeal = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
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
                luminousTeal = luminousTeal,
                hasNotes = hasNotes
            )
            
            // Right Section - Empty as requested
            Spacer(modifier = Modifier.width(48.dp))
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
            
            val interactionSource = remember { MutableInteractionSource() }
            val isHovered by interactionSource.collectIsHoveredAsState()
            
            Text(
                text = item,
                color = if (index == path.lastIndex) 
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = if (index == path.lastIndex) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier
                    .hoverable(interactionSource)
                    .clip(RoundedCornerShape(4.dp))
                    .then(
                        if (isHovered && index < path.lastIndex) {
                            Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
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
    luminousTeal: Color,
    hasNotes: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OmniBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            onCommandPaletteOpen = onCommandPaletteOpen,
            luminousTeal = luminousTeal,
            hasNotes = hasNotes
        )
    }
}

@Composable
private fun OmniBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onCommandPaletteOpen: () -> Unit,
    luminousTeal: Color,
    hasNotes: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isFocused by remember { mutableStateOf(false) }
    
    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> MaterialTheme.colorScheme.surfaceVariant
            isHovered -> MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        },
        animationSpec = KlarityMotion.standardExit(),
        label = "bgColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> luminousTeal.copy(alpha = 0.5f)
            isHovered -> MaterialTheme.colorScheme.outline
            else -> Color.Transparent
        },
        animationSpec = KlarityMotion.standardExit(),
        label = "borderColor"
    )
    
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(520.dp)
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
                tint = if (isFocused) luminousTeal else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
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
                                text = "Search notes...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
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
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = key,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// COMMAND PALETTE DIALOG
// ══════════════════════════════════════════════════════════════════════════════

sealed class CommandItem(
    val icon: String,
    val title: String,
    val shortcut: String? = null,
    val category: String = "General"
) {
    object GoToHome : CommandItem("🏠", "Go to Home", "Ctrl+H", "Navigation")
    object GoToNotes : CommandItem("📝", "Go to Notes", "Ctrl+N", "Navigation")
    object GoToTasks : CommandItem("🧩", "Go to Tasks", "Ctrl+T", "Navigation")
    object GoToSettings : CommandItem("⚙️", "Settings", "Ctrl+,", "Navigation")
    
    object CreateNote : CommandItem("➕", "Create New Note", "Ctrl+Shift+N", "Notes")
    object QuickCapture : CommandItem("⚡", "Quick Capture", "Ctrl+Shift+C", "Notes")
    object SearchNotes : CommandItem("🔍", "Search All Notes", "Ctrl+F", "Notes")
    object RecentNotes : CommandItem("🕐", "Recent Notes", null, "Notes")
    
    object ToggleSidebar : CommandItem("◧", "Toggle Left Panel", "Ctrl+B", "View")
    object ToggleRightPanel : CommandItem("◨", "Toggle Right Panel", "Ctrl+Shift+B", "View")
    object ZenMode : CommandItem("🧘", "Enter Zen Mode", "Ctrl+Shift+Z", "View")
    object SplitView : CommandItem("⊟", "Split View", "Ctrl+\\", "View")
}

@Composable
fun CommandPalette(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onCommandSelected: (CommandItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val luminousTeal = KlarityColors.LuminousTeal
    
    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(KlarityMotion.quickExit()) + scaleIn(initialScale = 0.95f, animationSpec = KlarityMotion.quickExit()),
        exit = fadeOut(KlarityMotion.quickExit()) + scaleOut(targetScale = 0.95f, animationSpec = KlarityMotion.quickExit())
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
                        onClick = {}
                    ),
                color = MaterialTheme.colorScheme.surfaceVariant,
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
    
    LaunchedEffect(filteredCommands.size) {
        selectedIndex = 0
    }
    
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
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
                        color = MaterialTheme.colorScheme.onSurface,
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
        
        val groupedCommands = filteredCommands.groupBy { it.category }
        var currentIndex = 0
        
        Column(
            modifier = Modifier
                .heightIn(max = 400.dp)
        ) {
            groupedCommands.forEach { (category, commands) ->
                Text(
                    text = category,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
                
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
            isHovered -> MaterialTheme.colorScheme.surfaceVariant
            else -> Color.Transparent
        },
        animationSpec = KlarityMotion.quickExit(),
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
                    color = if (isSelected) luminousTeal else MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
            
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
