package com.capstone.bookshelf.feature.readbook.presentation.component.content

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.AnnotatedString

@Immutable
data class ImageContent(
    val content: String,
){
    var zoom = mutableFloatStateOf(1f)
    var offset = mutableStateOf(Offset.Zero)
    var popup = mutableStateOf(false)
}

@Immutable
data class HeaderContent(
    val content: String
){
    val removePatten = Regex("""<[^>]+>""")
}

@Immutable
data class ParagraphContent(
    val content: AnnotatedString
)
