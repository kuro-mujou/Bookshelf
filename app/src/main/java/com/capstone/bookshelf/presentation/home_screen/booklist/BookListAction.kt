package com.capstone.bookshelf.presentation.home_screen.booklist

import com.capstone.bookshelf.domain.wrapper.Book
import com.capstone.bookshelf.domain.wrapper.Category

sealed interface BookListAction {
    data class OnBookLongClick(val book: Book?, val isOpenBottomSheet: Boolean) : BookListAction
    data class OnBookDeleteClick(val book: Book) : BookListAction
    data class OnBookBookmarkClick(val book: Book) : BookListAction
    data class OnBookListBookmarkClick(val isSortListByFavorite: Boolean) : BookListAction
    data class OnViewBookDetailClick(val book: Book) : BookListAction
    data class OnBookCheckBoxClick(val checked: Boolean, val book: Book) : BookListAction
    data class OnDeletingBooks(val deleteState: Boolean) : BookListAction
    data class UpdateBookListType(val type: Int? = null) : BookListAction
    data class OnBookClick(val book: Book) : BookListAction
    data object OnConfirmDeleteBooks : BookListAction
    data class ChangeChipState(val chip: Category) : BookListAction
    data object ResetChipState : BookListAction
}