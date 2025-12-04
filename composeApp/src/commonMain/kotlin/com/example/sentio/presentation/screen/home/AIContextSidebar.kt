package com.example.sentio.presentation.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentio.domain.models.Note
import com.example.sentio.presentation.theme.SentioColors

/**
 * AI Context Sidebar - Right panel showing AI-generated context for the selected note
 */
@Composable
fun AIContextSidebar(note: Note?) {
    var refsExpanded by remember { mutableStateOf(true) }
    var issuesExpanded by remember { mutableStateOf(false) }
    var keywordsExpanded by remember { mutableStateOf(false) }
    var graphExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.width(280.dp).fillMaxHeight(),
        color = Color(0xFF11221F),
        border = BorderStroke(1.dp, SentioColors.BorderPrimary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("✨", fontSize = 16.sp, color = SentioColors.AccentAI)
                Text(
                    "AI Context",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // Scrollable sections
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // AI-Generated References
                ContextSection(
                    title = "AI-Generated References",
                    isExpanded = refsExpanded,
                    onToggle = { refsExpanded = !refsExpanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReferenceItem("1. 'Advanced Markdown Editors' - Tech Journal, 2023")
                        ReferenceItem("2. 'AI in Developer Workflows' - DevWeekly, 2024")
                    }
                }

                // Linked GitHub Issues
                ContextSection(
                    title = "Linked GitHub Issues",
                    isExpanded = issuesExpanded,
                    onToggle = { issuesExpanded = !issuesExpanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GitHubIssueItem("#124: Implement Tri-pane view", Color(0xFF10B981))
                        GitHubIssueItem("#119: Fix code block highlight", Color(0xFFA855F7), isOpen = false)
                    }
                }

                // Detected Keywords
                ContextSection(
                    title = "Detected Keywords",
                    isExpanded = keywordsExpanded,
                    onToggle = { keywordsExpanded = !keywordsExpanded }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val keywords = note?.tags?.ifEmpty { listOf("Markdown", "AI", "Editor", "UI/UX") }
                            ?: listOf("Markdown", "AI", "Editor", "UI/UX")
                        keywords.take(4).forEach { keyword ->
                            KeywordChip(keyword)
                        }
                    }
                }

                // Graph Connections
                ContextSection(
                    title = "Graph Connections",
                    isExpanded = graphExpanded,
                    onToggle = { graphExpanded = !graphExpanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "CONNECTS TO:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SentioColors.TextTertiary.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        ConnectionItem("Product Roadmap Q3")
                        ConnectionItem("Initial Design Mockups")
                    }
                }
            }

            // Note metadata at bottom
            if (note != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = SentioColors.AccentAI.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, SentioColors.AccentAI.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "NOTE INFO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SentioColors.AccentAI,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        NoteInfoRow("Created", formatRelativeTime(note.createdAt))
                        NoteInfoRow("Updated", formatRelativeTime(note.updatedAt))
                        NoteInfoRow("Words", "${note.wordCount()}")
                        if (note.isPinned) {
                            NoteInfoRow("Status", "📌 Pinned")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                if (isExpanded) "▲" else "▼",
                fontSize = 12.sp,
                color = SentioColors.TextTertiary
            )
        }

        // Content
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(modifier = Modifier.padding(bottom = 12.dp)) {
                content()
            }
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SentioColors.BorderPrimary)
        )
    }
}

@Composable
private fun ReferenceItem(text: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Text(
        text,
        fontSize = 13.sp,
        color = if (isHovered) SentioColors.AccentAI else SentioColors.TextSecondary,
        lineHeight = 20.sp,
        modifier = Modifier
            .clickable(interactionSource = interactionSource, indication = null) { }
            .hoverable(interactionSource)
    )
}

@Composable
private fun GitHubIssueItem(text: String, statusColor: Color, isOpen: Boolean = true) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(statusColor, CircleShape)
        )
        Text(
            text,
            fontSize = 13.sp,
            color = if (isOpen) SentioColors.TextSecondary else SentioColors.TextTertiary.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun KeywordChip(keyword: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Surface(
        modifier = Modifier
            .clickable(interactionSource = interactionSource, indication = null) { }
            .hoverable(interactionSource),
        shape = RoundedCornerShape(4.dp),
        color = if (isHovered) SentioColors.AccentAI.copy(alpha = 0.2f) else SentioColors.BgSelected.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (isHovered) SentioColors.AccentAI.copy(alpha = 0.5f) else SentioColors.BorderPrimary
        )
    ) {
        Text(
            keyword,
            fontSize = 12.sp,
            color = if (isHovered) SentioColors.AccentAI else SentioColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ConnectionItem(text: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Text(
        "• $text",
        fontSize = 13.sp,
        color = if (isHovered) SentioColors.AccentAI else SentioColors.TextSecondary,
        modifier = Modifier
            .clickable(interactionSource = interactionSource, indication = null) { }
            .hoverable(interactionSource)
    )
}

@Composable
private fun NoteInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = SentioColors.TextTertiary)
        Text(value, fontSize = 12.sp, color = SentioColors.TextSecondary)
    }
}

