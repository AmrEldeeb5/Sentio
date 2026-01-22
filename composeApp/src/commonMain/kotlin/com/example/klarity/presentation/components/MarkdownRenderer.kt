package com.example.klarity.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Wiki link annotation tag
private const val WIKI_LINK_TAG = "wiki_link"

/**
 * Markdown Renderer - Renders markdown content with syntax highlighting
 * Supports wiki-style links: [[note-name]] or [[note-name|display text]]
 */
@Composable
fun MarkdownRenderer(
    content: String,
    onWikiLinkClick: (noteName: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val lines = content.split("\n")
        var inCodeBlock = false
        var codeBlockContent = StringBuilder()
        var codeBlockLanguage = ""

        lines.forEach { line ->
            when {
                // Code block start
                line.trimStart().startsWith("```") && !inCodeBlock -> {
                    inCodeBlock = true
                    codeBlockLanguage = line.trimStart().removePrefix("```").trim()
                    codeBlockContent = StringBuilder()
                }
                // Code block end
                line.trimStart() == "```" && inCodeBlock -> {
                    inCodeBlock = false
                    CodeBlock(
                        code = codeBlockContent.toString().trimEnd(),
                        language = codeBlockLanguage
                    )
                }
                // Inside code block
                inCodeBlock -> {
                    codeBlockContent.appendLine(line)
                }
                // Headers
                line.startsWith("# ") -> {
                    MarkdownHeader(line.removePrefix("# "), level = 1, onWikiLinkClick = onWikiLinkClick)
                }
                line.startsWith("## ") -> {
                    MarkdownHeader(line.removePrefix("## "), level = 2, onWikiLinkClick = onWikiLinkClick)
                }
                line.startsWith("### ") -> {
                    MarkdownHeader(line.removePrefix("### "), level = 3, onWikiLinkClick = onWikiLinkClick)
                }
                // Unordered list
                line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ") -> {
                    val indent = line.length - line.trimStart().length
                    MarkdownListItem(
                        text = line.trimStart().drop(2),
                        indent = indent / 2,
                        isOrdered = false,
                        onWikiLinkClick = onWikiLinkClick
                    )
                }
                // Ordered list
                line.trimStart().matches(Regex("^\\d+\\.\\s.*")) -> {
                    val indent = line.length - line.trimStart().length
                    val text = line.trimStart().replace(Regex("^\\d+\\.\\s"), "")
                    MarkdownListItem(
                        text = text,
                        indent = indent / 2,
                        isOrdered = true,
                        onWikiLinkClick = onWikiLinkClick
                    )
                }
                // Checkbox (task list)
                line.trimStart().startsWith("- [ ] ") -> {
                    MarkdownCheckbox(
                        text = line.trimStart().removePrefix("- [ ] "), 
                        checked = false,
                        onWikiLinkClick = onWikiLinkClick
                    )
                }
                line.trimStart().startsWith("- [x] ") || line.trimStart().startsWith("- [X] ") -> {
                    MarkdownCheckbox(
                        text = line.trimStart().drop(6), 
                        checked = true,
                        onWikiLinkClick = onWikiLinkClick
                    )
                }
                // Blockquote
                line.startsWith("> ") -> {
                    MarkdownBlockquote(line.removePrefix("> "), onWikiLinkClick = onWikiLinkClick)
                }
                // Horizontal rule
                line.trim() in listOf("---", "***", "___") -> {
                    MarkdownHorizontalRule()
                }
                // Empty line
                line.isBlank() -> {
                    Spacer(Modifier.height(8.dp))
                }
                // Regular paragraph with inline formatting
                else -> {
                    MarkdownParagraph(line, onWikiLinkClick = onWikiLinkClick)
                }
            }
        }
    }
}

@Composable
fun MarkdownHeader(
    text: String, 
    level: Int,
    onWikiLinkClick: (String) -> Unit = {}
) {
    val (fontSize, fontWeight) = when (level) {
        1 -> 28.sp to FontWeight.Bold
        2 -> 22.sp to FontWeight.SemiBold
        3 -> 18.sp to FontWeight.Medium
        else -> 16.sp to FontWeight.Normal
    }
    
    val annotatedString = parseInlineMarkdownWithWikiLinks(text)
    ClickableText(
        text = annotatedString,
        style = androidx.compose.ui.text.TextStyle(
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = Color.White
        ),
        modifier = Modifier.padding(vertical = 4.dp),
        onClick = { offset ->
            annotatedString.getStringAnnotations(WIKI_LINK_TAG, offset, offset)
                .firstOrNull()?.let { annotation ->
                    onWikiLinkClick(annotation.item)
                }
        }
    )
}

@Composable
fun MarkdownParagraph(
    text: String,
    onWikiLinkClick: (String) -> Unit = {}
) {
    val annotatedString = parseInlineMarkdownWithWikiLinks(text)
    ClickableText(
        text = annotatedString,
        style = androidx.compose.ui.text.TextStyle(
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(WIKI_LINK_TAG, offset, offset)
                .firstOrNull()?.let { annotation ->
                    onWikiLinkClick(annotation.item)
                }
        }
    )
}

@Composable
fun MarkdownListItem(
    text: String, 
    indent: Int, 
    isOrdered: Boolean,
    onWikiLinkClick: (String) -> Unit = {}
) {
    Row(
        modifier = Modifier.padding(start = (indent * 16).dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            if (isOrdered) "•" else "•",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.tertiary
        )
        val annotatedString = parseInlineMarkdownWithWikiLinks(text)
        ClickableText(
            text = annotatedString,
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            ),
            onClick = { offset ->
                annotatedString.getStringAnnotations(WIKI_LINK_TAG, offset, offset)
                    .firstOrNull()?.let { annotation ->
                        onWikiLinkClick(annotation.item)
                    }
            }
        )
    }
}

@Composable
fun MarkdownCheckbox(
    text: String, 
    checked: Boolean,
    onWikiLinkClick: (String) -> Unit = {}
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            if (checked) "☑" else "☐",
            fontSize = 15.sp,
            color = if (checked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        val annotatedString = parseInlineMarkdownWithWikiLinks(text)
        ClickableText(
            text = annotatedString,
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 15.sp,
                color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            ),
            onClick = { offset ->
                annotatedString.getStringAnnotations(WIKI_LINK_TAG, offset, offset)
                    .firstOrNull()?.let { annotation ->
                        onWikiLinkClick(annotation.item)
                    }
            }
        )
    }
}

@Composable
fun MarkdownBlockquote(
    text: String,
    onWikiLinkClick: (String) -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.tertiary)
            )
            val annotatedString = parseInlineMarkdownWithWikiLinks(text)
            ClickableText(
                text = annotatedString,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(12.dp),
                onClick = { offset ->
                    annotatedString.getStringAnnotations(WIKI_LINK_TAG, offset, offset)
                        .firstOrNull()?.let { annotation ->
                            onWikiLinkClick(annotation.item)
                        }
                }
            )
        }
    }
}

@Composable
fun MarkdownHorizontalRule() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline)
    )
}


@Composable
fun CodeBlock(code: String, language: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0D1117),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            // Language header
            if (language.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF161B22)
                ) {
                    Text(
                        language,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            // Code content with syntax highlighting
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = highlightSyntax(code, language),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// Parse inline markdown (bold, italic, code, links)
fun parseInlineMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var remaining = text
        
        while (remaining.isNotEmpty()) {
            when {
                // Bold **text**
                remaining.startsWith("**") -> {
                    val endIndex = remaining.indexOf("**", 2)
                    if (endIndex > 2) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                            append(remaining.substring(2, endIndex))
                        }
                        remaining = remaining.substring(endIndex + 2)
                    } else {
                        append("**")
                        remaining = remaining.drop(2)
                    }
                }
                // Italic _text_ or *text*
                remaining.startsWith("_") && !remaining.startsWith("__") -> {
                    val endIndex = remaining.indexOf("_", 1)
                    if (endIndex > 1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(remaining.substring(1, endIndex))
                        }
                        remaining = remaining.substring(endIndex + 1)
                    } else {
                        append("_")
                        remaining = remaining.drop(1)
                    }
                }
                // Inline code `code`
                remaining.startsWith("`") && !remaining.startsWith("```") -> {
                    val endIndex = remaining.indexOf("`", 1)
                    if (endIndex > 1) {
                        withStyle(SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0xFF2D333B),
                            color = Color(0xFFE06C75)
                        )) {
                            append(" ${remaining.substring(1, endIndex)} ")
                        }
                        remaining = remaining.substring(endIndex + 1)
                    } else {
                        append("`")
                        remaining = remaining.drop(1)
                    }
                }
                // Link [text](url)
                remaining.startsWith("[") -> {
                    val textEnd = remaining.indexOf("]")
                    val urlStart = remaining.indexOf("(", textEnd)
                    val urlEnd = remaining.indexOf(")", urlStart)
                    if (textEnd > 1 && urlStart == textEnd + 1 && urlEnd > urlStart) {
                        val linkText = remaining.substring(1, textEnd)
                        withStyle(SpanStyle(color = Color(0xFF1FDBC8))) { // Use primary teal color
                            append(linkText)
                        }
                        remaining = remaining.substring(urlEnd + 1)
                    } else {
                        append("[")
                        remaining = remaining.drop(1)
                    }
                }
                else -> {
                    append(remaining.first())
                    remaining = remaining.drop(1)
                }
            }
        }
    }
}

/**
 * Parse inline markdown with wiki-style links support
 * Supports: [[note-name]] and [[note-name|display text]]
 */
fun parseInlineMarkdownWithWikiLinks(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var remaining = text
        
        while (remaining.isNotEmpty()) {
            when {
                // Wiki-style link [[note-name]] or [[note-name|display text]]
                remaining.startsWith("[[") -> {
                    val endIndex = remaining.indexOf("]]")
                    if (endIndex > 2) {
                        val linkContent = remaining.substring(2, endIndex)
                        val (noteName, displayText) = if (linkContent.contains("|")) {
                            val parts = linkContent.split("|", limit = 2)
                            parts[0].trim() to parts[1].trim()
                        } else {
                            linkContent.trim() to linkContent.trim()
                        }
                        
                        // Add clickable annotation
                        pushStringAnnotation(tag = WIKI_LINK_TAG, annotation = noteName)
                        withStyle(SpanStyle(
                            color = Color(0xFF1FDBC8), // Luminous Teal
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline
                        )) {
                            append("📄 $displayText")
                        }
                        pop()
                        
                        remaining = remaining.substring(endIndex + 2)
                    } else {
                        append("[[")
                        remaining = remaining.drop(2)
                    }
                }
                // Bold **text**
                remaining.startsWith("**") -> {
                    val endIndex = remaining.indexOf("**", 2)
                    if (endIndex > 2) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                            append(remaining.substring(2, endIndex))
                        }
                        remaining = remaining.substring(endIndex + 2)
                    } else {
                        append("**")
                        remaining = remaining.drop(2)
                    }
                }
                // Italic _text_ or *text*
                remaining.startsWith("_") && !remaining.startsWith("__") -> {
                    val endIndex = remaining.indexOf("_", 1)
                    if (endIndex > 1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(remaining.substring(1, endIndex))
                        }
                        remaining = remaining.substring(endIndex + 1)
                    } else {
                        append("_")
                        remaining = remaining.drop(1)
                    }
                }
                // Inline code `code`
                remaining.startsWith("`") && !remaining.startsWith("```") -> {
                    val endIndex = remaining.indexOf("`", 1)
                    if (endIndex > 1) {
                        withStyle(SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0xFF2D333B),
                            color = Color(0xFFE06C75)
                        )) {
                            append(" ${remaining.substring(1, endIndex)} ")
                        }
                        remaining = remaining.substring(endIndex + 1)
                    } else {
                        append("`")
                        remaining = remaining.drop(1)
                    }
                }
                // Link [text](url)
                remaining.startsWith("[") && !remaining.startsWith("[[") -> {
                    val textEnd = remaining.indexOf("]")
                    val urlStart = remaining.indexOf("(", textEnd)
                    val urlEnd = remaining.indexOf(")", urlStart)
                    if (textEnd > 1 && urlStart == textEnd + 1 && urlEnd > urlStart) {
                        val linkText = remaining.substring(1, textEnd)
                        withStyle(SpanStyle(
                            color = Color(0xFF1FDBC8), // Luminous Teal
                            textDecoration = TextDecoration.Underline
                        )) {
                            append(linkText)
                        }
                        remaining = remaining.substring(urlEnd + 1)
                    } else {
                        append("[")
                        remaining = remaining.drop(1)
                    }
                }
                else -> {
                    append(remaining.first())
                    remaining = remaining.drop(1)
                }
            }
        }
    }
}

// Basic syntax highlighting for code blocks
fun highlightSyntax(code: String, language: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val keywords = when (language.lowercase()) {
            "kotlin", "kt" -> listOf("fun", "val", "var", "class", "object", "interface", "if", "else", "when", "for", "while", "return", "import", "package", "private", "public", "internal", "suspend", "override", "data", "sealed", "enum", "companion", "null", "true", "false")
            "javascript", "js", "typescript", "ts" -> listOf("function", "const", "let", "var", "class", "if", "else", "for", "while", "return", "import", "export", "from", "async", "await", "null", "undefined", "true", "false", "new", "this")
            "python", "py" -> listOf("def", "class", "if", "elif", "else", "for", "while", "return", "import", "from", "as", "None", "True", "False", "and", "or", "not", "in", "is", "lambda", "with", "try", "except", "finally")
            "java" -> listOf("public", "private", "protected", "class", "interface", "extends", "implements", "if", "else", "for", "while", "return", "import", "package", "new", "null", "true", "false", "void", "static", "final")
            else -> emptyList()
        }
        
        val lines = code.split("\n")
        lines.forEachIndexed { index, line ->
            var remaining = line
            
            while (remaining.isNotEmpty()) {
                when {
                    // Comments
                    remaining.startsWith("//") || remaining.startsWith("#") -> {
                        withStyle(SpanStyle(color = Color(0xFF6A737D))) {
                            append(remaining)
                        }
                        remaining = ""
                    }
                    // Strings
                    remaining.startsWith("\"") -> {
                        val endIndex = remaining.indexOf("\"", 1)
                        if (endIndex > 0) {
                            withStyle(SpanStyle(color = Color(0xFF98C379))) {
                                append(remaining.substring(0, endIndex + 1))
                            }
                            remaining = remaining.substring(endIndex + 1)
                        } else {
                            withStyle(SpanStyle(color = Color(0xFF98C379))) {
                                append(remaining)
                            }
                            remaining = ""
                        }
                    }
                    remaining.startsWith("'") -> {
                        val endIndex = remaining.indexOf("'", 1)
                        if (endIndex > 0) {
                            withStyle(SpanStyle(color = Color(0xFF98C379))) {
                                append(remaining.substring(0, endIndex + 1))
                            }
                            remaining = remaining.substring(endIndex + 1)
                        } else {
                            withStyle(SpanStyle(color = Color(0xFF98C379))) {
                                append(remaining)
                            }
                            remaining = ""
                        }
                    }
                    // Keywords
                    keywords.any { kw -> remaining.startsWith(kw) && (remaining.length == kw.length || !remaining[kw.length].isLetterOrDigit()) } -> {
                        val keyword = keywords.first { kw -> remaining.startsWith(kw) && (remaining.length == kw.length || !remaining[kw.length].isLetterOrDigit()) }
                        withStyle(SpanStyle(color = Color(0xFFC678DD))) {
                            append(keyword)
                        }
                        remaining = remaining.drop(keyword.length)
                    }
                    // Numbers
                    remaining.first().isDigit() -> {
                        val numEnd = remaining.indexOfFirst { !it.isDigit() && it != '.' }.takeIf { it > 0 } ?: remaining.length
                        withStyle(SpanStyle(color = Color(0xFFD19A66))) {
                            append(remaining.substring(0, numEnd))
                        }
                        remaining = remaining.substring(numEnd)
                    }
                    else -> {
                        withStyle(SpanStyle(color = Color(0xFFABB2BF))) {
                            append(remaining.first())
                        }
                        remaining = remaining.drop(1)
                    }
                }
            }
            if (index < lines.lastIndex) append("\n")
        }
    }
}
