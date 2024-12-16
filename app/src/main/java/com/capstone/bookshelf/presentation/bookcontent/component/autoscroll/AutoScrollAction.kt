package com.capstone.bookshelf.presentation.bookcontent.component.autoscroll

sealed interface AutoScrollAction {
    data class UpdateIsAutoScroll(val isAutoScroll: Boolean) : AutoScrollAction
}