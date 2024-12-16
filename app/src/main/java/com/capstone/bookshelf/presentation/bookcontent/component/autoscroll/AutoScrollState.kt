package com.capstone.bookshelf.presentation.bookcontent.component.autoscroll

data class AutoScrollState(
    val isAutoScroll : Boolean = false,
    val isAutoScrollPaused : Boolean = false,
    val currentSpeed : Float = 1f,
)
