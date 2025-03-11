package com.capstone.bookshelf.presentation.bookcontent.component.autoscroll

sealed interface AutoScrollAction {
    data class UpdateIsStart(val isAutoScroll: Boolean) : AutoScrollAction
    data class UpdateIsPaused(val isPaused: Boolean) : AutoScrollAction
    data class UpdateStopAutoScroll(val stopScroll: Boolean) : AutoScrollAction
    data class UpdateAutoResumeScrollMode(val autoResumeScrollMode: Boolean) : AutoScrollAction
    data class UpdateAutoScrollSpeed(val autoScrollSpeed: Int) : AutoScrollAction
    data class UpdateDelayAtStart(val delayAtStart: Int) : AutoScrollAction
    data class UpdateDelayAtEnd(val delayAtEnd: Int) : AutoScrollAction
    data class UpdateDelayResume(val delayResume: Int) : AutoScrollAction
}