package com.capstone.bookshelf.presentation.bookcontent

import com.capstone.bookshelf.domain.wrapper.Book

sealed interface  BookContentRootAction {
    data class SelectedBook(val book: Book): BookContentRootAction
}