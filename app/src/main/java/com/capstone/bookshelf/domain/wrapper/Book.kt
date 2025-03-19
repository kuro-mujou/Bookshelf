package com.capstone.bookshelf.domain.wrapper

data class Book(
    val id: String,
    val title: String,
    val coverImagePath: String,
    val authors: List<String>,
    val categories: List<String>,
    val description: String?,
    val totalChapter: Int,
    val currentChapter: Int = 0,
    val currentParagraph: Int = 0,
    val isFavorite: Boolean = false,
    val storagePath: String?,
    val isEditable: Boolean
)

data class EmptyBook(
    val id: String? = "",
    val title: String? = "",
    val coverImagePath: String? = "",
    val authors: List<String>? = emptyList(),
    val categories: List<String>? = emptyList(),
    val description: String? = "",
    val totalChapter: Int? = 0,
    val currentChapter: Int = 0,
    val currentParagraph: Int = 0,
    val isFavorite: Boolean = false,
    val storagePath: String? = "",
    val isEditable: Boolean? = false
)
