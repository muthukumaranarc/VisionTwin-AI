package com.visiontwin.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visiontwin.app.ui.theme.VTOnSurface
import com.visiontwin.app.ui.theme.VTPrimary
import com.visiontwin.app.ui.theme.VTSurfaceLow

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = VTOnSurface
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val lines = markdown.split("\n")
        var inCodeBlock = false
        val codeBlockLines = mutableListOf<String>()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            if (line.trim().startsWith("```")) {
                if (inCodeBlock) {
                    // End of code block
                    CodeBlock(code = codeBlockLines.joinToString("\n"))
                    codeBlockLines.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                i++
                continue
            }

            if (inCodeBlock) {
                codeBlockLines.add(line)
                i++
                continue
            }

            val trimmed = line.trim()
            when {
                trimmed.startsWith("# ") -> {
                    Heading(text = trimmed.substring(2), level = 1, color = textColor)
                }
                trimmed.startsWith("## ") -> {
                    Heading(text = trimmed.substring(3), level = 2, color = textColor)
                }
                trimmed.startsWith("### ") -> {
                    Heading(text = trimmed.substring(4), level = 3, color = textColor)
                }
                trimmed.startsWith("#### ") -> {
                    Heading(text = trimmed.substring(5), level = 4, color = textColor)
                }
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    BulletItem(text = trimmed.substring(2), color = textColor)
                }
                trimmed.firstOrNull()?.isDigit() == true && trimmed.contains(". ") && trimmed.split(". ").firstOrNull()?.all { it.isDigit() } == true -> {
                    val parts = trimmed.split(". ", limit = 2)
                    NumberedItem(num = parts[0], text = parts.getOrNull(1) ?: "", color = textColor)
                }
                trimmed.isEmpty() -> {
                    // Small spacer
                    Spacer(modifier = Modifier.height(2.dp))
                }
                else -> {
                    Paragraph(text = line, color = textColor)
                }
            }
            i++
        }

        if (inCodeBlock && codeBlockLines.isNotEmpty()) {
            CodeBlock(code = codeBlockLines.joinToString("\n"))
        }
    }
}

@Composable
private fun Heading(text: String, level: Int, color: Color) {
    val fontSize = when (level) {
        1 -> 20.sp
        2 -> 18.sp
        3 -> 16.sp
        else -> 14.sp
    }
    val fontWeight = FontWeight.Bold
    Text(
        text = parseInlineMarkdown(text),
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = if (level == 1) VTPrimary else color,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun BulletItem(text: String, color: Color) {
    Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
        Text("• ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = VTPrimary)
        Text(
            text = parseInlineMarkdown(text),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = color
        )
    }
}

@Composable
private fun NumberedItem(num: String, text: String, color: Color) {
    Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
        Text("$num. ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VTPrimary)
        Text(
            text = parseInlineMarkdown(text),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = color
        )
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(VTSurfaceLow, RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun Paragraph(text: String, color: Color) {
    Text(
        text = parseInlineMarkdown(text),
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = color
    )
}

private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // Bold: **text** or __text__
                text.startsWith("**", i) && text.indexOf("**", i + 2) != -1 -> {
                    val end = text.indexOf("**", i + 2)
                    val boldText = text.substring(i + 2, end)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(boldText)
                    pop()
                    i = end + 2
                }
                text.startsWith("__", i) && text.indexOf("__", i + 2) != -1 -> {
                    val end = text.indexOf("__", i + 2)
                    val boldText = text.substring(i + 2, end)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(boldText)
                    pop()
                    i = end + 2
                }
                // Italic: *text* or _text_
                text.startsWith("*", i) && text.indexOf("*", i + 1) != -1 -> {
                    val end = text.indexOf("*", i + 1)
                    val italicText = text.substring(i + 1, end)
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(italicText)
                    pop()
                    i = end + 1
                }
                text.startsWith("_", i) && text.indexOf("_", i + 1) != -1 -> {
                    val end = text.indexOf("_", i + 1)
                    val italicText = text.substring(i + 1, end)
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(italicText)
                    pop()
                    i = end + 1
                }
                // Inline Code: `code`
                text.startsWith("`", i) && text.indexOf("`", i + 1) != -1 -> {
                    val end = text.indexOf("`", i + 1)
                    val codeText = text.substring(i + 1, end)
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            background = Color(0xFFE2E8F0),
                            color = Color(0xFF0F172A)
                        )
                    )
                    append(codeText)
                    pop()
                    i = end + 1
                }
                else -> {
                    append(text[i].toString())
                    i++
                }
            }
        }
    }
}
