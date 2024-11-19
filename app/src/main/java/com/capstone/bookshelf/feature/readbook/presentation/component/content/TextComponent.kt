package com.capstone.bookshelf.feature.readbook.presentation.component.content

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HeaderText(
    content: HeaderContent,
    style: TextStyle,
    isHighlighted: Boolean,
    isSpeaking: Boolean
) {
    val level = content.content.substring(2,3).toInt()
    val color = if(isHighlighted && isSpeaking)
        style.background
    else
        Color.Transparent
    val size = when(level){
        1 -> style.fontSize*2
        2 -> style.fontSize*1.5
        3 -> style.fontSize*1.17
        4 -> style.fontSize*1
        5 -> style.fontSize*0.83
        6 -> style.fontSize*0.67
        else -> style.fontSize
    }
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        text = content.content.replace(content.removePatten, ""),
        style = TextStyle(
            fontSize = size,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = style.color,
            background = color
        )
    )
}
@Composable
fun ParagraphText(
    content: ParagraphContent,
    style: TextStyle,
    isHighlighted: Boolean,
    isSpeaking: Boolean
) {
    val color = if(isHighlighted && isSpeaking)
        style.background
    else
        Color.Transparent
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        text = content.content,
        style = TextStyle(
            textIndent = style.textIndent,
            textAlign = style.textAlign,
            fontSize = style.fontSize,
            color = style.color,
            background = color,
            lineBreak = style.lineBreak,
        ),
    )
}