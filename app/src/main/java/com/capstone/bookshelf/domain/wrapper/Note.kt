package com.capstone.bookshelf.domain.wrapper

data class Note(
    val noteId: Int = 0,
    val bookId: String,
    val tocId: Int,
    val contentId: Int,
    val noteBody: String,
    val noteInput: String,
    val timestamp: String
)
