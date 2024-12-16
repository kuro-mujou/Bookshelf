package com.capstone.bookshelf.domain.wrapper

data class Chapter(
    val chapterContentId: Int = 0,
    val tocId: Int,
    val bookId: String,
    val chapterTitle: String,
    val content: List<String>,
)