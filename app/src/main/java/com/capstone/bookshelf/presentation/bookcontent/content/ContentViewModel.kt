package com.capstone.bookshelf.presentation.bookcontent.content

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextMeasurer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.capstone.bookshelf.app.Route
import com.capstone.bookshelf.domain.book.BookRepository
import com.capstone.bookshelf.domain.book.ChapterRepository
import com.capstone.bookshelf.domain.wrapper.Chapter
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSService
import com.capstone.bookshelf.util.DataStoreManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.Locale

class ContentViewModel(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val bookId = savedStateHandle.toRoute<Route.BookContent>().bookId
    private var ttsServiceConnection: ServiceConnection? = null
    var serviceBinder: TTSService.TTSBinder? = null
    private val _chapterContent: MutableState<Chapter?> = mutableStateOf(null)
    val chapterContent: State<Chapter?> = _chapterContent
    private val _state = MutableStateFlow(ContentState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onContentAction(dataStoreManager : DataStoreManager, action: ContentAction) {
        when(action){
            is ContentAction.UpdateFlagTriggerAdjustScroll -> {
                _state.value = _state.value.copy(
                    flagTriggerAdjustScroll = action.value
                )
            }
            is ContentAction.UpdateFlagStartScrolling -> {
                _state.value = _state.value.copy(
                    flagStartScrolling = action.value
                )
            }
            is ContentAction.UpdateFlagScrollAdjusted -> {
                _state.value = _state.value.copy(
                    flagScrollAdjusted = action.value
                )
            }
            is ContentAction.UpdateFlagStartAdjustScroll -> {
                _state.value = _state.value.copy(
                    flagStartAdjustScroll = action.value
                )
            }
            is ContentAction.UpdateFirstVisibleItemIndex -> {
                _state.value = _state.value.copy(
                    firstVisibleItemIndex = action.index
                )
                serviceBinder?.setFirstVisibleItemIndex(action.index)
            }
            is ContentAction.UpdateLastVisibleItemIndex -> {
                _state.value = _state.value.copy(
                    lastVisibleItemIndex = action.index
                )
            }
            is ContentAction.UpdateCurrentChapterIndex -> {
                _state.value = _state.value.copy(
                    currentChapterIndex = action.index
                )
                serviceBinder?.setCurrentChapterIndex(action.index)
            }
            is ContentAction.UpdateScreenHeight -> {
                _state.value = _state.value.copy(
                    screenHeight = action.value
                )
            }
            is ContentAction.UpdateScreenWidth -> {
                _state.value = _state.value.copy(
                    screenWidth = action.value
                )
            }
            is ContentAction.UpdatePreviousChapterIndex -> {
                _state.value = _state.value.copy(
                    previousChapterIndex = action.index
                )
            }
            is ContentAction.UpdateBookInfo -> {
                viewModelScope.launch {
                    bookRepository.saveBookInfo(bookId, action.index)
                }
            }
            is ContentAction.UpdateFlagTriggerScrolling -> {
                _state.value = _state.value.copy(
                    flagTriggerScrolling = action.value
                )
                serviceBinder?.setFlagTriggerScroll(action.value)
            }
            is ContentAction.UpdateChapterHeader -> {
                serviceBinder?.setChapterTitle(action.header)
            }
            is ContentAction.UpdateIsSpeaking -> {
                _state.value = _state.value.copy(
                    isSpeaking = action.isSpeaking
                )
                serviceBinder?.setIsSpeaking(action.isSpeaking)
            }
            is ContentAction.UpdateIsFocused -> {
                _state.value = _state.value.copy(
                    isFocused = action.isFocused
                )
            }
            is ContentAction.UpdateIsPaused -> {
                _state.value = _state.value.copy(
                    isPaused = action.isPaused
                )
                serviceBinder?.setIsPaused(action.isPaused)
            }
            is ContentAction.UpdateTTSLanguage -> {
                _state.value = _state.value.copy(
                    currentLanguage = action.currentLanguage
                )
                viewModelScope.launch {
                    dataStoreManager.setTTSLocale(action.currentLanguage.displayName)
                }
                serviceBinder?.setCurrentLanguage(action.currentLanguage)
                serviceBinder?.updateTTS()
            }
            is ContentAction.UpdateTTSPitch -> {
                _state.value = _state.value.copy(
                    currentPitch = action.currentPitch
                )
                viewModelScope.launch {
                    dataStoreManager.setTTSPitch(action.currentPitch)
                }
                serviceBinder?.setCurrentPitch(action.currentPitch)
                serviceBinder?.updateTTS()
            }
            is ContentAction.UpdateTTSSpeed -> {
                _state.value = _state.value.copy(
                    currentSpeed = action.currentSpeed
                )
                viewModelScope.launch {
                    dataStoreManager.setTTSSpeed(action.currentSpeed)
                }
                serviceBinder?.setCurrentSpeed(action.currentSpeed)
                serviceBinder?.updateTTS()
            }
            is ContentAction.UpdateTTSVoice -> {
                _state.value = _state.value.copy(
                    currentVoice = action.currentVoice
                )
                if(action.currentVoice != null) {
                    viewModelScope.launch {
                        dataStoreManager.setTTSVoice(action.currentVoice.name)
                    }
                }
                serviceBinder?.setCurrentVoice(action.currentVoice)
                serviceBinder?.updateTTS()
            }
            is ContentAction.UpdateCurrentReadingParagraph -> {
                _state.value = _state.value.copy(
                    currentReadingParagraph = action.pos
                )
                serviceBinder?.setCurrentParagraphIndex(action.pos)
            }
            is ContentAction.UpdateScrollTime -> {
                _state.value = _state.value.copy(
                    scrollTime = action.scrollTimes
                )
                serviceBinder?.setScrollTimes(action.scrollTimes)
            }
            is ContentAction.UpdateFontSize -> {
                _state.value = _state.value.copy(
                    fontSize = action.fontSize
                )
                serviceBinder?.setFontSize(action.fontSize)
            }
            is ContentAction.UpdateLineSpacing -> {
                _state.value = _state.value.copy(
                    lineSpacing = action.lineSpacing
                )
                serviceBinder?.setLineSpacing(action.lineSpacing)
            }
            is ContentAction.UpdateSelectedFontFamilyIndex -> {
                _state.value = _state.value.copy(
                    selectedFontFamilyIndex = action.index
                )
                serviceBinder?.setFontFamily(_state.value.fontFamilies[action.index])
            }
            is ContentAction.UpdateTextAlign -> {
                _state.value = _state.value.copy(
                    textAlign = action.textAlign
                )
                serviceBinder?.setTextAlign(action.textAlign)
            }
            is ContentAction.UpdateTextIndent -> {
                _state.value = _state.value.copy(
                    textIndent = action.textIndent
                )
                serviceBinder?.setTextIndent(action.textIndent)
            }
            is ContentAction.SelectedBook -> {
                _state.update { it.copy(
                    book = action.book
                )}
            }

            is ContentAction.UpdateKeepScreenOn -> {
                _state.value = _state.value.copy(
                    keepScreenOn = action.keepScreenOn
                )
            }
        }
    }
    suspend fun getChapter(page: Int) {
        _chapterContent.value = chapterRepository.getChapterContent(bookId, page)
    }

    fun loadTTSSetting(dataStoreManager: DataStoreManager, tts: TextToSpeech) {
        viewModelScope.launch {
            val selectedLocale = tts.availableLanguages?.find {
                it.displayName == dataStoreManager.ttsLocale.first()
            } ?: Locale.getDefault()

            var selectedVoice = tts.voices?.find {
                it.name == dataStoreManager.ttsVoice.first() && it.locale == selectedLocale
            }

            if (selectedVoice == null) {
                selectedVoice = tts.voices?.firstOrNull {
                    it.locale == selectedLocale
                } ?: tts.defaultVoice
            }
            onContentAction(dataStoreManager, ContentAction.UpdateTTSPitch(dataStoreManager.ttsPitch.first()))
            onContentAction(dataStoreManager, ContentAction.UpdateTTSSpeed(dataStoreManager.ttsSpeed.first()))
            onContentAction(dataStoreManager, ContentAction.UpdateTTSLanguage(selectedLocale))
            onContentAction(dataStoreManager, ContentAction.UpdateTTSVoice(selectedVoice!!))
        }
    }
    fun fixNullVoice(dataStoreManager: DataStoreManager, textToSpeech: TextToSpeech){
        viewModelScope.launch {
            var selectedVoice = textToSpeech.voices?.find {
                it.locale == _state.value.currentLanguage
            }
            if (selectedVoice == null) {
                selectedVoice = textToSpeech.voices?.firstOrNull {
                    it.locale == _state.value.currentLanguage
                } ?: textToSpeech.defaultVoice
            }
            onContentAction(dataStoreManager, ContentAction.UpdateTTSVoice(selectedVoice!!))
        }
    }

    fun startTTSService(context: Context , textMeasurer: TextMeasurer) {
        if (!state.value.isServiceBound) {
            val serviceIntent = Intent(context, TTSService::class.java).apply {
                action = TTSService.ACTION_START_TTS_SERVICE // Use the action constant
            }
            ttsServiceConnection = object : ServiceConnection { // Store the connection
                override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                    viewModelScope.launch {
                        binder as TTSService.TTSBinder
                        serviceBinder = binder
                        _state.update {
                            it.copy(
                                service = binder.getService(),
                            )
                        }
                        binder.initializeTts()
                        binder.loadImage(_state.value.book?.coverImagePath!!)
                        binder.setTextMeasure(textMeasurer)
                        binder.setCurrentChapterIndex(_state.value.currentChapterIndex)
                        binder.setCurrentParagraphIndex(_state.value.currentReadingParagraph)
                        binder.setIsSpeaking(_state.value.isSpeaking)
                        binder.setIsPaused(_state.value.isPaused)
                        binder.setScrollTimes(_state.value.scrollTime)
                        binder.setFlagTriggerScroll(_state.value.flagTriggerScrolling)
                        binder.setTotalChapter(_state.value.book?.totalChapter!!)
                        binder.setBookTitle(_state.value.book?.title!!)
                        binder.setScreenWidth(_state.value.screenWidth)
                        binder.setScreenHeight(_state.value.screenHeight)
                        coroutineScope {
                            val jobs = listOf(
                                launch {
                                    binder.currentParagraphIndex().collectLatest { currentReadingParagraph ->
                                        _state.update { it.copy(currentReadingParagraph = currentReadingParagraph) }
                                    }
                                },
                                launch {
                                    binder.currentChapterIndex().collectLatest { currentChapterIndex ->
                                        _state.update { it.copy(currentChapterIndex = currentChapterIndex) }
                                    }
                                },
                                launch {
                                    binder.isSpeaking().collectLatest { isSpeaking ->
                                        _state.update { it.copy(isSpeaking = isSpeaking) }
                                    }
                                },
                                launch {
                                    binder.isPaused().collectLatest { isPaused ->
                                        _state.update { it.copy(isPaused = isPaused, isServiceBound = true) } }
                                },
                                launch {
                                    binder.scrollTimes().collectLatest { scrollTimes ->
                                        _state.update { it.copy(scrollTime = scrollTimes) }
                                    }
                                },
                                launch {
                                    binder.flagTriggerScroll().collectLatest { flagTriggerScroll ->
                                        _state.update { it.copy(flagTriggerScrolling = flagTriggerScroll) }
                                    }
                                }
                            )
                            jobs.joinAll() // Wait for all collectLatest blocks to start
                        }
                    }
                }
                override fun onServiceDisconnected(name: ComponentName?) {
                    viewModelScope.launch {
                        _state.update {
                            it.copy(
                                isServiceBound = false
                            )
                        }
                    }
                }
            }
            context.startService(serviceIntent)
            context.bindService(serviceIntent, ttsServiceConnection!!, Context.BIND_AUTO_CREATE)
        }
    }

    fun stopTTSService(context: Context) {
        if (_state.value.isServiceBound && ttsServiceConnection != null) {
            _state.value.service?.shutdownTts()
            val serviceIntent = Intent(context, TTSService::class.java)
            context.unbindService(ttsServiceConnection!!)
            context.stopService(serviceIntent)
            viewModelScope.launch {
                _state.update {
                    it.copy(
                        isServiceBound = false,
                        service = null
                    )
                }
            }
            ttsServiceConnection = null
        }
    }

    fun setupTTS(context : Context){
        viewModelScope.launch {
            _state.update {
                it.copy(
                    tts = TextToSpeech(context,null)
                )
            }
        }
    }

    fun stopTTS(){
        _state.value.tts?.stop()
        _state.value.tts?.shutdown()
    }
}