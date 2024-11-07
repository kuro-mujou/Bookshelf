package com.capstone.bookshelf.feature.readbook.presentation.state

data class TTSState(
    val isSpeaking : Boolean = false,
    val isPaused : Boolean = false,
    val isFocused : Boolean = false,
    val isStop : Boolean = true,
    val scrollTime : Int = 0,
    val currentReadingParagraph : Int = 0,
    val firstVisibleItemIndex : Int = 0,
    val lastVisibleItemIndex : Int = 0,
    val flagTriggerScrolling : Boolean = false,
    val flagStartScrolling : Boolean = false,
    val flagScrollAdjusted : Boolean = false,
    val flagTriggerAdjustScroll : Boolean = false,
    val flagStartAdjustScroll : Boolean = false,
)
