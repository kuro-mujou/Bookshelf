package com.capstone.bookshelf.presentation.bookdetail

import com.capstone.bookshelf.domain.book.wrapper.Book

data class BookDetailState(
    val isLoading: Boolean = true,
    val isFavorite: Boolean = false,
    val book: Book? = null
)
