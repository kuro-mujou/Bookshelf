package com.capstone.bookshelf.presentation.bookcontent.content.content_component

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.capstone.bookshelf.presentation.bookcontent.component.font.FontState

@Immutable
data class ImageContent(
    val content: String,
){
    var zoom = mutableFloatStateOf(1f)
    var offset = mutableStateOf(Offset.Zero)
}

@Immutable
data class HeaderContent(
    val content: String,
    val fontState : FontState,
    val level : Int
){
    val fontSize = mutableFloatStateOf(calculateHeaderSize(level,fontState.fontSize))
}
private fun calculateHeaderSize(level : Int, fontSize: Int): Float {
    return when(level){
        1 -> fontSize*2f
        2 -> fontSize*1.5f
        3 -> fontSize*1.17f
        4 -> fontSize*1f
        5 -> fontSize*0.83f
        6 -> fontSize*0.67f
        else -> fontSize.toFloat()
    }
}
@Immutable
data class ParagraphContent(
    val content: String,
    val fontState : FontState
){
    val text = mutableStateOf(convertToAnnotatedStrings(content))
}
private fun convertToAnnotatedStrings(paragraph: String): AnnotatedString {
    return buildAnnotatedString {
        val stack = mutableListOf<String>()
        var currentIndex = 0

        while (currentIndex < paragraph.length) {
            when {
                paragraph.startsWith("<b>", currentIndex) -> {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    stack.add("b")
                    currentIndex += 3
                }
                paragraph.startsWith("</b>", currentIndex) -> {
                    if (stack.lastOrNull() == "b") {
                        pop()
                        stack.removeAt(stack.lastIndex)
                    }
                    currentIndex += 4
                }
                paragraph.startsWith("<i>", currentIndex) -> {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    stack.add("i")
                    currentIndex += 3
                }
                paragraph.startsWith("</i>", currentIndex) -> {
                    if (stack.lastOrNull() == "i") {
                        pop()
                        stack.removeAt(stack.lastIndex)
                    }
                    currentIndex += 4
                }
                paragraph.startsWith("<u>", currentIndex) -> {
                    pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                    stack.add("u")
                    currentIndex += 3
                }
                paragraph.startsWith("</u>", currentIndex) -> {
                    if (stack.lastOrNull() == "u") {
                        pop()
                        stack.removeAt(stack.lastIndex)
                    }
                    currentIndex += 4
                }
                else -> {
                    append(paragraph[currentIndex])
                    currentIndex++
                }
            }
        }
    }
}
