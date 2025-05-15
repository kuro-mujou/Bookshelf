package com.capstone.bookshelf.presentation.bookdetail

import com.capstone.bookshelf.domain.wrapper.Category


sealed interface BookDetailAction {
    data class OnDrawerItemClick(val index: Int) : BookDetailAction
    data object OnBookMarkClick : BookDetailAction
    data class ChangeChipState(val category: Category) : BookDetailAction
}