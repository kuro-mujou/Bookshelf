package com.capstone.bookshelf.presentation.main.booklist.local

import com.capstone.bookshelf.core.presentation.UiText
import com.capstone.bookshelf.domain.book.wrapper.Book

data class LocalBookListState(
    val isSortedByFavorite: Boolean = false,
    val isOpenBottomSheet: Boolean = false,
    val isOnDeleteBooks: Boolean = false,
    val bookList: List<Book> = emptyList(),
    val selectedBook: Book? = null,
    val isLoading: Boolean = true,
    val errorMessage: UiText? = null,
    val isSavingBook: Boolean = false,
    val selectedBookList: List<Book> = emptyList()
)