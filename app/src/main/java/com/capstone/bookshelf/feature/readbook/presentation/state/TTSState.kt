package com.capstone.bookshelf.feature.readbook.presentation.state

import android.speech.tts.Voice
import java.util.Locale

data class TTSState(
    val readingContent : List<String> = emptyList(),
    val isSpeaking : Boolean = false,
    val isPaused : Boolean = false,
    val isFocused : Boolean = false,
    val isAutoScroll : Boolean = false,
    val isAutoScrollPaused : Boolean = false,
    val scrollTime : Int = 0,
    val currentReadingParagraph : Int = 0,
    val firstVisibleItemIndex : Int = 0,
    val lastVisibleItemIndex : Int = 0,
    val flagTriggerScrolling : Boolean = false,
    val flagStartScrolling : Boolean = false,
    val flagScrollAdjusted : Boolean = false,
    val flagTriggerAdjustScroll : Boolean = false,
    val flagStartAdjustScroll : Boolean = false,
    val currentSpeed : Float? = null,
    val currentPitch : Float? = null,
    val currentLanguage : Locale? = null,
    val currentVoice : Voice? = null,
    val autoScrollSpeed : Float? = null,
)
