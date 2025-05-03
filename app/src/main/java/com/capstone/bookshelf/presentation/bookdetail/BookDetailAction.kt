package com.capstone.bookshelf.presentation.bookdetail


sealed interface BookDetailAction {
    data class OnDrawerItemClick(val index: Int) : BookDetailAction
    data object OnBookMarkClick : BookDetailAction
}