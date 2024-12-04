package com.capstone.bookshelf.domain.book.wrapper

import androidx.compose.runtime.Immutable

@Immutable
data class Book(
    val id: String,
    val title: String,
    val coverImagePath: String,
    val authors: List<String>,
    val categories: List<String>,
    val description: String?,
    val totalChapter: Int,
    val currentChapter: Int = 1,
    val isFavorite: Boolean = false,
    val ratingsAverage: Double?,
    val ratingsCount: Int?,
)
