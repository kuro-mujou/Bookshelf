package com.capstone.bookshelf.domain.book.wrapper

import androidx.compose.runtime.Immutable

@Immutable
data class Chapter(
    val chapterContentId: Int = 0,
    val tocId: Int,
    val bookId: String,
    val chapterTitle: String,
    val content: List<String>,
)