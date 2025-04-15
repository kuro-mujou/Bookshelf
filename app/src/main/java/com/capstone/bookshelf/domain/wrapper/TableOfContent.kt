package com.capstone.bookshelf.domain.wrapper

data class TableOfContent (
    val tocId: Int? = null,
    val bookId: String,
    val title: String,
    val index: Int,
    val isFavorite: Boolean,
)