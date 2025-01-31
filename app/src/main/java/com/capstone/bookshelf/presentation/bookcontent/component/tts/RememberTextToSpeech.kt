package com.capstone.bookshelf.presentation.bookcontent.component.tts
//
////import com.capstone.bookshelf.util.NotificationHelper
////import com.capstone.bookshelf.util.NotificationReceiver
//import android.content.Context
//import android.media.AudioAttributes
//import android.media.AudioFocusRequest
//import android.media.AudioManager
//import android.speech.tts.TextToSpeech
//import android.speech.tts.UtteranceProgressListener
//import android.speech.tts.Voice
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.runtime.snapshots.SnapshotStateList
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.TextMeasurer
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.rememberTextMeasurer
//import androidx.compose.ui.text.style.LineBreak
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextIndent
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.Constraints
//import androidx.compose.ui.unit.sp
//import com.capstone.bookshelf.presentation.bookcontent.component.font.FontState
//import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
//import kotlinx.coroutines.flow.StateFlow
//import java.util.Locale
//
//@Composable
//fun rememberTextToSpeechController(
//    ttsState: StateFlow<TTSState>,
//    contentState: StateFlow<ContentState>,
//    fontState : StateFlow<FontState>,
//    callbackForScroll : (shouldScroll: Boolean,scrollTime: Int) -> Unit,
//    callbackForNextChapter : (chapterIndex: Int,paragraphIndex: Int) -> Unit,
//    callbackForUpdateState : (isSpeaking: Boolean,isPaused: Boolean) -> Unit
//): TextToSpeechController {
//    val context = LocalContext.current
//    val textMeasurer = rememberTextMeasurer()
//    return remember {
//        TextToSpeechController(
//            ttsState = ttsState,
//            contentState = contentState,
//            fontState = fontState,
//            textMeasurer = textMeasurer,
//            context = context,
//            callbackForScroll = callbackForScroll,
//            callbackForNextChapter = callbackForNextChapter,
//            callbackForUpdateState = callbackForUpdateState
//        )
//    }
//}
//
//class TextToSpeechController(
//    private val ttsState: StateFlow<TTSState>,
//    private val contentState: StateFlow<ContentState>,
//    private val fontState: StateFlow<FontState>,
//    private val textMeasurer: TextMeasurer,
//    private val context: Context,
//    private val callbackForScroll : (shouldScroll: Boolean,scrollTime: Int) -> Unit,
//    private val callbackForNextChapter : (chapterIndex: Int,paragraphIndex: Int) -> Unit,
//    private val callbackForUpdateState : (isSpeaking: Boolean,isPaused: Boolean) -> Unit
//) {
//    private var textToSpeech: TextToSpeech? = null
//    private var currentParagraphIndex by mutableIntStateOf(0)
//    private var chapterParagraphsMap by mutableStateOf<Map<Int, List<String>>>(emptyMap())
//    private var isTtsInitialized by mutableStateOf(false)
//    private var currentReadingPositionInParagraph by mutableIntStateOf(0)
//    private var oldPos by mutableIntStateOf(0)
//    private var textToSpeakNow by mutableStateOf("")
//    private var flowTextLength = mutableStateListOf<Int>()
//    private var sumLength by mutableIntStateOf(0)
//    private var i by mutableIntStateOf(0)
//    private var audioManager: AudioManager? = null
//    private var playbackAttributes: AudioAttributes? = null
//    private var audioFocusChangeListener =
//        AudioManager.OnAudioFocusChangeListener { focusChange ->
//            when (focusChange) {
//                AudioManager.AUDIOFOCUS_GAIN -> {
//                    resumeReading()
//                }
//                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
//                    pauseReading()
//                }
//                AudioManager.AUDIOFOCUS_LOSS -> {
//                    stopReading()
//                }
//            }
//        }
//    var audioFocusRequest : Int = 0
//    private var focusRequest : AudioFocusRequest? = null
//
//    init {
//        initializeTts()
//    }
//
//    private fun initializeTts() {
//        textToSpeech = TextToSpeech(context) { status ->
//            if (status == TextToSpeech.SUCCESS) {
//                textToSpeech?.language = ttsState.value.currentLanguage?: Locale.getDefault()
//                textToSpeech?.voice = ttsState.value.currentVoice?: textToSpeech?.defaultVoice
//                textToSpeech?.setSpeechRate(ttsState.value.currentSpeed?: 1f)
//                textToSpeech?.setPitch(ttsState.value.currentPitch?: 1f)
//                isTtsInitialized = true
//            }
//        }
//
//        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
//            override fun onStart(utteranceId: String?) {}
//            override fun onDone(utteranceId: String?) {
//                playNextParagraphOrChapter()
//                currentReadingPositionInParagraph = 0
//            }
//            @Deprecated("Deprecated in Java")
//            override fun onError(utteranceId: String?) {}
//            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
//                super.onRangeStart(utteranceId, start, end, frame)
//                if(ttsState.value.isSpeaking) {
//                    val currentPos = textToSpeakNow.substring(0, end).length
//                    currentReadingPositionInParagraph = oldPos + currentPos
//                    callbackForScroll(contentState.value.flagTriggerScrolling,i)
//                    if (flowTextLength.size > 1) {
//                        if (oldPos + currentPos > sumLength) {
//                            callbackForScroll(!contentState.value.flagTriggerScrolling,i+1)
//                            sumLength += flowTextLength[i++]
//                            currentReadingPositionInParagraph = oldPos + currentPos
//                        }
//                    }
//                }
//            }
//        })
//
//        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        playbackAttributes = AudioAttributes.Builder()
//            .setUsage(AudioAttributes.USAGE_MEDIA)
//            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//            .build()
//        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
//            .setAudioAttributes(playbackAttributes!!)
//            .setAcceptsDelayedFocusGain(true)
//            .setOnAudioFocusChangeListener(audioFocusChangeListener)
//            .build()
//        audioFocusRequest = audioManager!!.requestAudioFocus(focusRequest!!)
//    }
//    fun setChapterParagraphs(chapterParagraphs: Map<Int, List<String>>) {
//        chapterParagraphsMap = chapterParagraphs
//    }
//
//    fun startReading() {
//        if (!isTtsInitialized) {
//            return
//        }
//        if (chapterParagraphsMap.isEmpty()) {
//            return
//        }
//        callbackForUpdateState(true,false)
//        currentReadingPositionInParagraph = 0
//        currentParagraphIndex = contentState.value.firstVisibleItemIndex
//        startSpeakCurrentParagraph()
//    }
//
//    fun pauseReading() {
//        if (textToSpeech?.isSpeaking == true) {
//            textToSpeech?.stop()
//        }
//    }
//
//    fun resumeReading() {
//        startSpeakCurrentParagraph()
//    }
//
//    fun stopReading() {
//        if (textToSpeech?.isSpeaking == true) {
//            textToSpeech?.stop()
//            currentParagraphIndex = 0
//            currentReadingPositionInParagraph = 0
//            callbackForUpdateState(false,false)
//        }
//    }
//
//    private fun startSpeakCurrentParagraph() {
//        if (contentState.value.currentChapterIndex != -1) {
//            val currentChapterParagraphs = chapterParagraphsMap[contentState.value.currentChapterIndex]
//            if (currentChapterParagraphs != null && currentParagraphIndex < currentChapterParagraphs.size) {
//                i = 0
//                val text = currentChapterParagraphs[currentParagraphIndex]
//                textToSpeakNow = text.substring(currentReadingPositionInParagraph)
//                flowTextLength = processTextLength(
//                    text = text,
//                    maxWidth = contentState.value.screenWidth,
//                    maxHeight = contentState.value.screenHeight,
//                    textStyle = TextStyle(
//                        textIndent = if(fontState.value.textIndent)
//                            TextIndent(firstLine = (fontState.value.fontSize * 2).sp)
//                        else
//                            TextIndent.None,
//                        textAlign = if(fontState.value.textAlign) TextAlign.Justify else TextAlign.Left,
//                        fontSize = fontState.value.fontSize.sp,
//                        fontFamily = fontState.value.fontFamilies[fontState.value.selectedFontFamilyIndex],
//                        lineBreak = LineBreak.Paragraph,
//                        lineHeight = (fontState.value.fontSize + fontState.value.lineSpacing).sp
//                    ),
//                    textMeasurer = textMeasurer
//                )
//                oldPos = text.length - textToSpeakNow.length
//                sumLength = flowTextLength[i]
//                textToSpeech?.speak(textToSpeakNow, TextToSpeech.QUEUE_FLUSH, null, "paragraph_${contentState.value.currentChapterIndex}_${currentParagraphIndex}")
//            } else {
//                moveToNextChapterOrStop()
//            }
//        }
//    }
//
//    fun playNextParagraphOrChapter() {
//        if (contentState.value.currentChapterIndex != -1) {
//            val currentChapterParagraphs = chapterParagraphsMap[contentState.value.currentChapterIndex]
//            if (currentChapterParagraphs != null && currentParagraphIndex < currentChapterParagraphs.size - 1) {
//                currentParagraphIndex++
//                callbackForNextChapter(contentState.value.currentChapterIndex,currentParagraphIndex)
//                currentReadingPositionInParagraph = 0
//                if(ttsState.value.isSpeaking && !ttsState.value.isPaused)
//                    startSpeakCurrentParagraph()
//            } else {
//                moveToNextChapterOrStop()
//            }
//        }
//    }
//
//    fun playPreviousParagraphOrChapter() {
//        if (contentState.value.currentChapterIndex != -1) {
//            val currentChapterParagraphs = chapterParagraphsMap[contentState.value.currentChapterIndex]
//            if (currentChapterParagraphs != null && currentParagraphIndex-1 >= 0) {
//                currentParagraphIndex--
//                callbackForNextChapter(contentState.value.currentChapterIndex,currentParagraphIndex)
//                currentReadingPositionInParagraph = 0
//                if(ttsState.value.isSpeaking && !ttsState.value.isPaused)
//                    startSpeakCurrentParagraph()
//            } else {
//                moveToPreviousChapterOrStop()
//            }
//        }
//    }
//
//    fun moveToNextChapterOrStop() {
//        if (contentState.value.currentChapterIndex + 1 <= contentState.value.totalChapter) {
//            callbackForNextChapter(contentState.value.currentChapterIndex + 1,0)
//            currentReadingPositionInParagraph = 0
//            currentParagraphIndex = 0
//            if(ttsState.value.isSpeaking && !ttsState.value.isPaused)
//                startSpeakCurrentParagraph()
//        } else {
//            stopReading()
//        }
//    }
//
//    fun moveToPreviousChapterOrStop() {
//        if (contentState.value.currentChapterIndex -1 >= 0) {
//            callbackForNextChapter(contentState.value.currentChapterIndex - 1,0)
//            currentReadingPositionInParagraph = 0
//            currentParagraphIndex = 0
//            if(ttsState.value.isSpeaking && !ttsState.value.isPaused)
//                startSpeakCurrentParagraph()
//        } else {
//            stopReading()
//        }
//    }
//
//    fun shutdownTts() {
//        textToSpeech?.stop()
//        textToSpeech?.shutdown()
//        audioManager?.abandonAudioFocusRequest(focusRequest!!)
//        isTtsInitialized = false
//    }
//
//    private fun processTextLength(
//        text: String,
//        maxWidth: Int,
//        maxHeight: Int,
//        textStyle: TextStyle,
//        textMeasurer: TextMeasurer,
//    ): SnapshotStateList<Int> {
//        var longText = text
//        val subStringLength = mutableStateListOf<Int>()
//        while (longText.isNotEmpty()) {
//            val measuredLayoutResult = textMeasurer.measure(
//                text = longText,
//                style = textStyle,
//                overflow = TextOverflow.Ellipsis,
//                constraints = Constraints(
//                    maxWidth = maxWidth,
//                    maxHeight = maxHeight
//                ),
//            )
//            if(measuredLayoutResult.hasVisualOverflow){
//                val lastVisibleCharacterIndex = measuredLayoutResult.getLineEnd(
//                    lineIndex = measuredLayoutResult.lineCount - 1,
//                    visibleEnd = true
//                )
//                val endIndex = minOf(lastVisibleCharacterIndex, longText.length)
//                val endSubString = longText.substring(0, endIndex)
//                subStringLength.add(endSubString.trim().length)
//                longText = longText.replaceFirst(endSubString, "", true)
//            }else{
//                subStringLength.add(longText.trim().length)
//                longText = ""
//            }
//        }
//        return subStringLength
//    }
//
//    fun setPitch(currentPitch: Float) {
//        textToSpeech?.setPitch(currentPitch)
//    }
//
//    fun setSpeechRate(currentSpeed: Float) {
//        textToSpeech?.setSpeechRate(currentSpeed)
//    }
//
//    fun setVoice(currentVoice: Voice) {
//        textToSpeech?.setVoice(currentVoice)
//    }
//
//    fun setLanguage(currentLanguage: Locale) {
//        textToSpeech?.setLanguage(currentLanguage)
//    }
//
//    fun updateTTS(){
//        textToSpeech?.language = ttsState.value.currentLanguage?: Locale.getDefault()
//        textToSpeech?.voice = ttsState.value.currentVoice?: textToSpeech?.defaultVoice
//        textToSpeech?.setSpeechRate(ttsState.value.currentSpeed?: 1f)
//        textToSpeech?.setPitch(ttsState.value.currentPitch?: 1f)
//    }
//}