package com.capstone.bookshelf.presentation.bookcontent.content

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextMeasurer
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.Builder
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
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TtsUiEvent.Backward
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TtsUiEvent.Forward
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TtsUiEvent.JumpToRandomChapter
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TtsUiEvent.PlayPause
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TtsUiEvent.SkipToBack
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TtsUiEvent.SkipToNext
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TtsUiEvent.Stop
import com.capstone.bookshelf.util.DataStoreManager
import com.capstone.bookshelf.util.deleteImageFromPrivateStorage
import com.capstone.bookshelf.util.saveImageToPrivateStorage
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
    private val ttsServiceHandler: TTSServiceHandler,
    private val dataStoreManager: DataStoreManager,
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
                        Builder()
                            .setUri(media3Item.uri)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setArtworkUri(_state.value.book?.coverImagePath?.toUri())
                                    .setTitle(_state.value.book?.title)
                                    .setArtist(_state.value.chapterHeader)
                                    .build()
                            )
                            .build()
                    })
                    if (mediaItemList.isNotEmpty()) {
                        ttsServiceHandler.isTracksNull = false
                        mediaController?.apply {
                            if (_state.value.enableBackgroundMusic) {
                                mediaItemList.shuffle()
                                setMediaItems(mediaItemList)
                                prepare()
                                play()
                            }
                        }
                    } else {
                        ttsServiceHandler.isTracksNull = true
                    }
                }
        }
    }

    fun onContentAction(action: ContentAction) {
        when (action) {
            is ContentAction.LoadBook -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            book = bookRepository.getBook(bookId)
                        )
                    }
                }
            }

            is ContentAction.UpdateFlagTriggerAdjustScroll -> {
                _state.update {
                    it.copy(
                        flagTriggerAdjustScroll = action.value
                    )
                }
            }

            is ContentAction.UpdateFlagStartScrolling -> {
                _state.update {
                    it.copy(
                        flagStartScrolling = action.value
                    )
                }
            }

            is ContentAction.UpdateFlagScrollAdjusted -> {
                _state.update {
                    it.copy(
                        flagScrollAdjusted = action.value
                    )
                }
            }

            is ContentAction.UpdateFlagStartAdjustScroll -> {
                _state.update {
                    it.copy(
                        flagStartAdjustScroll = action.value
                    )
                }
            }

            is ContentAction.UpdateFirstVisibleItemIndex -> {
                _state.update {
                    it.copy(
                        firstVisibleItemIndex = action.index
                    )
                }
            }

            is ContentAction.UpdateLastVisibleItemIndex -> {
                _state.update {
                    it.copy(
                        lastVisibleItemIndex = action.index
                    )
                }
            }

            is ContentAction.UpdateCurrentChapterIndex -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            currentChapterIndex = action.index
                        )
                    }
                    if (mediaController?.isPlaying == true) {
                        mediaController?.apply {
                            if (_state.value.isSpeaking) {
                                val chapter = chapterRepository.getChapterContent(bookId, action.index)
                                val updatedMetadata = currentMediaItem?.mediaMetadata?.buildUpon()
                                    ?.setArtist(chapter?.chapterTitle)?.build()
                                val updatedMediaItem = updatedMetadata?.let{
                                    currentMediaItem?.buildUpon()?.setMediaMetadata(updatedMetadata)
                                        ?.build()
                                }
                                updatedMediaItem?.let { replaceMediaItem(0, it) }
                                prepare()
                                play()
                            }
                        }
                    }
                    ttsServiceHandler.updateCurrentChapterIndex(action.index)
                }
            }

            is ContentAction.UpdateScreenHeight -> {
                _state.update {
                    it.copy(
                        screenHeight = action.value
                    )
                }
            }

            is ContentAction.UpdateScreenWidth -> {
                _state.update {
                    it.copy(
                        screenWidth = action.value
                    )
                }
            }

            is ContentAction.UpdatePreviousChapterIndex -> {
                _state.update {
                    it.copy(
                        previousChapterIndex = action.index
                    )
                }
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
                _state.update {
                    it.copy(
                        chapterHeader = action.header
                    )
                }
            }

            is ContentAction.UpdateIsSpeaking -> {
                _state.update {
                    it.copy(
                        isSpeaking = action.isSpeaking
                    )
                }
            }

            is ContentAction.UpdateIsFocused -> {
                _state.update {
                    it.copy(
                        isFocused = action.isFocused
                    )
                }
            }

            is ContentAction.UpdateIsPaused -> {
                _state.update {
                    it.copy(
                        isPaused = action.isPaused
                    )
                }
                onTtsUiEvent(PlayPause(action.isPaused))
            }

            is ContentAction.UpdateTTSLanguage -> {
                _state.update {
                    it.copy(
                        currentLanguage = action.currentLanguage
                    )
                }
                viewModelScope.launch {
                    dataStoreManager.setTTSLocale(action.currentLanguage.displayName)
                }
                ttsServiceHandler.updateTTSLanguage(action.currentLanguage)
            }

            is ContentAction.UpdateTTSPitch -> {
                _state.update {
                    it.copy(
                        currentPitch = action.currentPitch
                    )
                }
                viewModelScope.launch {
                    dataStoreManager.setTTSPitch(action.currentPitch)
                }
                ttsServiceHandler.updateTTSPitch(action.currentPitch)
            }

            is ContentAction.UpdateTTSSpeed -> {
                _state.update {
                    it.copy(
                        currentSpeed = action.currentSpeed
                    )
                }
                viewModelScope.launch {
                    dataStoreManager.setTTSSpeed(action.currentSpeed)
                }
                ttsServiceHandler.updateTTSSpeed(action.currentSpeed)
            }

            is ContentAction.UpdateTTSVoice -> {
                _state.update {
                    it.copy(
                        currentVoice = action.currentVoice
                    )
                }
                if (action.currentVoice != null) {
                    viewModelScope.launch {
                        dataStoreManager.setTTSVoice(action.currentVoice.name)
                    }
                }
                ttsServiceHandler.updateTTSVoice(action.currentVoice)
            }

            is ContentAction.UpdateCurrentReadingParagraph -> {
                _state.update {
                    it.copy(
                        currentReadingParagraph = action.pos
                    )
                }
            }

            is ContentAction.UpdateFontSize -> {
                _state.update {
                    it.copy(
                        fontSize = action.fontSize
                    )
                }
                ttsServiceHandler.fontSizeTTS = action.fontSize
            }

            is ContentAction.UpdateLineSpacing -> {
                _state.update {
                    it.copy(
                        lineSpacing = action.lineSpacing
                    )
                }
                ttsServiceHandler.lineSpacingTTS = action.lineSpacing
            }

            is ContentAction.UpdateSelectedFontFamilyIndex -> {
                _state.update {
                    it.copy(
                        selectedFontFamilyIndex = action.index
                    )
                }
                ttsServiceHandler.fontFamilyTTS = _state.value.fontFamilies[action.index]
            }

            is ContentAction.UpdateTextAlign -> {
                _state.update {
                    it.copy(
                        textAlign = action.textAlign
                    )
                }
                viewModelScope.launch {
                    dataStoreManager.setTextAlign(action.textAlign)
                }
                ttsServiceHandler.textAlignTTS = action.textAlign
            }

            is ContentAction.UpdateTextIndent -> {
                _state.update {
                    it.copy(
                        textIndent = action.textIndent
                    )
                }
                viewModelScope.launch {
                    dataStoreManager.setTextIndent(action.textIndent)
                }
                ttsServiceHandler.textIndentTTS = action.textIndent
            }

            is ContentAction.SelectedBook -> {
                _state.update {
                    it.copy(
                        book = action.book
                    )
                }
            }

            is ContentAction.UpdateKeepScreenOn -> {
                _state.update {
                    it.copy(
                        keepScreenOn = action.keepScreenOn
                    )
                }
            }

            is ContentAction.UpdateEnablePagerScroll -> {
                _state.update {
                    it.copy(
                        enablePagerScroll = action.enable
                    )
                }
            }

            is ContentAction.UpdateEnableBackgroundMusic -> {
                viewModelScope.launch {
                    dataStoreManager.setEnableBackgroundMusic(action.enable)
                    val chapter = chapterRepository.getChapterContent(bookId, _state.value.currentChapterIndex)
                    val selectedTrack = musicPathRepository.getSelectedMusicPaths()
                    val silentMediaItem = Builder()
                        .setUri("asset:///silent.mp3".toUri())
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setArtworkUri(_state.value.book?.coverImagePath!!.toUri())
                                .setTitle(_state.value.book?.title)
                                .setArtist(chapter?.chapterTitle)
                                .build()
                        )
                        .build()
                    _state.update {
                        it.copy(
                            enableBackgroundMusic = action.enable
                        )
                    }
                    ttsServiceHandler.enableBackgroundMusic = action.enable
                    if (action.enable) {
                        mediaItemList.clear()
                        if (selectedTrack.isNotEmpty()) {
                            ttsServiceHandler.isTracksNull = false
                            selectedTrack.forEach { track ->
                                val mediaItem = Builder()
                                    .setUri(track.uri)
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setArtworkUri(_state.value.book?.coverImagePath!!.toUri())
                                            .setTitle(_state.value.book?.title)
                                            .setArtist(chapter?.chapterTitle)
                                            .build()
                                    )
                                    .build()
                                mediaItemList.add(mediaItem)
                            }
                            mediaController?.apply {
                                if (_state.value.isSpeaking) {
                                    volume = 0.3f
                                }
                                mediaItemList.shuffle()
                                setMediaItems(mediaItemList, true)
                                prepare()
                                play()
                            }
                        } else {
                            ttsServiceHandler.isTracksNull = true
                        }
                    } else {
                        if (_state.value.isSpeaking) {
                            mediaController?.apply {
                                setMediaItems(listOf(silentMediaItem))
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
                if (!_state.value.isSpeaking) {
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

            is ContentAction.UpdateEnableUndoButton -> {
                _state.update {
                    it.copy(
                        enableUndoButton = action.enable
                    )
                }
            }

            is ContentAction.UpdateImagePaddingState -> {
                _state.update {
                    it.copy(
                        imagePaddingState = action.imagePaddingState
                    )
                }
                viewModelScope.launch {
                    dataStoreManager.setImagePaddingState(action.imagePaddingState)
                }
            }

            is ContentAction.UpdateSelectedBookmarkStyle -> {
                _state.update {
                    it.copy(
                        selectedBookmarkStyle = action.style
                    )
                }
                viewModelScope.launch {
                    dataStoreManager.setBookmarkStyle(action.style)
                }
            }

            is ContentAction.UpdateFlagTriggerScrollForNote -> {
                _state.update {
                    it.copy(
                        flagTriggerScrollForNote = action.value
                    )
                }
            }

            is ContentAction.UpdateBookTitle -> {
                viewModelScope.launch {
                    bookRepository.saveBookInfoTitle(bookId,action.title+"(Draft)")
                }
            }

            is ContentAction.UpdateBookAuthors -> {
                viewModelScope.launch {
                    val authorList = action.authors.split(",")
                    bookRepository.saveBookInfoAuthors(bookId, authorList)
                }
            }

            is ContentAction.UpdateCoverImage -> {
                viewModelScope.launch {
                    val imageFileName = action.path.substringAfterLast('/').substringBeforeLast('.')
                    deleteImageFromPrivateStorage(action.path)
                    action.context.contentResolver.openInputStream(action.uri).use {
                        val bitmap = BitmapFactory.decodeStream(it)
                        saveImageToPrivateStorage(
                            context = action.context,
                            bitmap = bitmap,
                            filename = imageFileName
                        )
                    }
                }
            }

            is ContentAction.UpdateBookAsRecentRead -> {
                viewModelScope.launch {
                    bookRepository.updateRecentRead(bookId)
                }
            }
        }
    }

    fun onTtsUiEvent(event: TtsUiEvent) {
        when (event) {
            is Backward -> {
                ttsServiceHandler.onTtsPlayerEvent(TtsPlayerEvent.Backward)
            }

            is Forward -> {
                ttsServiceHandler.onTtsPlayerEvent(TtsPlayerEvent.Forward)
            }

            is PlayPause -> {
                ttsServiceHandler.onTtsPlayerEvent(TtsPlayerEvent.PlayPause(isPaused = event.isPaused))
            }

            is SkipToBack -> {
                ttsServiceHandler.onTtsPlayerEvent(TtsPlayerEvent.SkipToBack)
            }

            is SkipToNext -> {
                ttsServiceHandler.onTtsPlayerEvent(TtsPlayerEvent.SkipToNext)
            }

            is Stop -> {
                ttsServiceHandler.onTtsPlayerEvent(TtsPlayerEvent.Stop)
            }

            is JumpToRandomChapter -> {
                ttsServiceHandler.onTtsPlayerEvent(TtsPlayerEvent.JumpToRandomChapter)
            }
        }
    }

    suspend fun getChapter(page: Int) {
        _chapterContent.value = chapterRepository.getChapterContent(bookId, page)
    }

    fun loadTTSSetting(tts: TextToSpeech) {
        viewModelScope.launch {
            val selectedLocale = tts.availableLanguages?.find {
                it.displayName == dataStoreManager.ttsLocale.first()
            }
            val selectedVoice = tts.voices?.find {
                it.name == dataStoreManager.ttsVoice.first() && it.locale == selectedLocale
            } ?: tts.voices?.firstOrNull {
                it.locale == selectedLocale
            }
            onContentAction(ContentAction.UpdateTTSPitch(dataStoreManager.ttsPitch.first()))
            onContentAction(ContentAction.UpdateTTSSpeed(dataStoreManager.ttsSpeed.first()))
            selectedLocale?.let { ContentAction.UpdateTTSLanguage(it) }?.let {
                onContentAction(it)
            }
            onContentAction(ContentAction.UpdateTTSVoice(selectedVoice))
        }
    }

    fun fixNullVoice(textToSpeech: TextToSpeech) {
        viewModelScope.launch {
            var selectedVoice = textToSpeech.voices?.find {
                it.locale == _state.value.currentLanguage
            }
            if (selectedVoice == null) {
                selectedVoice = textToSpeech.voices?.firstOrNull {
                    it.locale == _state.value.currentLanguage
                } ?: textToSpeech.defaultVoice
            }
            onContentAction(ContentAction.UpdateTTSVoice(selectedVoice!!))
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

    fun setupTTS(context: Context) {
        viewModelScope.launch {
            val tts = TextToSpeech(context, null)
            _state.update {
                it.copy(
                    tts = tts
                )
            }
        }
    }

    fun stopTTS() {
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
        enableBackgroundMusic: Boolean,
        initChapterIndex: Int
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
                onContentAction(ContentAction.UpdateEnableBackgroundMusic(enableBackgroundMusic))
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
            ttsServiceHandler.fontFamilyTTS =
                _state.value.fontFamilies[_state.value.selectedFontFamilyIndex]
            ttsServiceHandler.textIndentTTS = _state.value.textIndent
            ttsServiceHandler.updateCurrentChapterIndex(_state.value.currentChapterIndex)
            coroutineScope {
                serviceJob += launch {
                    ttsServiceHandler.currentParagraphIndex.collectLatest { currentReadingParagraph ->
                        if (currentReadingParagraph != -1) {
                            _state.update { it.copy(currentReadingParagraph = currentReadingParagraph) }
                            bookRepository.saveBookInfoParagraphIndex(
                                bookId,
                                currentReadingParagraph
                            )
                        }
                    }
                }
                serviceJob += launch {
                    ttsServiceHandler.currentChapterIndex.collectLatest { currentChapterIndex ->
                        _state.update { it.copy(currentChapterIndex = currentChapterIndex) }
                        bookRepository.saveBookInfoChapterIndex(bookId, currentChapterIndex)
                        val chapter = chapterRepository.getChapterContent(bookId, currentChapterIndex)
                        val htmlTagPattern = Regex(pattern = """<[^>]+>""")
                        val linkPattern = Regex("""\.capstone\.bookshelf/files/[^ ]*""")
                        ttsServiceHandler.currentChapterParagraphs = chapter?.content?.map { raw ->
                            val cleaned = htmlTagPattern.replace(raw, replacement = "")
                            if (linkPattern.containsMatchIn(cleaned)) {
                                " "
                            } else {
                                cleaned.trim()
                            }
                        } ?: emptyList()
                        mediaController?.apply {
                            if (_state.value.isSpeaking) {
                                val updatedMetadata = currentMediaItem?.mediaMetadata?.buildUpon()
                                    ?.setArtist(chapter?.chapterTitle)?.build()
                                val updatedMediaItem = updatedMetadata?.let{
                                    currentMediaItem?.buildUpon()?.setMediaMetadata(updatedMetadata)
                                        ?.build()
                                }
                                updatedMediaItem?.let { replaceMediaItem(0, it) }
                                prepare()
                                play()
                            }
                        }
                    }
                }
                serviceJob += launch {
                    ttsServiceHandler.isSpeaking.collectLatest { isSpeaking ->
                        _state.update { it.copy(isSpeaking = isSpeaking) }
                        if (!isSpeaking) {
                            bookRepository.saveBookInfoChapterIndex(
                                bookId,
                                _state.value.currentChapterIndex
                            )
                            bookRepository.saveBookInfoParagraphIndex(
                                bookId,
                                _state.value.currentReadingParagraph
                            )
                        }
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
        val mediaItem = Builder()
            .setUri("asset:///silent.mp3".toUri())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setArtworkUri(_state.value.book?.coverImagePath!!.toUri())
                    .setTitle(_state.value.book?.title)
                    .setArtist(_state.value.chapterHeader)
                    .build()
            )
            .build()
        if (mediaController?.isPlaying!!) {
            mediaController?.apply {
                volume = 0.3f
            }
            ttsServiceHandler.startReading(
                paragraphIndex = _state.value.firstVisibleItemIndex,
                chapterIndex = _state.value.currentChapterIndex
            )
        } else {
            mediaController?.apply {
                setMediaItems(listOf(mediaItem))
                prepare()
                play()
                ttsServiceHandler.startReading(
                    paragraphIndex = _state.value.firstVisibleItemIndex,
                    chapterIndex = _state.value.currentChapterIndex
                )
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