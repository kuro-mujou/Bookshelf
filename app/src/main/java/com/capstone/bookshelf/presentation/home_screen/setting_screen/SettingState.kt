package com.capstone.bookshelf.presentation.home_screen.setting_screen

import android.speech.tts.Voice
import java.util.Locale

data class SettingState(
    val temp: Int = 0,
    val keepScreenOn: Boolean = false,
    val isAutoResumeScrollMode: Boolean = false,
    val openAutoScrollMenu: Boolean = false,
    val openTTSVoiceMenu: Boolean = false,
    val currentPitch: Float = 1f,
    val currentSpeed: Float = 1f,
    val currentLanguage: Locale? = null,
    val currentVoice: Voice? = null,
)