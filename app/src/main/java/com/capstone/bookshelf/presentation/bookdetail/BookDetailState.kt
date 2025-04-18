package com.capstone.bookshelf.presentation.bookdetail

import com.capstone.bookshelf.domain.wrapper.Book
import com.capstone.bookshelf.domain.wrapper.TableOfContent

data class BookDetailState(
    val isLoading: Boolean = true,
    val isFavorite: Boolean = false,
    val book: Book? = null,
    val tableOfContents: List<TableOfContent> = emptyList(),
    val isSortedByFavorite: Boolean = false,
)
