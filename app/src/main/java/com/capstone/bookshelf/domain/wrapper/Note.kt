package com.capstone.bookshelf.domain.wrapper

data class Note(
    val noteId: Int = 0,
    val bookId: String,
    val contentIndex: Int,
    val contentDetail: String,
    val noteContent: String,
    val timestamp: Long
)
