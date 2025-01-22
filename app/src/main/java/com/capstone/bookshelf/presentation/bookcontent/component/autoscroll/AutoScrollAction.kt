package com.capstone.bookshelf.presentation.bookcontent.component.autoscroll

sealed interface AutoScrollAction {
    data class UpdateIsStart(val isAutoScroll: Boolean) : AutoScrollAction
    data class UpdateAutoScrollSpeed(val autoScrollSpeed: Int) : AutoScrollAction
    data class UpdateIsPaused(val isPaused: Boolean) : AutoScrollAction
}