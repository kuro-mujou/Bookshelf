package com.capstone.bookshelf.presentation.bookwriter.component

import java.util.UUID

data class Paragraph(
    val id: String = UUID.randomUUID().toString(),
    var text: String,
    var isControllerVisible: Boolean = false,
    val type: ParagraphType,
    val headerLevel: Int? = null
)

enum class ParagraphType{
    TITLE,
    SUBTITLE,
    PARAGRAPH,
    IMAGE,
    ADD_IMAGE,
}