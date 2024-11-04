package com.capstone.bookshelf.feature.readbook.presentation.component.bottomBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.capstone.bookshelf.R

@ExperimentalAnimationApi
@Composable
fun BottomBar(
    bottomBarState: Boolean,
//    drawerLazyColumnState: LazyListState,
//    tts: MutableState<TextToSpeech?>,
    isSpeaking: Boolean,
//    isPaused: Boolean,
//    contentLazyColumnState: LazyListState?,
//    currentChapterIndex: Int,
//    currentReadingItemIndex: Int,
//    currentPosition: Int,
//    contentParagraphList: Array<List<String>?>,
//    maxWidth: Int,
//    maxHeight: Int,
//    textStyle: TextStyle,
//    textMeasurer: TextMeasurer,
//    shouldScroll: Boolean,
//    readingState: (Boolean, Boolean, Int, Int, Int, Boolean, Int) -> Unit
) {
    val iconList = listOf(
        R.drawable.ic_previous_chapter,
        R.drawable.ic_previous,
        R.drawable.ic_next,
        R.drawable.ic_next_chapter,
        R.drawable.ic_play,
        R.drawable.ic_pause,
        R.drawable.ic_stop,
    )
    AnimatedVisibility(
        visible = bottomBarState,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(
                    onClick = {
//                        if (isSpeaking) {
//                            stopReading(tts)
//                            readingState(false,false, 0, maxOf(currentChapterIndex - 1,0), 0, shouldScroll, 0)
//                        }else
//                            readingState(false,false, 0, maxOf(currentChapterIndex - 1,0), 0, shouldScroll, 0)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = iconList[0]),
                        contentDescription = "previous chapter"
                    )
                }
                IconButton(
                    onClick = {
//                        if (isSpeaking) {
//                            readNextParagraph(
//                                tts = tts,
//                                contentParagraphList = contentParagraphList,
//                                targetParagraphIndex = maxOf(currentReadingItemIndex - 1,0),
//                                currentChapterIndex = currentChapterIndex,
//                                currentPosition = 0,
//                                isReading = true,
//                                maxWidth = maxWidth,
//                                maxHeight = maxHeight,
//                                textStyle = textStyle,
//                                textMeasurer = textMeasurer,
//                                shouldScroll = shouldScroll
//                            ) { index, chapterIndex, currentPos,scroll,times,stopReading ->
//                                readingState(false, stopReading, index, chapterIndex, currentPos, scroll, times)
//                            }
//                        } else if(isPaused){
//                            readingState(true,false, maxOf(currentReadingItemIndex - 1,0), currentChapterIndex, 0, shouldScroll, 0)
//                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = iconList[1]),
                        contentDescription = "previous paragraph"
                    )
                }
                IconButton(
                    onClick = {
//                        if (isSpeaking){
//                            stopReading(tts = tts)
//                            readingState(true,false, currentReadingItemIndex,currentChapterIndex,currentPosition, shouldScroll, 0)
//                        } else if(isPaused){
//                            readNextParagraph(
//                                tts = tts,
//                                contentParagraphList = contentParagraphList,
//                                targetParagraphIndex = currentReadingItemIndex,
//                                currentChapterIndex = currentChapterIndex,
//                                currentPosition = currentPosition,
//                                isReading = true,
//                                maxWidth = maxWidth,
//                                maxHeight = maxHeight,
//                                textStyle = textStyle,
//                                textMeasurer = textMeasurer,
//                                shouldScroll = shouldScroll
//                            ) { index,chapterIndex,currentPos, scroll,times,stopReading ->
//                                readingState(false,stopReading, index, chapterIndex, currentPos, scroll,times)
//                            }
//                        }else{
//                            if (contentLazyColumnState != null) {
//                                readNextParagraph(
//                                    tts = tts,
//                                    contentParagraphList = contentParagraphList,
//                                    targetParagraphIndex = contentLazyColumnState.firstVisibleItemIndex,
//                                    currentChapterIndex = currentChapterIndex,
//                                    currentPosition = 0,
//                                    isReading = true,
//                                    maxWidth = maxWidth,
//                                    maxHeight = maxHeight,
//                                    textStyle = textStyle,
//                                    textMeasurer = textMeasurer,
//                                    shouldScroll = shouldScroll
//                                ) { index,chapterIndex,currentPos,scroll,times,stopReading ->
//                                    readingState(false,stopReading, index, chapterIndex, currentPos, scroll,times)
//                                }
//                            }
//                        }
                    }
                ) {
                    if(isSpeaking)
                        Icon(
                            painter = painterResource(id = iconList[5]),
                            contentDescription = "Play/Pause"
                        )
                    else
                        Icon(
                            painter = painterResource(id = iconList[4]),
                            contentDescription = "Play/Pause"
                        )
                }
                IconButton(
                    onClick = {
//                        if (isSpeaking) {
//                            if (contentLazyColumnState != null) {
//                                readNextParagraph(
//                                    tts = tts,
//                                    contentParagraphList = contentParagraphList,
//                                    targetParagraphIndex = minOf(currentReadingItemIndex + 1,contentLazyColumnState.layoutInfo.totalItemsCount-1),
//                                    currentChapterIndex = currentChapterIndex,
//                                    currentPosition = 0,
//                                    isReading = true,
//                                    maxWidth = maxWidth,
//                                    maxHeight = maxHeight,
//                                    textStyle = textStyle,
//                                    textMeasurer = textMeasurer,
//                                    shouldScroll = shouldScroll
//                                ) { index, chapterIndex, currentPos,scroll,times,stopReading ->
//                                    readingState(false, stopReading, index, chapterIndex, currentPos, scroll,times)
//                                }
//                            }
//                        }else if (isPaused){
//                            if (contentLazyColumnState != null) {
//                                readingState(true,false, minOf(currentReadingItemIndex + 1,contentLazyColumnState.layoutInfo.totalItemsCount-1), currentChapterIndex, 0, shouldScroll, 0)
//                            }
//                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = iconList[2]),
                        contentDescription = "next paragraph"
                    )
                }
                IconButton(
                    onClick = {
//                        if (isSpeaking) {
//                            stopReading(tts)
//                            readingState(false,false, 0, minOf(currentChapterIndex + 1,drawerLazyColumnState.layoutInfo.totalItemsCount-1), 0, shouldScroll, 0)
//                        }else
//                            readingState(false,false, 0, minOf(currentChapterIndex + 1,drawerLazyColumnState.layoutInfo.totalItemsCount-1), 0, shouldScroll, 0)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = iconList[3]),
                        contentDescription = "Next chapter"
                    )
                }
                IconButton(
                    onClick = {
//                        if (isSpeaking||isPaused) {
//                            stopReading(tts)
//                            readingState(false,false, currentReadingItemIndex, currentChapterIndex, 0, shouldScroll, 0)
//                        }else
//                            readingState(false,false, currentReadingItemIndex, currentChapterIndex, 0, shouldScroll, 0)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = iconList[6]),
                        contentDescription = "Stop"
                    )
                }
            }

        }
    }
}