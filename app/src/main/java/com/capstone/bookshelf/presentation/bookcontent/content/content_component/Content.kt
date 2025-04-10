package com.capstone.bookshelf.presentation.bookcontent.content.content_component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.util.calculateHeaderSize

@UnstableApi
@Composable
fun Content(
    content: String,
    isHighlighted : Boolean,
    isSpeaking : Boolean,
    colorPaletteState: ColorPalette,
    fontState: ContentState,
){
    val linkPattern = Regex("""\.capstone\.bookshelf/files/[^ ]*""")
    val headerPatten = Regex("""<h([1-6])[^>]*>(.*?)</h([1-6])>""")
    val headerLevel = Regex("""<h([1-6])>.*?</h\1>""")
    val htmlTagPattern = Regex(pattern = """<[^>]+>""")
    if(linkPattern.containsMatchIn(content)) {
        ImageComponent(
            content = ImageContent(
                content = content,
                contentState = fontState
            ),
        )
    }else if(headerPatten.containsMatchIn(content)) {
        if(htmlTagPattern.replace(content, replacement = "").isNotEmpty()){
            HeaderText(
                colorPaletteState = colorPaletteState,
                contentState = fontState,
                content = HeaderContent(
                    content = htmlTagPattern.replace(content, replacement = ""),
                    contentState = fontState,
                    level = headerLevel.find(content)!!.groupValues[1].toInt(),
                ),
                isHighlighted = isHighlighted,
                isSpeaking = isSpeaking
            )
        }
    } else{
        if(htmlTagPattern.replace(content, replacement = "").isNotEmpty()){
            ParagraphText(
                colorPaletteState = colorPaletteState,
                contentState = fontState,
                content = ParagraphContent(
                    content = content,
                    contentState = fontState,
                ),
                isHighlighted = isHighlighted,
                isSpeaking = isSpeaking
            )
        }
    }
}

@Immutable
@UnstableApi
data class ImageContent(
    val content: String,
    val contentState : ContentState
)

@Immutable
@UnstableApi
data class HeaderContent(
    val content: String,
    val contentState : ContentState,
    val level : Int
){
    val fontSize = mutableFloatStateOf(calculateHeaderSize(level,contentState.fontSize))
}
@Immutable
@UnstableApi
data class ParagraphContent(
    val content: String,
    val contentState : ContentState
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
                paragraph.startsWith("<strong>", currentIndex) -> {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    stack.add("strong")
                    currentIndex += 8
                }
                paragraph.startsWith("</strong>", currentIndex) -> {
                    if (stack.lastOrNull() == "strong") {
                        pop()
                        stack.removeAt(stack.lastIndex)
                    }
                    currentIndex += 9
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
                paragraph.startsWith("<em>", currentIndex) -> {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    stack.add("em")
                    currentIndex += 4
                }
                paragraph.startsWith("</em>", currentIndex) -> {
                    if (stack.lastOrNull() == "em") {
                        pop()
                        stack.removeAt(stack.lastIndex)
                    }
                    currentIndex += 5
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