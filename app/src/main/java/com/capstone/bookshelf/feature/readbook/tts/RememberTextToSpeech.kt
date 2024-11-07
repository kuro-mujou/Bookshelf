package com.capstone.bookshelf.feature.readbook.tts

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


@Composable
fun rememberTextToSpeech(): MutableState<TextToSpeech?> {
    val context = LocalContext.current
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale("vi_VN")
                setPreferredVoice(tts.value)
            }
        }
        tts.value = textToSpeech

        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }
    return tts
}

fun readNextParagraph(
    tts: MutableState<TextToSpeech?>,
    contentParagraphList: Array<List<String>?>,
    targetParagraphIndex: Int,
    currentChapterIndex: Int,
    currentPosition: Int,
    isReading: Boolean,
    maxWidth: Int,
    maxHeight: Int,
    textStyle: TextStyle,
    textMeasurer: TextMeasurer,
    shouldScroll: Boolean,
    nextParagraph: (Int, Int, Int, Boolean, Int, Boolean) -> Unit
) {
    val paragraphList = contentParagraphList[currentChapterIndex]
    if (paragraphList != null) {
        if (targetParagraphIndex in paragraphList.indices) {
            val currentText = paragraphList[targetParagraphIndex]
            val textToSpeakNow = currentText.substring(currentPosition)
            val oldPos = currentText.length - textToSpeakNow.length
            var i = 0
            var sumLength: Int
            val flowTextLength = processTextLength(
                text = currentText,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                textStyle = textStyle,
                textMeasurer = textMeasurer
            )
            sumLength = flowTextLength[i]
            tts.value?.let { textToSpeech ->
                // Set speech rate and pitch for more natural sound
                textToSpeech.setSpeechRate(0.9f)  // Slightly slower than default
                textToSpeech.setPitch(1.1f)       // Slightly higher pitch
                textToSpeech.speak(textToSpeakNow, TextToSpeech.QUEUE_FLUSH, null, "utteranceId-$targetParagraphIndex")
            }
            tts.value?.setOnUtteranceProgressListener(
                object : UtteranceProgressListener(){
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        if (targetParagraphIndex+1 < paragraphList.size) {
                            readNextParagraph(
                                tts = tts,
                                contentParagraphList = contentParagraphList,
                                targetParagraphIndex = targetParagraphIndex+1,
                                currentChapterIndex = currentChapterIndex,
                                nextParagraph = nextParagraph,
                                currentPosition = 0,
                                isReading = isReading,
                                maxWidth = maxWidth,
                                maxHeight = maxHeight,
                                textStyle = textStyle,
                                textMeasurer = textMeasurer,
                                shouldScroll = shouldScroll
                            )
                        }else{
                            if(currentChapterIndex+1 < contentParagraphList.size) {
                                nextParagraph(
                                    0,
                                    currentChapterIndex + 1,
                                    currentPosition,
                                    shouldScroll,
                                    i,
                                    true
                                )
                                stopReading(tts)
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(1000)
                                    readNextParagraph(
                                        tts = tts,
                                        contentParagraphList = contentParagraphList,
                                        targetParagraphIndex = 0,
                                        currentChapterIndex = currentChapterIndex + 1,
                                        nextParagraph = nextParagraph,
                                        currentPosition = 0,
                                        isReading = isReading,
                                        maxWidth = maxWidth,
                                        maxHeight = maxHeight,
                                        textStyle = textStyle,
                                        textMeasurer = textMeasurer,
                                        shouldScroll = shouldScroll
                                    )
                                }
                            }else{
                                nextParagraph(
                                    targetParagraphIndex,
                                    currentChapterIndex,
                                    currentPosition,
                                    shouldScroll,
                                    i,
                                    false
                                )
                                stopReading(tts)
                            }
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {}
                    override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                        super.onRangeStart(utteranceId, start, end, frame)
                        if(isReading) {
                            val currentPos = textToSpeakNow.substring(0, end).length
                            nextParagraph(
                                targetParagraphIndex,
                                currentChapterIndex,
                                oldPos + currentPos,
                                shouldScroll,
                                i,
                                true
                            )
                            if (flowTextLength.size > 1) {
                                if (oldPos + currentPos > sumLength) {
                                    nextParagraph(
                                        targetParagraphIndex,
                                        currentChapterIndex,
                                        oldPos + currentPos,
                                        !shouldScroll,
                                        i + 1,
                                        true
                                    )
                                    sumLength += flowTextLength[i++]
                                }
                            }
                        }
                    }
                }
            )
        }else{
            nextParagraph(
                0,
                currentChapterIndex,
                currentPosition,
                shouldScroll,
                0,
                false
            )
            stopReading(tts)
        }
    }
}

fun stopReading(
    tts: MutableState<TextToSpeech?>
) {
    if (tts.value?.isSpeaking == true) {
        tts.value?.stop()
    }
}

fun setPreferredVoice(tts: TextToSpeech?) {
    tts?.let { textToSpeech ->
        val voices = textToSpeech.voices
        val preferredVoice = voices.firstOrNull { voice ->
            voice.name.contains("female", ignoreCase = true) &&
                    voice.name.contains("vi-vn", ignoreCase = true)
        } ?: voices.firstOrNull { it.locale == Locale("vi", "VN") }

        preferredVoice?.let {
            textToSpeech.voice = it
        }
    }
}
fun processTextLength(
    text: String,
    maxWidth: Int,
    maxHeight: Int,
    textStyle: TextStyle,
    textMeasurer: TextMeasurer,
): SnapshotStateList<Int> {
    var longText = text
    val subStringLength = mutableStateListOf<Int>()
    while (longText.isNotEmpty()) {
        val measuredLayoutResult = textMeasurer.measure(
            text = longText,
            style = textStyle,
            overflow = TextOverflow.Ellipsis,
            constraints = Constraints(
                maxWidth = maxWidth,
                maxHeight = maxHeight
            ),
        )
        if(measuredLayoutResult.hasVisualOverflow){
            val lastVisibleCharacterIndex = measuredLayoutResult.getLineEnd(
                lineIndex = measuredLayoutResult.lineCount - 1,
                visibleEnd = true
            )
            val endIndex = minOf(lastVisibleCharacterIndex, longText.length)
            val endSubString = longText.substring(0, endIndex)
            subStringLength.add(endSubString.trim().length)
            longText = longText.replaceFirst(endSubString, "", true)
        }else{
            subStringLength.add(longText.trim().length)
            longText = ""
        }
    }
    return subStringLength
}