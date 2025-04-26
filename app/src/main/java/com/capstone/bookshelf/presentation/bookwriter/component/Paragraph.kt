package com.capstone.bookshelf.presentation.bookwriter.component

import com.mohamedrejeb.richeditor.model.RichTextState
import java.util.UUID

data class Paragraph(
    val id: String = UUID.randomUUID().toString(),
    val type: ParagraphType,
    var richTextState: RichTextState,
    var isControllerVisible: Boolean = false,
)

enum class ParagraphType {
    TITLE,
    PARAGRAPH,
    IMAGE
}