package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.speech.tts.Voice
import java.util.Locale

sealed interface TTSAction {
    data class UpdateIsSpeaking(val isSpeaking: Boolean) : TTSAction
    data class UpdateIsPaused(val isPaused: Boolean) : TTSAction
    data class UpdateIsFocused(val isFocused: Boolean) : TTSAction
    data class UpdateTTSLanguage(val currentLanguage: Locale) : TTSAction
    data class UpdateTTSVoice(val currentVoice: Voice) : TTSAction
    data class UpdateTTSSpeed(val currentSpeed: Float) : TTSAction
    data class UpdateTTSPitch(val currentPitch: Float) : TTSAction
    data class UpdateCurrentChapterContent(val strings: List<String>?): TTSAction
    data class UpdateCurrentReadingParagraph(val pos: Int): TTSAction
}