package com.capstone.bookshelf.presentation.bookcontent.content

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextMeasurer
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.uri.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.toRoute
import com.capstone.bookshelf.app.Route
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.domain.repository.ChapterRepository
import com.capstone.bookshelf.domain.repository.MusicPathRepository
import com.capstone.bookshelf.domain.wrapper.Chapter
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSService
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSServiceHandler
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TtsPlayerEvent
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TtsUiEvent
import com.capstone.bookshelf.util.DataStoreManager
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@UnstableApi
class ContentViewModel(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val musicPathRepository: MusicPathRepository,
    private val ttsServiceHandler : TTSServiceHandler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val bookId = savedStateHandle.toRoute<Route.BookContent>().bookId
    private val _chapterContent: MutableState<Chapter?> = mutableStateOf(null)
    val chapterContent: State<Chapter?> = _chapterContent
    private val serviceJob = mutableListOf<Job>()
    private val mediaItemList = mutableListOf<MediaItem>()
    private var _state = MutableStateFlow(ContentState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )
    init {
        viewModelScope.launch {
            musicPathRepository.getMusicPaths()
                .collectLatest { sortedMusicItems ->
                    val selectedItems = sortedMusicItems.filter { it.isSelected }
                    mediaItemList.clear()
                    mediaItemList.addAll(selectedItems.map { media3Item ->
                        MediaItem.Builder()
                            .setUri(media3Item.uri)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setArtworkUri(_state.value.book?.coverImagePath!!.toUri())
                                    .setTitle(_state.value.book?.title)
                                    .setArtist(_state.value.chapterHeader)
                                    .build()
                            )
                            .build()
                    })
                    mediaController?.apply {
                        setMediaItems(mediaItemList)
                        prepare()
                        if(_state.value.enableBackgroundMusic)
                            play()
                    }
                }
        }
    }
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
            }
            is ContentAction.UpdateLastVisibleItemIndex -> {
                _state.value = _state.value.copy(
                    lastVisibleItemIndex = action.index
                )
            }
            is ContentAction.UpdateCurrentChapterIndex -> {
                viewModelScope.launch {
                    _state.value = _state.value.copy(
                        currentChapterIndex = action.index
                    )
                    if(mediaController?.isPlaying == true){
                        mediaController?.apply {
                            val chapter = chapterRepository.getChapterContent(bookId,action.index)
                            val updatedMetadata = currentMediaItem?.mediaMetadata?.buildUpon()
                                ?.setArtist(chapter?.chapterTitle)?.build()!!
                            val updatedMediaItem =
                                currentMediaItem?.buildUpon()?.setMediaMetadata(updatedMetadata)
                                    ?.build()!!
                            replaceMediaItem(currentMediaItemIndex, updatedMediaItem)
                            prepare()
                            play()
                        }
                    }
                    ttsServiceHandler.updateCurrentChapterIndex(action.index)
                }
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
            is ContentAction.UpdateBookInfoCurrentChapterIndex -> {
                viewModelScope.launch {
                    bookRepository.saveBookInfoChapterIndex(bookId, action.index)
                }
            }
            is ContentAction.UpdateBookInfoFirstParagraphIndex -> {
                viewModelScope.launch {
                    bookRepository.saveBookInfoParagraphIndex(bookId, action.index)
                }
            }
            is ContentAction.UpdateChapterHeader -> {
                _state.value = _state.value.copy(
                    chapterHeader = action.header
                )
            }
            is ContentAction.UpdateIsSpeaking -> {
                _state.value = _state.value.copy(
                    isSpeaking = action.isSpeaking
                )
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
                onTtsUiEvent(TtsUiEvent.PlayPause(action.isPaused))
            }
            is ContentAction.UpdateTTSLanguage -> {
                _state.value = _state.value.copy(
                    currentLanguage = action.currentLanguage
                )
                viewModelScope.launch {
                    dataStoreManager.setTTSLocale(action.currentLanguage.displayName)
                }
                ttsServiceHandler.updateTTSLanguage(action.currentLanguage)
            }
            is ContentAction.UpdateTTSPitch -> {
                _state.value = _state.value.copy(
                    currentPitch = action.currentPitch
                )
                viewModelScope.launch {
                    dataStoreManager.setTTSPitch(action.currentPitch)
                }
                ttsServiceHandler.updateTTSPitch(action.currentPitch)
            }
            is ContentAction.UpdateTTSSpeed -> {
                _state.value = _state.value.copy(
                    currentSpeed = action.currentSpeed
                )
                viewModelScope.launch {
                    dataStoreManager.setTTSSpeed(action.currentSpeed)
                }
                ttsServiceHandler.updateTTSSpeed(action.currentSpeed)
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
                ttsServiceHandler.updateTTSVoice(action.currentVoice)
            }
            is ContentAction.UpdateCurrentReadingParagraph -> {
                _state.value = _state.value.copy(
                    currentReadingParagraph = action.pos
                )
            }
            is ContentAction.UpdateFontSize -> {
                _state.value = _state.value.copy(
                    fontSize = action.fontSize
                )
                ttsServiceHandler.fontSizeTTS = action.fontSize
            }
            is ContentAction.UpdateLineSpacing -> {
                _state.value = _state.value.copy(
                    lineSpacing = action.lineSpacing
                )
                ttsServiceHandler.lineSpacingTTS = action.lineSpacing
            }
            is ContentAction.UpdateSelectedFontFamilyIndex -> {
                _state.value = _state.value.copy(
                    selectedFontFamilyIndex = action.index
                )
                ttsServiceHandler.fontFamilyTTS = _state.value.fontFamilies[action.index]
            }
            is ContentAction.UpdateTextAlign -> {
                _state.value = _state.value.copy(
                    textAlign = action.textAlign
                )
                ttsServiceHandler.textAlignTTS = action.textAlign
            }
            is ContentAction.UpdateTextIndent -> {
                _state.value = _state.value.copy(
                    textIndent = action.textIndent
                )
                ttsServiceHandler.textIndentTTS = action.textIndent
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
            is ContentAction.UpdateEnablePagerScroll -> {
                _state.value = _state.value.copy(
                    enablePagerScroll = action.enable
                )
            }
            is ContentAction.UpdateEnableBackgroundMusic -> {
                viewModelScope.launch {
                    val selectedTrack = musicPathRepository.getSelectedMusicPaths()
                    val silentMediaItem = MediaItem.Builder()
                        .setUri("asset:///silent.mp3".toUri())
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setArtworkUri(_state.value.book?.coverImagePath!!.toUri())
                                .setTitle(_state.value.book?.title)
                                .setArtist(_state.value.chapterHeader)
                                .build()
                        )
                        .build()
                    _state.value = _state.value.copy(
                        enableBackgroundMusic = action.enable
                    )
                    ttsServiceHandler.enableBackgroundMusic = action.enable
                    mediaItemList.clear()
                    if(action.enable) {
                        if (selectedTrack.isNotEmpty()) {
                            selectedTrack.forEach { track ->
                                val mediaItem = MediaItem.Builder()
                                    .setUri(track.uri)
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setArtworkUri(_state.value.book?.coverImagePath!!.toUri())
                                            .setTitle(_state.value.book?.title)
                                            .setArtist(_state.value.chapterHeader)
                                            .build()
                                    )
                                    .build()
                                mediaItemList.add(mediaItem)
                            }
                        } else {
                            mediaItemList.add(silentMediaItem)
                        }
                        mediaController?.apply {
                            if(_state.value.isSpeaking) {
                                volume = 0.3f
                            }
                            setMediaItems(mediaItemList,true)
                            prepare()
                            play()
                        }
                    } else {
                        if(_state.value.isSpeaking) {
                            mediaItemList.add(silentMediaItem)
                            mediaController?.apply {
                                setMediaItems(mediaItemList)
                                prepare()
                                play()
                            }
                        } else {
                            mediaController?.apply {
                                pause()
                                stop()
                            }
                        }
                    }
                }
            }
            is ContentAction.UpdatePlayerVolume -> {
                if(!_state.value.isSpeaking){
                    mediaController?.apply {
                        volume = action.volume
                    }
                }
            }
            is ContentAction.GetChapterContent -> {
                viewModelScope.launch {
                    getChapter(action.index)
                }
            }
        }
    }
    fun onTtsUiEvent(event: TtsUiEvent){
        when(event){
            is TtsUiEvent.Backward -> {
                ttsServiceHandler.onTtsPlayerEvent(TtsPlayerEvent.Backward)
            }
            is TtsUiEvent.Forward -> {
                ttsServiceHandler.onTtsPlayerEvent(TtsPlayerEvent.Forward)
            }
            is TtsUiEvent.PlayPause -> {
                ttsServiceHandler.onTtsPlayerEvent(TtsPlayerEvent.PlayPause(isPaused = event.isPaused))
            }
            is TtsUiEvent.SkipToBack -> {
                ttsServiceHandler.onTtsPlayerEvent(TtsPlayerEvent.SkipToBack)
            }
            is TtsUiEvent.SkipToNext -> {
                ttsServiceHandler.onTtsPlayerEvent(TtsPlayerEvent.SkipToNext)
            }
            is TtsUiEvent.Stop -> {
                ttsServiceHandler.onTtsPlayerEvent(TtsPlayerEvent.Stop)
            }
            is TtsUiEvent.JumpToRandomChapter -> {
                ttsServiceHandler.onTtsPlayerEvent(TtsPlayerEvent.JumpToRandomChapter)
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
            }

            val selectedVoice = tts.voices?.find {
                it.name == dataStoreManager.ttsVoice.first() && it.locale == selectedLocale
            } ?: tts.voices?.firstOrNull {
                it.locale == selectedLocale
            }
            onContentAction(dataStoreManager, ContentAction.UpdateTTSPitch(dataStoreManager.ttsPitch.first()))
            onContentAction(dataStoreManager, ContentAction.UpdateTTSSpeed(dataStoreManager.ttsSpeed.first()))
            selectedLocale?.let { ContentAction.UpdateTTSLanguage(it) }?.let {
                onContentAction(dataStoreManager,
                    it
                )
            }
            onContentAction(dataStoreManager, ContentAction.UpdateTTSVoice(selectedVoice))
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
    fun stopTTSService(context: Context) {
        mediaController?.pause()
        mediaController?.stop()
        mediaController?.release()
        val serviceIntent = Intent(context, TTSService::class.java)
        context.stopService(serviceIntent)
        controllerFuture?.let {
            MediaController.releaseFuture(it)
            mediaController = null
        }
        controllerFuture = null
    }
    fun setupTTS(context : Context){
        viewModelScope.launch {
            val tts = TextToSpeech(context,null)
            _state.update {
                it.copy(
                    tts = tts
                )
            }
        }
    }
    fun stopTTS(){
        _state.value.tts?.stop()
        _state.value.tts?.shutdown()
        serviceJob.forEach { it.cancel() }
        serviceJob.clear()
    }
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    fun initialize(
        context: Context,
        textMeasurer: TextMeasurer,
        dataStoreManager: DataStoreManager,
        enableBackgroundMusic: Boolean
    ) {
        if (mediaController != null) {
            return
        }
        viewModelScope.launch {
            val serviceIntent = Intent(context, TTSService::class.java)
            context.startService(serviceIntent)
        }
        val serviceComponentName = ComponentName(context, TTSService::class.java)
        val sessionToken = SessionToken(context, serviceComponentName)
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                mediaController?.prepare()
                onContentAction(dataStoreManager, ContentAction.UpdateEnableBackgroundMusic(enableBackgroundMusic))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
        viewModelScope.launch {
            ttsServiceHandler.totalChapter = _state.value.book?.totalChapter!!
            ttsServiceHandler.screenWidth = _state.value.screenWidth
            ttsServiceHandler.screenHeight = _state.value.screenHeight
            ttsServiceHandler.textMeasurer = textMeasurer
            ttsServiceHandler.updateTTSLanguage(_state.value.currentLanguage)
            ttsServiceHandler.updateTTSPitch(_state.value.currentPitch)
            ttsServiceHandler.updateTTSSpeed(_state.value.currentSpeed)
            ttsServiceHandler.updateTTSVoice(_state.value.currentVoice)
            ttsServiceHandler.textAlignTTS = _state.value.textAlign
            ttsServiceHandler.fontSizeTTS = _state.value.fontSize
            ttsServiceHandler.lineSpacingTTS = _state.value.lineSpacing
            ttsServiceHandler.fontFamilyTTS = _state.value.fontFamilies[_state.value.selectedFontFamilyIndex]
            ttsServiceHandler.textIndentTTS = _state.value.textIndent
            coroutineScope {
                serviceJob += launch {
                    ttsServiceHandler.currentParagraphIndex.collectLatest { currentReadingParagraph ->
                        _state.update { it.copy(currentReadingParagraph = currentReadingParagraph) }
                    }
                }
                serviceJob += launch {
                    ttsServiceHandler.currentChapterIndex.collectLatest { currentChapterIndex ->
                        _state.update { it.copy(currentChapterIndex = currentChapterIndex) }
                        val chapter = chapterRepository.getChapterContent(bookId, currentChapterIndex)
                        chapter?.let {content->
                            parseListToUsableLists(content.content).also {
                                ttsServiceHandler.currentChapterParagraphs = it.second
                            }
                            mediaController?.apply {
                                if(_state.value.isSpeaking) {
                                    val updatedMetadata = currentMediaItem?.mediaMetadata?.buildUpon()
                                        ?.setArtist(content.chapterTitle)?.build()!!
                                    val updatedMediaItem =
                                        currentMediaItem?.buildUpon()?.setMediaMetadata(updatedMetadata)
                                            ?.build()!!
                                    replaceMediaItem(0, updatedMediaItem)
                                    prepare()
                                    play()
                                }
                            }
                        }

                    }
                }
                serviceJob += launch {
                    ttsServiceHandler.isSpeaking.collectLatest { isSpeaking ->
                        _state.update { it.copy(isSpeaking = isSpeaking) }
                    }
                }
                serviceJob += launch {
                    ttsServiceHandler.isPaused.collectLatest { isPaused ->
                        _state.update { it.copy(isPaused = isPaused) }
                    }
                }
                serviceJob += launch {
                    ttsServiceHandler.scrollTimes.collectLatest { scrollTimes ->
                        _state.update { it.copy(scrollTime = scrollTimes) }
                    }
                }
                serviceJob += launch {
                    ttsServiceHandler.flagTriggerScroll.collectLatest { flagTriggerScroll ->
                        _state.update { it.copy(flagTriggerScrolling = flagTriggerScroll) }
                    }
                }
            }
        }
    }
    fun play() {
        val mediaItem = MediaItem.Builder()
            .setUri("asset:///silent.mp3".toUri())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setArtworkUri(_state.value.book?.coverImagePath!!.toUri())
                    .setTitle(_state.value.book?.title)
                    .setArtist(_state.value.chapterHeader)
                    .build())
            .build()
        if(mediaController?.isPlaying!!){
            mediaController?.apply {
                volume = 0.3f
            }
            ttsServiceHandler.startReading(_state.value.firstVisibleItemIndex)
        } else {
            mediaController?.apply {
                setMediaItems(listOf(mediaItem))
                prepare()
                play()
                ttsServiceHandler.startReading(_state.value.firstVisibleItemIndex)
            }
        }
    }
    fun removeMediaItemByUri(uri: Uri) {
        mediaController?.let { controller ->
            val index = mediaItemList.indexOfFirst { it.localConfiguration?.uri == uri }
            if (index != -1) {
                mediaItemList.removeAt(index)
                controller.removeMediaItem(index)
            }
        }
    }
}