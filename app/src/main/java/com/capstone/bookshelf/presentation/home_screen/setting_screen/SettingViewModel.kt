package com.capstone.bookshelf.presentation.home_screen.setting_screen

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.domain.repository.BookRepository
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
    private val bookRepository: BookRepository,
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
                    it.copy(openTTSVoiceMenu = action.open)
                }
            }

            is SettingAction.LoadTTSSetting -> {
                loadTTSSetting()
            }

            is SettingAction.OpenAutoScrollMenu -> {
                _state.update {
                    it.copy(openAutoScrollMenu = action.open)
                }
            }

            is SettingAction.KeepScreenOn -> {
                viewModelScope.launch {
                    dataStoreManager.setKeepScreenOn(action.keepScreenOn)
                }
            }

            is SettingAction.UpdateVoice -> {
                viewModelScope.launch {
                    if (action.voice != null) {
                        dataStoreManager.setTTSVoice(action.voice.name)
                    }
                    _state.update {
                        it.copy(currentVoice = action.voice)
                    }
                }
            }

            is SettingAction.UpdateLanguage -> {
                viewModelScope.launch {
                    dataStoreManager.setTTSLocale(action.language?.displayName.toString())
                    _state.update {
                        it.copy(currentLanguage = action.language)
                    }
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

            is SettingAction.SetupTTS -> {
                setupTTS(action.context)
            }

            is SettingAction.UpdateSelectedBookmarkStyle -> {
                viewModelScope.launch {
                    dataStoreManager.setBookmarkStyle(action.style)
                }
            }

            is SettingAction.OnEnableBackgroundMusicChange -> {
                viewModelScope.launch {
                    dataStoreManager.setEnableBackgroundMusic(action.enable)
                }
            }

            is SettingAction.OnPlayerVolumeChange -> {
                viewModelScope.launch {
                    dataStoreManager.setPlayerVolume(action.volume)
                }
            }

            is SettingAction.UpdateAutoResumeScrollMode -> {
                viewModelScope.launch {
                    dataStoreManager.setAutoScrollResumeMode(action.autoResume)
                }
            }

            is SettingAction.UpdateDelayAtEnd -> {
                viewModelScope.launch {
                    dataStoreManager.setDelayTimeAtEnd(action.delay)
                }
            }

            is SettingAction.UpdateDelayAtStart -> {
                viewModelScope.launch {
                    dataStoreManager.setDelayTimeAtStart(action.delay)
                }
            }

            is SettingAction.UpdateDelayResumeMode -> {
                viewModelScope.launch {
                    dataStoreManager.setAutoScrollResumeDelayTime(action.delay)
                }
            }

            is SettingAction.UpdateScrollSpeed -> {
                viewModelScope.launch {
                    dataStoreManager.setAutoScrollSpeed(action.speed)
                }
            }

            is SettingAction.ChangeChipState -> {
                _state.update {
                    it.copy(
                        bookCategories = it.bookCategories.map { chip ->
                            if (chip.id == action.chip.id) {
                                chip.copy(isSelected = !chip.isSelected)
                            } else {
                                chip
                            }
                        }
                    )
                }
            }

            is SettingAction.AddCategory -> {
                viewModelScope.launch {
                    bookRepository.insertCategory(action.category)
                }
            }

            is SettingAction.DeleteCategory -> {
                viewModelScope.launch {
                    bookRepository.deleteCategory(
                        _state.value.bookCategories.filter {
                            it.isSelected
                        }
                    )
                }
            }

            is SettingAction.ResetChipState -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            bookCategories = it.bookCategories.map { category ->
                                category.copy(isSelected = false)
                            }
                        )
                    }
                }
            }

            is SettingAction.OpenSpecialCodeSuccess -> {
                viewModelScope.launch {
                    dataStoreManager.setUnlockSpecialCodeStatus(true)
                }
            }
        }
    }

    private fun loadTTSSetting() {
        viewModelScope.launch {
            val selectedLocale = _state.value.tts?.availableLanguages?.find {
                it.displayName == dataStoreManager.ttsLocale.first()
            }
            val selectedVoice = _state.value.tts?.voices?.find {
                it.name == dataStoreManager.ttsVoice.first() && it.locale == selectedLocale
            } ?: _state.value.tts?.voices?.firstOrNull {
                it.locale == selectedLocale
            }
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

    fun setupTTS(context: Context) {
        viewModelScope.launch {
            val tts = TextToSpeech(context) {
                if (it == TextToSpeech.SUCCESS) {
                    loadTTSSetting()
                }
            }
            _state.update {
                it.copy(tts = tts)
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
        viewModelScope.launch {
            dataStoreManager.bookmarkStyle.collectLatest { bookmarkStyle ->
                _state.update {
                    it.copy(selectedBookmarkStyle = bookmarkStyle)
                }
            }
        }
        viewModelScope.launch {
            dataStoreManager.enableBackgroundMusic.collectLatest { enableBackgroundMusic ->
                _state.update {
                    it.copy(enableBackgroundMusic = enableBackgroundMusic)
                }
            }
        }
        viewModelScope.launch {
            dataStoreManager.autoScrollSpeed.collectLatest { speed ->
                _state.update {
                    it.copy(currentScrollSpeed = speed)
                }
            }
        }
        viewModelScope.launch {
            dataStoreManager.autoScrollResumeMode.collectLatest { autoScrollResumeMode ->
                _state.update {
                    it.copy(isAutoResumeScrollMode = autoScrollResumeMode)
                }
            }
        }
        viewModelScope.launch {
            dataStoreManager.autoScrollResumeDelayTime.collectLatest { delay ->
                _state.update {
                    it.copy(delayResumeMode = delay)
                }
            }
        }
        viewModelScope.launch {
            dataStoreManager.delayTimeAtEnd.collectLatest { delay ->
                _state.update {
                    it.copy(delayAtEnd = delay)
                }
            }
        }
        viewModelScope.launch {
            dataStoreManager.delayTimeAtStart.collectLatest { delay ->
                _state.update {
                    it.copy(delayAtStart = delay)
                }
            }
        }
        viewModelScope.launch {
            bookRepository.getBookCategory().collectLatest { categories ->
                _state.update {
                    it.copy(bookCategories = categories)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _state.value.tts?.stop()
        _state.value.tts?.shutdown()
    }
}