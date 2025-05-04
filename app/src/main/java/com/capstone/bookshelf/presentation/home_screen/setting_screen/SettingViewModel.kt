package com.capstone.bookshelf.presentation.home_screen.setting_screen

import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.domain.repository.MusicPathRepository
import com.capstone.bookshelf.util.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingViewModel(
    private val musicPathRepository: MusicPathRepository,
    private val dataStoreManager: DataStoreManager,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _state.value
        )

    fun onAction(action: SettingAction) {
        when (action) {
            is SettingAction.OpenTTSVoiceMenu -> {
                _state.update {
                    it.copy(
                        openTTSVoiceMenu = action.open
                    )
                }
            }

            is SettingAction.OpenAutoScrollMenu -> {
                _state.update {
                    it.copy(
                        openAutoScrollMenu = action.open
                    )
                }
            }

            is SettingAction.KeepScreenOn -> {
                viewModelScope.launch {
                    dataStoreManager.setKeepScreenOn(action.keepScreenOn)
                }
            }
            is SettingAction.UpdateSpeed -> {
                viewModelScope.launch {
                    dataStoreManager.setTTSSpeed(action.speed)
                }
            }
            is SettingAction.UpdatePitch -> {
                viewModelScope.launch {
                    dataStoreManager.setTTSPitch(action.pitch)
                }
            }
            is SettingAction.FixNullVoice -> {
                fixNullVoice(action.tts)
            }
            is SettingAction.LoadTTSSetting -> {
                loadTTSSetting(action.tts)
            }
        }
    }

    private fun loadTTSSetting(tts: TextToSpeech) {
        Log.d("SettingViewModel", "loadTTSSetting called")
        Log.d("SettingViewModel", "tts: $tts")
        viewModelScope.launch {
            Log.d("SettingViewModel", "ttsLocale: ${dataStoreManager.ttsLocale.first()}")
            Log.d("SettingViewModel", "ttsVoice: ${dataStoreManager.ttsVoice.first()}")
            Log.d("SettingViewModel", "availableLanguages: ${tts.availableLanguages}")
            val selectedLocale = tts.availableLanguages?.find {
                it.displayName == dataStoreManager.ttsLocale.first()
            }
            val selectedVoice = tts.voices?.find {
                it.name == dataStoreManager.ttsVoice.first() && it.locale == selectedLocale
            } ?: tts.voices?.firstOrNull {
                it.locale == selectedLocale
            }
            Log.d("SettingViewModel", "selectedLocale: ${selectedLocale?.displayName}")
            Log.d("SettingViewModel", "selectedVoice: ${selectedVoice?.name}")
            _state.update {
                it.copy(
                    currentLanguage = selectedLocale,
                    currentVoice = selectedVoice
                )
            }
        }
    }

    private fun fixNullVoice(textToSpeech: TextToSpeech) {
        viewModelScope.launch {
            var selectedVoice = textToSpeech.voices?.find {
                it.locale == _state.value.currentLanguage
            }
            if (selectedVoice == null) {
                selectedVoice = textToSpeech.voices?.firstOrNull {
                    it.locale == _state.value.currentLanguage
                } ?: textToSpeech.defaultVoice
            }
            dataStoreManager.setTTSVoice(selectedVoice.name)
            _state.update {
                it.copy(currentVoice = selectedVoice)
            }
        }
    }

    init {
        viewModelScope.launch {
            dataStoreManager.keepScreenOn.collectLatest { keepScreenOn ->
                _state.update {
                    it.copy(keepScreenOn = keepScreenOn)
                }
            }
        }
        viewModelScope.launch {
            dataStoreManager.ttsPitch.collectLatest { ttsPitch ->
                _state.update {
                    it.copy(currentPitch = ttsPitch)
                }
            }
        }
        viewModelScope.launch {
            dataStoreManager.ttsSpeed.collectLatest { ttsSpeed ->
                _state.update {
                    it.copy(currentSpeed = ttsSpeed)
                }
            }
        }
    }
}