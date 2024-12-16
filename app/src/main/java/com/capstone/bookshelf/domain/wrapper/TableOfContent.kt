package com.capstone.bookshelf.domain.wrapper

data class TableOfContent (
    val tocId: Int,
    val bookId: String,
    val title: String,
    val index: Int
)