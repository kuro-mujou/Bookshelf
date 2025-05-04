package com.capstone.bookshelf.presentation.home_screen.setting_screen

import android.speech.tts.TextToSpeech

sealed interface SettingAction {
    data class OpenTTSVoiceMenu(val open: Boolean) : SettingAction
    data class OpenAutoScrollMenu(val open: Boolean) : SettingAction
    data class KeepScreenOn(val keepScreenOn: Boolean) : SettingAction
    data class UpdateSpeed(val speed: Float) : SettingAction
    data class UpdatePitch(val pitch: Float) : SettingAction
    data class FixNullVoice(val tts: TextToSpeech) : SettingAction
    data class LoadTTSSetting(val tts: TextToSpeech) : SettingAction
}