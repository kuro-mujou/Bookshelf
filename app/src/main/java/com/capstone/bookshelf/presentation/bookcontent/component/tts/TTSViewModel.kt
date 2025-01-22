package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.util.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class TTSViewModel : ViewModel() {
    private val _state = MutableStateFlow(TTSState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )
    fun onAction(dataStoreManager: DataStoreManager, action: TTSAction) {
        when (action) {
            is TTSAction.UpdateIsSpeaking -> {
                _state.value = _state.value.copy(
                    isSpeaking = action.isSpeaking
                )
            }
            is TTSAction.UpdateIsFocused -> {
                _state.value = _state.value.copy(
                    isFocused = action.isFocused
                )
            }
            is TTSAction.UpdateIsPaused -> {
                _state.value = _state.value.copy(
                    isPaused = action.isPaused
                )
            }

            is TTSAction.UpdateTTSLanguage -> {
                _state.value = _state.value.copy(
                    currentLanguage = action.currentLanguage
                )
                viewModelScope.launch {
                    dataStoreManager.setTTSLocale(action.currentLanguage.displayName)
                }
            }
            is TTSAction.UpdateTTSPitch -> {
                _state.value = _state.value.copy(
                    currentPitch = action.currentPitch
                )
                viewModelScope.launch {
                    dataStoreManager.setTTSPitch(action.currentPitch)
                }
            }
            is TTSAction.UpdateTTSSpeed -> {
                _state.value = _state.value.copy(
                    currentSpeed = action.currentSpeed
                )
                viewModelScope.launch {
                    dataStoreManager.setTTSSpeed(action.currentSpeed)
                }
            }
            is TTSAction.UpdateTTSVoice -> {
                _state.value = _state.value.copy(
                    currentVoice = action.currentVoice
                )
                if(action.currentVoice != null) {
                    viewModelScope.launch {
                        dataStoreManager.setTTSVoice(action.currentVoice.name)
                    }
                }
            }

            is TTSAction.UpdateCurrentChapterContent -> {
                _state.value = _state.value.copy(
                    currentReadingChapterContent = action.strings
                )
            }

            is TTSAction.UpdateCurrentReadingParagraph -> {
                _state.value = _state.value.copy(
                    currentReadingParagraph = action.pos
                )
            }
        }
    }
    fun loadTTSSetting(dataStoreManager: DataStoreManager, textToSpeech: TextToSpeech) {
        viewModelScope.launch {
            val selectedLocale = textToSpeech.availableLanguages.find {
                it.displayName == dataStoreManager.ttsLocale.first()
            } ?: Locale.getDefault()

            var selectedVoice = textToSpeech.voices.find {
                it.name == dataStoreManager.ttsVoice.first() && it.locale == selectedLocale
            }

            if (selectedVoice == null) {
                selectedVoice = textToSpeech.voices.firstOrNull {
                    it.locale == selectedLocale
                } ?: textToSpeech.defaultVoice
            }
            onAction(dataStoreManager,TTSAction.UpdateTTSPitch(dataStoreManager.ttsPitch.first()))
            onAction(dataStoreManager,TTSAction.UpdateTTSSpeed(dataStoreManager.ttsSpeed.first()))
            onAction(dataStoreManager,TTSAction.UpdateTTSLanguage(selectedLocale))
            onAction(dataStoreManager,TTSAction.UpdateTTSVoice(selectedVoice!!))
        }
    }
    fun fixNullVoice(dataStoreManager: DataStoreManager, textToSpeech: TextToSpeech){
        viewModelScope.launch {
            var selectedVoice = textToSpeech.voices.find {
                it.locale == _state.value.currentLanguage
            }
            if (selectedVoice == null) {
                selectedVoice = textToSpeech.voices.firstOrNull {
                    it.locale == _state.value.currentLanguage
                } ?: textToSpeech.defaultVoice
            }
            onAction(dataStoreManager, TTSAction.UpdateTTSVoice(selectedVoice!!))
        }
    }
}