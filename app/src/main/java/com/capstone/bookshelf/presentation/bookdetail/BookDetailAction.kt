package com.capstone.bookshelf.presentation.bookdetail

import com.capstone.bookshelf.domain.wrapper.Book


sealed interface BookDetailAction {
    data class OnSelectedBookChange(val book: Book) : BookDetailAction
    data class OnDrawerItemClick(val index: Int) : BookDetailAction

    data object OnBookMarkClick : BookDetailAction
}