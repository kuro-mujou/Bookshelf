package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.data.book.database.entity.BookSettingEntity
import com.capstone.bookshelf.domain.book.BookSettingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class TTSViewModel(
    private val bookSettingRepository: BookSettingRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TTSState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )
    fun onAction(action: TTSAction) {
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
                    bookSettingRepository.updateBookSettingLocale(0,action.currentLanguage.displayName)
                }
            }
            is TTSAction.UpdateTTSPitch -> {
                _state.value = _state.value.copy(
                    currentPitch = action.currentPitch
                )
                viewModelScope.launch {
                    bookSettingRepository.updateBookSettingPitch(0,action.currentPitch)
                }
            }
            is TTSAction.UpdateTTSSpeed -> {
                _state.value = _state.value.copy(
                    currentSpeed = action.currentSpeed
                )
                viewModelScope.launch {
                    bookSettingRepository.updateBookSettingSpeed(0,action.currentSpeed)
                }
            }
            is TTSAction.UpdateTTSVoice -> {
                _state.value = _state.value.copy(
                    currentVoice = action.currentVoice
                )
                viewModelScope.launch {
                    bookSettingRepository.updateBookSettingVoice(0,action.currentVoice.name)
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
        fun loadTTSSetting(textToSpeech: TextToSpeech) {
            viewModelScope.launch {
                val setting = bookSettingRepository.getBookSetting(0)
                if (setting != null) {
                    val selectedLocale = textToSpeech.availableLanguages.find {
                        it.displayName == setting.ttsLocale
                    } ?: Locale.getDefault()

                    var selectedVoice = textToSpeech.voices.find {
                        it.name == setting.ttsVoice && it.locale == selectedLocale
                    }

                    if (selectedVoice == null) {
                        selectedVoice = textToSpeech.voices.firstOrNull {
                            it.locale == selectedLocale
                        } ?: textToSpeech.defaultVoice
                    }
                    onAction(TTSAction.UpdateTTSPitch(setting.pitch ?: 1f))
                    onAction(TTSAction.UpdateTTSSpeed(setting.speed ?: 1f))
                    onAction(TTSAction.UpdateTTSLanguage(selectedLocale))
                    onAction(TTSAction.UpdateTTSVoice(selectedVoice!!))
                } else {
                    val newSetting = BookSettingEntity(settingId = 0)
                    bookSettingRepository.saveBookSetting(newSetting)
                }
            }
        }
        fun fixNullVoice(textToSpeech: TextToSpeech){
            viewModelScope.launch {
                var selectedVoice = textToSpeech.voices.find {
                    it.locale == _state.value.currentLanguage
                }
                if (selectedVoice == null) {
                    selectedVoice = textToSpeech.voices.firstOrNull {
                        it.locale == _state.value.currentLanguage
                    } ?: textToSpeech.defaultVoice
                }
                onAction(TTSAction.UpdateTTSVoice(selectedVoice!!))
            }
        }
    }
}