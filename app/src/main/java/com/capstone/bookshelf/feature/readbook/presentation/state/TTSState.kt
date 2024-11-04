package com.capstone.bookshelf.feature.readbook.presentation.state

data class TTSState(
    val isSpeaking : Boolean = false,
    val isPaused : Boolean = false,
    val isFocused : Boolean = false,
    val isStop : Boolean = true,
    val scrollTime : Int = 0,
    val currentReadingParagraph : Int = 0
)
