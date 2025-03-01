package com.capstone.bookshelf.presentation.bookcontent.component.autoscroll

data class AutoScrollState(
    val isStart : Boolean = false,
    val isPaused : Boolean = false,
    val isAutoResumeScrollMode : Boolean = false,
    val currentSpeed : Int = 10000,
    val delayAtStart: Int = 3000,
    val delayAtEnd: Int = 3000,
    val delayResumeMode: Int = 1000,
)
