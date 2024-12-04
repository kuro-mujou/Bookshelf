package com.capstone.bookshelf.presentation.main.booklist.local

import com.capstone.bookshelf.domain.book.wrapper.Book

sealed interface LocalBookListAction {
    data class OnBookClick(val book: Book): LocalBookListAction
    data class OnBookLongClick(val book: Book?, val isOpenBottomSheet: Boolean): LocalBookListAction
    data class OnBookDeleteClick(val book: Book): LocalBookListAction
    data class OnBookBookmarkClick(val book: Book): LocalBookListAction
    data class OnBookListBookmarkClick(val isSortListByFavorite: Boolean): LocalBookListAction
    data class OnViewBookDetailClick(val book: Book): LocalBookListAction
    data class OnSaveBook(val save: Boolean): LocalBookListAction
    data class OnBookCheckBoxClick(val checked: Boolean,val book: Book): LocalBookListAction
    data class OnDeletingBooks(val deleteState: Boolean): LocalBookListAction
    data object OnConfirmDeleteBooks : LocalBookListAction
}