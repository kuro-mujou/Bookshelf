package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

@UnstableApi
class TTSServiceHandler(
    private val context: Context,
) : Player.Listener {
    private val _currentParagraphIndex = MutableStateFlow(-1)
    val currentParagraphIndex = _currentParagraphIndex.asStateFlow()
    private val _currentChapterIndex = MutableStateFlow(-1)
    val currentChapterIndex = _currentChapterIndex.asStateFlow()
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking = _isSpeaking.asStateFlow()
    private val _isPaused = MutableStateFlow(false)
    val isPaused = _isPaused.asStateFlow()
    private val _scrollTimes = MutableStateFlow(0)
    val scrollTimes = _scrollTimes.asStateFlow()
    private val _flagTriggerScroll = MutableStateFlow(false)
    val flagTriggerScroll = _flagTriggerScroll.asStateFlow()
    private var player: Player? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null
    private var audioFocusRequestResult: Int = AudioManager.AUDIOFOCUS_NONE
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized by mutableStateOf(false)
    private var oldPos by mutableIntStateOf(0)
    private var sumLength by mutableIntStateOf(0)
    private var currentReadingPositionInParagraph by mutableIntStateOf(0)
    private var totalParagraphs by mutableIntStateOf(0)
    private var textToSpeakNow by mutableStateOf("")
    private var flowTextLength = mutableStateListOf<Int>()
    var textMeasurer: TextMeasurer? = null
    var fontFamilyTTS by mutableStateOf<FontFamily?>(null)
    var fontSizeTTS by mutableIntStateOf(0)
    var lineSpacingTTS by mutableIntStateOf(0)
    var totalChapter by mutableIntStateOf(0)
    var screenWidth by mutableIntStateOf(0)
    var screenHeight by mutableIntStateOf(0)
    var currentChapterParagraphs by mutableStateOf<List<String>>(emptyList())
    var textIndentTTS by mutableStateOf(false)
    var textAlignTTS by mutableStateOf(false)
    var enableBackgroundMusic by mutableStateOf(false)
    var isTracksNull by mutableStateOf(false)
    fun onTtsPlayerEvent(event: TtsPlayerEvent) {
        when (event) {
            is TtsPlayerEvent.Backward -> {
                playPreviousParagraphOrChapter()
            }

            is TtsPlayerEvent.Forward -> {
                playNextParagraphOrChapter()
            }

            is TtsPlayerEvent.PlayPause -> {
                if (event.isPaused) {
                    pauseReading()
                } else {
                    resumeReading()
                }
            }

            is TtsPlayerEvent.Stop -> {
                stopReading()
            }

            is TtsPlayerEvent.SkipToBack -> {
                moveToPreviousChapterOrStop()
            }

            is TtsPlayerEvent.SkipToNext -> {
                moveToNextChapterOrStop()
            }

            is TtsPlayerEvent.JumpToRandomChapter -> {
                jumpToRandomChapter()
            }
        }
    }

    fun initSystem(
        player: Player?,
        audioManager: AudioManager,
        focusRequest: AudioFocusRequest
    ) {
        this.player = player
        this.audioManager = audioManager
        this.focusRequest = focusRequest
    }

    fun initializeTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
                textToSpeech?.voice = textToSpeech?.defaultVoice
                textToSpeech?.setSpeechRate(1f)
                textToSpeech?.setPitch(1f)
                isTtsInitialized = true
            }
        }
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _flagTriggerScroll.value = false
            }

            override fun onDone(utteranceId: String?) {
                playNextParagraphOrChapter()
                _flagTriggerScroll.value = false
                currentReadingPositionInParagraph = 0
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                super.onRangeStart(utteranceId, start, end, frame)
                if (_isSpeaking.value) {
                    val currentPos = textToSpeakNow.substring(0, end).length
                    _flagTriggerScroll.value = false
                    currentReadingPositionInParagraph = oldPos + currentPos
                    if (flowTextLength.size > 1) {
                        if (oldPos + currentPos > sumLength) {
                            _flagTriggerScroll.value = true
                            _scrollTimes.value += 1
                            sumLength += flowTextLength[_scrollTimes.value]
                            currentReadingPositionInParagraph = oldPos + currentPos
                        }
                    }
                }
            }
        })
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (_isSpeaking.value) {
            if (isPlaying) {
                _isPaused.value = false
                resumeReading()
            } else {
                _isPaused.value = true
                pauseReading()
            }
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        super.onEvents(player, events)
    }

    fun startReading(paragraphIndex: Int, chapterIndex: Int) {
        if (!isTtsInitialized) {
            stopReading()
        }
        if (currentChapterParagraphs.isEmpty()) {
            stopReading()
        }
        audioFocusRequestResult = audioManager!!.requestAudioFocus(focusRequest!!)
        _isSpeaking.value = true
        _isPaused.value = false
        currentReadingPositionInParagraph = 0
        _currentParagraphIndex.value = paragraphIndex
        _currentChapterIndex.value = chapterIndex
        if (audioFocusRequestResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            startSpeakCurrentParagraph()
        }
    }

    fun pauseReading() {
        if (textToSpeech?.isSpeaking == true) {
            if (!enableBackgroundMusic) {
                player?.pause()
            } else {
                player?.volume = 1f
            }
            textToSpeech?.stop()
        }
    }

    fun resumeReading() {
        if (!enableBackgroundMusic) {
            player?.play()
        } else {
            player?.volume = 0.3f
        }
        startSpeakCurrentParagraph()
    }

    fun stopReading() {
        if (!enableBackgroundMusic || isTracksNull) {
            player?.stop()
            player?.clearMediaItems()
        } else {
            player?.volume = 1f
        }
        _isSpeaking.value = false
        _isPaused.value = false
        textToSpeech?.stop()
        _currentParagraphIndex.value = -1
        currentReadingPositionInParagraph = 0
        audioFocusRequestResult = audioManager?.abandonAudioFocusRequest(focusRequest!!)!!
    }

    fun shutdown() {
        stopReading()
        textToSpeech?.shutdown()
        textToSpeech = null
    }

    private fun startSpeakCurrentParagraph() {
        if (_currentChapterIndex.value != -1) {
            if (currentChapterParagraphs.isNotEmpty() && _currentParagraphIndex.value < currentChapterParagraphs.size) {
                totalParagraphs = currentChapterParagraphs.size
                val text = currentChapterParagraphs[_currentParagraphIndex.value]
                textToSpeakNow = text.substring(currentReadingPositionInParagraph)
                oldPos = text.length - textToSpeakNow.length
                _scrollTimes.value = 0
                flowTextLength = processTextLength(
                    text = text,
                    maxWidth = screenWidth,
                    maxHeight = screenHeight,
                    textStyle = TextStyle(
                        textIndent = if (textIndentTTS)
                            TextIndent(firstLine = (fontSizeTTS * 2).sp)
                        else
                            TextIndent.None,
                        textAlign = if (textAlignTTS) TextAlign.Justify else TextAlign.Left,
                        fontSize = fontSizeTTS.sp,
                        fontFamily = fontFamilyTTS,
                        lineBreak = LineBreak.Paragraph,
                        lineHeight = (fontSizeTTS + lineSpacingTTS).sp
                    ),
                    textMeasurer = textMeasurer!!
                )
                sumLength = flowTextLength[_scrollTimes.value]
                textToSpeech?.speak(
                    textToSpeakNow,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "paragraph_${_currentChapterIndex.value}_${_currentParagraphIndex.value}"
                )
            } else {
                moveToNextChapterOrStop()
            }
        }
    }

    private fun playNextParagraphOrChapter() {
        if (_currentChapterIndex.value != -1) {
            if (currentChapterParagraphs.isNotEmpty() && _currentParagraphIndex.value < currentChapterParagraphs.size - 1) {
                _currentParagraphIndex.value += 1
                currentReadingPositionInParagraph = 0
                if (_isSpeaking.value && !_isPaused.value)
                    startSpeakCurrentParagraph()
            } else {
                moveToNextChapterOrStop()
            }
        }
    }

    private fun playPreviousParagraphOrChapter() {
        if (_currentChapterIndex.value != -1) {
            if (currentChapterParagraphs.isNotEmpty() && _currentParagraphIndex.value - 1 >= 0) {
                _currentParagraphIndex.value -= 1
                currentReadingPositionInParagraph = 0
                if (_isSpeaking.value && !_isPaused.value)
                    startSpeakCurrentParagraph()
            } else {
                moveToPreviousChapterOrStop()
            }
        }
    }

    fun moveToNextChapterOrStop() {
        if (_isSpeaking.value) {
            if ((_currentChapterIndex.value + 1) < totalChapter) {
                textToSpeech?.stop()
                currentReadingPositionInParagraph = 0
                _currentParagraphIndex.value = 0
                _currentChapterIndex.value += 1
                Handler(Looper.getMainLooper()).postDelayed({
                    if (_isSpeaking.value && !_isPaused.value)
                        startSpeakCurrentParagraph()
                }, 1000)
            } else {
                stopReading()
            }
        } else {
            player?.seekToNextMediaItem()
        }
    }

    fun moveToPreviousChapterOrStop() {
        if (_isSpeaking.value) {
            if ((_currentChapterIndex.value - 1) >= 0) {
                textToSpeech?.stop()
                currentReadingPositionInParagraph = 0
                _currentParagraphIndex.value = 0
                _currentChapterIndex.value -= 1
                Handler(Looper.getMainLooper()).postDelayed({
                    if (_isSpeaking.value && !_isPaused.value)
                        startSpeakCurrentParagraph()
                }, 1000)
            } else {
                stopReading()
            }
        } else {
            player?.seekToPreviousMediaItem()
        }
    }

    private fun jumpToRandomChapter() {
        textToSpeech?.stop()
        currentReadingPositionInParagraph = 0
        _currentParagraphIndex.value = 0
        Handler(Looper.getMainLooper()).postDelayed({
            if (_isSpeaking.value && !_isPaused.value)
                startSpeakCurrentParagraph()
        }, 1000)
    }

    private fun processTextLength(
        text: String,
        maxWidth: Int,
        maxHeight: Int,
        textStyle: TextStyle,
        textMeasurer: TextMeasurer,
    ): SnapshotStateList<Int> {
        var remainingText = text
        val subStringLength = mutableStateListOf<Int>()
        while (remainingText.isNotEmpty()) {
            val measuredLayoutResult = textMeasurer.measure(
                text = remainingText,
                style = textStyle,
                overflow = TextOverflow.Ellipsis,
                constraints = Constraints(
                    maxWidth = maxWidth,
                    maxHeight = maxHeight
                ),
            )
            if (measuredLayoutResult.hasVisualOverflow) {
                val lastVisibleCharacterIndex = measuredLayoutResult.getLineEnd(
                    lineIndex = measuredLayoutResult.lineCount - 1,
                    visibleEnd = true
                )
                val endIndex = minOf(lastVisibleCharacterIndex, remainingText.length)
                val endSubString = remainingText.substring(0, endIndex)
                subStringLength.add(endSubString.trim().length)
                remainingText = remainingText.substring(endIndex)
            } else {
                subStringLength.add(remainingText.trim().length)
                remainingText = ""
            }
        }
        return subStringLength
    }

    fun updateTTSLanguage(language: Locale?) {
        textToSpeech?.language = language ?: Locale.getDefault()
    }

    fun updateTTSVoice(voice: Voice?) {
        textToSpeech?.voice = voice ?: textToSpeech?.defaultVoice
    }

    fun updateTTSSpeed(speed: Float?) {
        textToSpeech?.setSpeechRate(speed ?: 1f)
    }

    fun updateTTSPitch(pitch: Float?) {
        textToSpeech?.setPitch(pitch ?: 1f)
    }

    fun updateCurrentChapterIndex(index: Int) {
        _currentChapterIndex.value = index
    }
}