package com.capstone.bookshelf.presentation.bookdetail

import com.capstone.bookshelf.domain.book.wrapper.Book


sealed interface BookDetailAction {
    data object OnBackClick: BookDetailAction
    data object OnFavoriteClick: BookDetailAction
    data class OnSelectedBookChange(val book: Book): BookDetailAction
}