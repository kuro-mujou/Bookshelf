package com.capstone.bookshelf.presentation.bookcontent.component.autoscroll

data class AutoScrollState(
    val isStart : Boolean = false,
    val isPaused : Boolean = false,
    val currentSpeed : Int = 10000,
)
