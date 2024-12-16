package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.speech.tts.Voice
import java.util.Locale

data class TTSState (
    //setup
    val currentSpeed : Float? = null,
    val currentPitch : Float? = null,
    val currentLanguage : Locale? = null,
    val currentVoice : Voice? = null,
    val scrollTime : Int = 0,
    //state
    val isSpeaking : Boolean = false,
    val isPaused : Boolean = false,
    val isFocused : Boolean = false,
    //ui info
    val currentReadingPosition : Int = 0,
    val currentReadingParagraph : Int = 0,

    val currentReadingChapterContent : List<String>? = null,
)