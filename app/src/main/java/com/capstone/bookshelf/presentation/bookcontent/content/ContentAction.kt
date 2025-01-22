package com.capstone.bookshelf.presentation.bookcontent.content

sealed interface ContentAction {
    data class UpdateFlagTriggerAdjustScroll(val value: Boolean) : ContentAction
    data class UpdateFlagStartScrolling(val value: Boolean): ContentAction
    data class UpdateFlagScrollAdjusted(val value: Boolean): ContentAction
    data class UpdateFlagStartAdjustScroll(val value: Boolean): ContentAction
    data class UpdateFirstVisibleItemIndex(val index: Int) : ContentAction
    data class UpdateLastVisibleItemIndex(val index: Int) : ContentAction
    data class UpdateCurrentChapterIndex(val index: Int) : ContentAction
    data class UpdatePreviousChapterIndex(val index: Int) : ContentAction
    data class UpdateScreenWidth(val value: Int) : ContentAction
    data class UpdateScreenHeight(val value: Int) : ContentAction
    data class UpdateTotalChapter(val value: Int) : ContentAction
    data class UpdateChapterIndexForBook(val index: Int) : ContentAction
}