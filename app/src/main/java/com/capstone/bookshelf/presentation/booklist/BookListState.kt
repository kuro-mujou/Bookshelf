package com.capstone.bookshelf.presentation.booklist

import com.capstone.bookshelf.domain.wrapper.Book

data class BookListState(
    val isSortedByFavorite: Boolean = false,
    val isOpenBottomSheet: Boolean = false,
    val isOnDeleteBooks: Boolean = false,
    val bookList: List<Book> = emptyList(),
    val selectedBook: Book? = null,
    val isLoading: Boolean = true,
    val isSavingBook: Boolean = false,
    val selectedBookList: List<Book> = emptyList()
)