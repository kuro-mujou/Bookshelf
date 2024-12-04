package com.capstone.bookshelf.presentation.main.booklist

import com.capstone.bookshelf.domain.book.wrapper.Book

interface LibraryAction {
    data class OnTabSelected(val index: Int) : LibraryAction
    data class OnBookClick(val book: Book) : LibraryAction
}