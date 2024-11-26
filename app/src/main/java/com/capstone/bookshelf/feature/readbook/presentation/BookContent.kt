package com.capstone.bookshelf.feature.readbook.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.capstone.bookshelf.core.domain.ChapterContentEntity
import com.capstone.bookshelf.feature.readbook.presentation.component.bottomBar.BottomBarAutoScroll
import com.capstone.bookshelf.feature.readbook.presentation.component.bottomBar.BottomBarDefault
import com.capstone.bookshelf.feature.readbook.presentation.component.bottomBar.BottomBarSetting
import com.capstone.bookshelf.feature.readbook.presentation.component.bottomBar.BottomBarTTS
import com.capstone.bookshelf.feature.readbook.presentation.component.bottomBar.BottomBarTheme
import com.capstone.bookshelf.feature.readbook.presentation.component.content.HeaderContent
import com.capstone.bookshelf.feature.readbook.presentation.component.content.HeaderText
import com.capstone.bookshelf.feature.readbook.presentation.component.content.ImageComponent
import com.capstone.bookshelf.feature.readbook.presentation.component.content.ImageContent
import com.capstone.bookshelf.feature.readbook.presentation.component.content.ParagraphContent
import com.capstone.bookshelf.feature.readbook.presentation.component.content.ParagraphText
import com.capstone.bookshelf.feature.readbook.presentation.component.drawer.NavigationDrawer
import com.capstone.bookshelf.feature.readbook.presentation.component.topBar.TopBar
import com.capstone.bookshelf.feature.readbook.presentation.state.ContentUIState
import com.capstone.bookshelf.feature.readbook.presentation.state.TTSState
import com.capstone.bookshelf.feature.readbook.tts.readNextParagraph
import com.capstone.bookshelf.feature.readbook.tts.rememberTextToSpeech
import com.capstone.bookshelf.feature.readbook.tts.stopReading
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BookContent(
    bookContentViewModel: BookContentViewModel,
    onBackIconClick: (Int) -> Unit
){
    val context = LocalContext.current
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val wakeLock: PowerManager.WakeLock = remember {
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BookShelf::TTSWakeLock")
    }
    val book by bookContentViewModel.book
    val uiState by bookContentViewModel.contentUIState.collectAsState()
    val ttsState by bookContentViewModel.ttsUiState.collectAsState()

    var triggerLoadChapter by remember { mutableStateOf(false) }
    var callbackLoadChapter by remember { mutableStateOf(false) }

    val textToSpeech = rememberTextToSpeech(context,wakeLock,ttsState)
    val textMeasurer = rememberTextMeasurer()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val pagerState = rememberPagerState(
        initialPage = uiState.currentChapterIndex,
        pageCount = { uiState.totalChapter }
    )

    val lazyListStates = remember { mutableStateMapOf<Int, LazyListState>() }
    val chapterContents = remember { mutableStateMapOf<Int, List<String>>() }
    val headers = remember{ mutableStateMapOf<Int,String>()}

    var currentLazyColumnState by remember { mutableStateOf<LazyListState?>(null) }

    val drawerLazyColumnState = rememberLazyListState()

    val scope = rememberCoroutineScope()


    val textStyle = TextStyle(
        textIndent = TextIndent(firstLine = 40.sp),
        textAlign = TextAlign.Left,
        fontSize = 24.sp,
        background = Color(0x80e2e873),
        lineBreak = LineBreak.Paragraph,
    )
    LaunchedEffect(Unit){
        drawerLazyColumnState.scrollToItem(uiState.currentChapterIndex)
        pagerState.animateScrollToPage(uiState.currentChapterIndex)
    }
    if (uiState.screenShallBeKeptOn) {
        KeepScreenOn()
    }
    NavigationDrawer(
        bookContentViewModel = bookContentViewModel,
        uiState = uiState,
        book = book,
        drawerState = drawerState,
        drawerLazyColumnState = drawerLazyColumnState,
        onDrawerItemClick = { chapterIndex ->
            bookContentViewModel.updateCurrentChapterIndex(chapterIndex)
            bookContentViewModel.updateDrawerState(false)
        }
    ){
        LaunchedEffect(ttsState.isSpeaking) {
            bookContentViewModel.updateIsFocused(!ttsState.isSpeaking && ttsState.isPaused || ttsState.isSpeaking && !ttsState.isPaused)
        }
        LaunchedEffect(ttsState.isPaused) {
            bookContentViewModel.updateIsFocused(!(!ttsState.isSpeaking && !ttsState.isPaused))
        }
        LaunchedEffect(drawerState.currentValue) {
            if(drawerState.currentValue == DrawerValue.Closed) {
                bookContentViewModel.updateDrawerState(false)
                drawerLazyColumnState.scrollToItem(uiState.currentChapterIndex)
            }
        }
        LaunchedEffect(uiState.currentChapterIndex) {
            drawerLazyColumnState.scrollToItem(uiState.currentChapterIndex)
            pagerState.animateScrollToPage(uiState.currentChapterIndex)
        }
        LaunchedEffect(uiState.drawerState){
            if(uiState.drawerState){
                drawerState.open()
            }else{
                drawerState.close()
                drawerLazyColumnState.animateScrollToItem(uiState.currentChapterIndex)
                pagerState.animateScrollToPage(uiState.currentChapterIndex)
            }
        }
        LaunchedEffect(uiState.bottomBarState){
            if(!uiState.bottomBarState){
                delay(300)
                if(ttsState.isSpeaking||ttsState.isPaused){
                    bookContentViewModel.updateBottomBarIndex(3)
                }else if(ttsState.isAutoScroll||ttsState.isAutoScrollPaused){
                    bookContentViewModel.updateBottomBarIndex(4)
                }else {
                    bookContentViewModel.changeMenuTriggerSetting(false)
                    bookContentViewModel.updateBottomBarIndex(0)
                }
            }
        }
        LaunchedEffect(ttsState.currentLanguage) {
            if(ttsState.currentLanguage != null){
                textToSpeech.setLanguage(ttsState.currentLanguage)
                bookContentViewModel.updateBookSettingLocale(ttsState.currentLanguage!!.displayName)
            }
        }
        LaunchedEffect(ttsState.currentVoice) {
            if(ttsState.currentVoice != null){
                textToSpeech.setVoice(ttsState.currentVoice)
                bookContentViewModel.updateBookSettingVoice(ttsState.currentVoice!!.name)
            }
        }
        LaunchedEffect(ttsState.currentSpeed){
            if(ttsState.currentSpeed != null){
                textToSpeech.setSpeechRate(ttsState.currentSpeed!!)
                bookContentViewModel.updateBookSettingSpeed(ttsState.currentSpeed!!)
            }
        }
        LaunchedEffect(ttsState.currentPitch){
            if(ttsState.currentPitch != null){
                textToSpeech.setPitch(ttsState.currentPitch!!)
                bookContentViewModel.updateBookSettingPitch(ttsState.currentPitch!!)
            }
        }
        Scaffold(
//            floatingActionButton = {
//                FloatingActionButton(
//                    onClick = {
//                        scope.launch {
//                            slowScrollToBottom(
//                                currentLazyColumnState!!,
//                                ttsUiState = ttsState,
//                                uiState = uiState
//                            )
//                        }
//                    },
//                    content = {
//
//                    }
//                )
//            },
            topBar = {
                TopBar(
                    topBarState = uiState.topBarState,
                    onMenuIconClick ={
                        bookContentViewModel.updateDrawerState(true)
                        bookContentViewModel.updateTopBarState(false)
                        bookContentViewModel.updateBottomBarState(false)
                    },
                    onBackIconClick = {
                        onBackIconClick(uiState.currentChapterIndex+1)
                        textToSpeech.stop()
                        textToSpeech.shutdown()
                    }
                )
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = uiState.bottomBarState,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                ) {
                    when(uiState.bottomBarIndex) {
                        0 -> {
                            BottomBarDefault(
                                uiState = uiState,
                                onThemeIconClick = {
                                    scope.launch {
                                        bookContentViewModel.updateBottomBarState(false)
                                        delay(200)
                                        bookContentViewModel.updateBottomBarIndex(1)
                                        bookContentViewModel.updateBottomBarState(true)
                                    }
                                },
                                onTTSIconClick = {
                                    scope.launch {
                                        bookContentViewModel.loadTTSSetting(textToSpeech)
                                        bookContentViewModel.updateIsPaused(false)
                                        bookContentViewModel.updateIsSpeaking(true)
                                        bookContentViewModel.updateBottomBarState(false)
                                        delay(200)
                                        bookContentViewModel.updateBottomBarIndex(3)
                                        bookContentViewModel.updateBottomBarState(true)
                                        bookContentViewModel.updateEnablePagerScroll(false)
                                        readNextParagraph(
                                            wakeLock = wakeLock,
                                            tts = textToSpeech,
                                            uiState = uiState,
                                            ttsState = ttsState,
                                            chapterContents = chapterContents,
                                            targetParagraphIndex = currentLazyColumnState?.firstVisibleItemIndex!!,
                                            currentChapterIndex = uiState.currentChapterIndex,
                                            currentPosition = 0,
                                            isReading = true,
                                            maxWidth = uiState.screenWidth,
                                            maxHeight = uiState.screenHeight,
                                            textStyle = textStyle,
                                            textMeasurer = textMeasurer,
                                            shouldScroll = ttsState.flagTriggerScrolling
                                        ) { index,chapterIndex,currentPos,scroll,times,stopReading ->
                                            updateVariable(
                                                bookContentViewModel = bookContentViewModel,
                                                isPaused = false,
                                                isSpeaking = stopReading,
                                                paragraphIndex = index,
                                                chapterIndex = chapterIndex,
                                                currentPos = currentPos,
                                                triggerScroll = scroll,
                                                scrollTimes = times
                                            )
                                        }
                                    }
                                },
                                onAutoScrollIconClick = {
                                    scope.launch {
                                        bookContentViewModel.loadTTSSetting(textToSpeech)
                                        bookContentViewModel.updateBottomBarState(false)
                                        delay(200)
                                        bookContentViewModel.updateBottomBarIndex(4)
                                        bookContentViewModel.updateBottomBarState(true)
                                        bookContentViewModel.updateIsAutoScroll(true)
                                    }
                                },
                                onSettingIconClick = {
                                    scope.launch {
                                        bookContentViewModel.loadTTSSetting(textToSpeech)
                                        bookContentViewModel.updateBottomBarState(false)
                                        delay(200)
                                        bookContentViewModel.updateBottomBarIndex(2)
                                        bookContentViewModel.updateBottomBarState(true)
                                        bookContentViewModel.changeMenuTriggerSetting(true)
                                    }
                                },
                            )
                        }

                        1 -> {
                            BottomBarTheme(
                                uiState = uiState,
                            )
                        }
                        2 -> {
                            BottomBarSetting(
                                bookContentViewModel = bookContentViewModel,
                                uiState = uiState,
                                textToSpeech = textToSpeech,
                                ttsState = ttsState,
                                context = context
                            )
                        }
                        3 -> {
                            BottomBarTTS(
                                bookContentViewModel = bookContentViewModel,
                                textToSpeech = textToSpeech,
                                uiState = uiState,
                                ttsState = ttsState,
                                onPreviousChapterIconClick = {
                                    stopReading(textToSpeech)
                                    updateVariable(
                                        bookContentViewModel = bookContentViewModel,
                                        isPaused = true,
                                        isSpeaking = false,
                                        paragraphIndex = 0,
                                        chapterIndex = maxOf(uiState.currentChapterIndex-1,0),
                                        currentPos = 0,
                                        triggerScroll = false,
                                        scrollTimes = 0
                                    )
                                },
                                onPreviousParagraphIconClick = {
                                    if (ttsState.isSpeaking) {
                                        bookContentViewModel.updateIsSpeaking(true)
                                        bookContentViewModel.updateCurrentReadingPosition(0)
                                        readNextParagraph(
                                            wakeLock = wakeLock,
                                            tts = textToSpeech,
                                            uiState = uiState,
                                            ttsState = ttsState,
                                            chapterContents = chapterContents,
                                            targetParagraphIndex = maxOf(ttsState.currentReadingParagraph - 1,0),
                                            currentChapterIndex = uiState.currentChapterIndex,
                                            currentPosition = 0,
                                            isReading = true,
                                            maxWidth = uiState.screenWidth,
                                            maxHeight = uiState.screenHeight,
                                            textStyle = textStyle,
                                            textMeasurer = textMeasurer,
                                            shouldScroll = ttsState.flagTriggerScrolling,
                                        ) { index, chapterIndex, currentPos,scroll,times,stopReading ->
                                            updateVariable(
                                                bookContentViewModel = bookContentViewModel,
                                                isPaused = false,
                                                isSpeaking = stopReading,
                                                paragraphIndex = index,
                                                chapterIndex = chapterIndex,
                                                currentPos = currentPos,
                                                triggerScroll = scroll,
                                                scrollTimes = times
                                            )
                                        }
                                    } else if(ttsState.isPaused){
                                        updateVariable(
                                            bookContentViewModel = bookContentViewModel,
                                            isPaused = true,
                                            isSpeaking = false,
                                            paragraphIndex = maxOf(ttsState.currentReadingParagraph - 1,0),
                                            chapterIndex = uiState.currentChapterIndex,
                                            currentPos = 0,
                                            triggerScroll = ttsState.flagTriggerScrolling,
                                            scrollTimes = 0
                                        )
                                    }
                                },
                                onPlayPauseIconClick = {
                                    if (ttsState.isSpeaking){
                                        stopReading(tts = textToSpeech)
                                        updateVariable(
                                            bookContentViewModel = bookContentViewModel,
                                            isPaused = true,
                                            isSpeaking = false,
                                            paragraphIndex = ttsState.currentReadingParagraph,
                                            chapterIndex = uiState.currentChapterIndex,
                                            currentPos = uiState.currentReadingPosition,
                                            triggerScroll = ttsState.flagTriggerScrolling,
                                            scrollTimes = 0
                                        )
                                    } else if(ttsState.isPaused){
                                        bookContentViewModel.updateIsSpeaking(true)
                                        bookContentViewModel.updateIsPaused(false)
                                        readNextParagraph(
                                            wakeLock = wakeLock,
                                            tts = textToSpeech,
                                            uiState = uiState,
                                            ttsState = ttsState,
                                            chapterContents = chapterContents,
                                            targetParagraphIndex = ttsState.currentReadingParagraph,
                                            currentChapterIndex = uiState.currentChapterIndex,
                                            currentPosition = uiState.currentReadingPosition,
                                            isReading = true,
                                            maxWidth = uiState.screenWidth,
                                            maxHeight = uiState.screenHeight,
                                            textStyle = textStyle,
                                            textMeasurer = textMeasurer,
                                            shouldScroll = ttsState.flagTriggerScrolling
                                        ) { index, chapterIndex, currentPos,scroll,times,stopReading ->
                                            updateVariable(
                                                bookContentViewModel = bookContentViewModel,
                                                isPaused = false,
                                                isSpeaking = stopReading,
                                                paragraphIndex = index,
                                                chapterIndex = chapterIndex,
                                                currentPos = currentPos,
                                                triggerScroll = scroll,
                                                scrollTimes = times
                                            )
                                        }
                                    }
                                },
                                onNextParagraphIconClick = {
                                    if (ttsState.isSpeaking) {
                                        bookContentViewModel.updateIsPaused(false)
                                        bookContentViewModel.updateIsSpeaking(true)
                                        readNextParagraph(
                                            wakeLock = wakeLock,
                                            tts = textToSpeech,
                                            uiState = uiState,
                                            ttsState = ttsState,
                                            chapterContents = chapterContents,
                                            targetParagraphIndex = minOf(ttsState.currentReadingParagraph + 1,currentLazyColumnState?.layoutInfo?.totalItemsCount!! - 1),
                                            currentChapterIndex = uiState.currentChapterIndex,
                                            currentPosition = 0,
                                            isReading = true,
                                            maxWidth = uiState.screenWidth,
                                            maxHeight = uiState.screenHeight,
                                            textStyle = textStyle,
                                            textMeasurer = textMeasurer,
                                            shouldScroll = ttsState.flagTriggerScrolling
                                        ) { index, chapterIndex, currentPos,scroll,times,stopReading ->
                                            updateVariable(
                                                bookContentViewModel = bookContentViewModel,
                                                isPaused = false,
                                                isSpeaking = stopReading,
                                                paragraphIndex = index,
                                                chapterIndex = chapterIndex,
                                                currentPos = currentPos,
                                                triggerScroll = scroll,
                                                scrollTimes = times
                                            )
                                        }
                                    }else if (ttsState.isPaused){
                                        updateVariable(
                                            bookContentViewModel = bookContentViewModel,
                                            isPaused = true,
                                            isSpeaking = false,
                                            paragraphIndex = minOf(ttsState.currentReadingParagraph + 1,currentLazyColumnState?.layoutInfo?.totalItemsCount!! - 1),
                                            chapterIndex = uiState.currentChapterIndex,
                                            currentPos = 0,
                                            triggerScroll = ttsState.flagTriggerScrolling,
                                            scrollTimes = 0
                                        )
                                    }
                                },
                                onNextChapterIconClick = {
                                    stopReading(textToSpeech)
                                    updateVariable(
                                        bookContentViewModel = bookContentViewModel,
                                        isPaused = true,
                                        isSpeaking = false,
                                        paragraphIndex = 0,
                                        chapterIndex = minOf(uiState.currentChapterIndex+1,uiState.totalChapter-1),
                                        currentPos = 0,
                                        triggerScroll = false,
                                        scrollTimes = 0
                                    )
                                },
                                onTimerIconClick = {

                                },
                                onStopIconClick = {
                                    stopReading(textToSpeech)
                                    updateVariable(
                                        bookContentViewModel = bookContentViewModel,
                                        isPaused = false,
                                        isSpeaking = false,
                                        paragraphIndex = ttsState.currentReadingParagraph,
                                        chapterIndex = uiState.currentChapterIndex,
                                        currentPos = 0,
                                        triggerScroll = ttsState.flagTriggerScrolling,
                                        scrollTimes = 0
                                    )
                                    bookContentViewModel.updateEnablePagerScroll(true)
                                    bookContentViewModel.updateBottomBarState(false)
                                    bookContentViewModel.updateTopBarState(false)
                                },
                                onTTSSettingIconClick = {
                                    bookContentViewModel.changeMenuTriggerVoice(!uiState.openTTSVoiceMenu)
                                    stopReading(tts = textToSpeech)
                                    updateVariable(
                                        bookContentViewModel = bookContentViewModel,
                                        isPaused = true,
                                        isSpeaking = false,
                                        paragraphIndex = ttsState.currentReadingParagraph,
                                        chapterIndex = uiState.currentChapterIndex,
                                        currentPos = uiState.currentReadingPosition,
                                        triggerScroll = ttsState.flagTriggerScrolling,
                                        scrollTimes = 0
                                    )
                                }
                            )
                        }
                        4 ->{
                            BottomBarAutoScroll(
                                bookContentViewModel = bookContentViewModel,
                                uiState = uiState,
                                ttsState = ttsState,
                                onPreviousChapterIconClick ={
                                    bookContentViewModel.updateCurrentChapterIndex(maxOf(uiState.currentChapterIndex-1,0))
                                },
                                onPlayPauseIconClick = {
                                    if(ttsState.isAutoScroll){
                                        bookContentViewModel.updateIsAutoScroll(false)
                                        bookContentViewModel.updateIsAutoScrollPaused(true)
                                    }else if(ttsState.isAutoScrollPaused){
                                        bookContentViewModel.updateIsAutoScroll(true)
                                        bookContentViewModel.updateIsAutoScrollPaused(false)
                                    }
                                },
                                onNextChapterIconClick = {
                                    bookContentViewModel.updateCurrentChapterIndex(minOf(uiState.currentChapterIndex+1,uiState.totalChapter-1))
                                },
                                onStopIconClick = {
                                    bookContentViewModel.updateIsAutoScroll(false)
                                    bookContentViewModel.updateIsAutoScrollPaused(false)
                                    bookContentViewModel.updateBottomBarState(false)
                                    bookContentViewModel.updateTopBarState(false)
                                },
                                onSettingIconClick = {
                                    bookContentViewModel.changeMenuTriggerAutoScroll(true)
                                },
                            )
                        }
                    }
                }
            }
        ){
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (uiState.enableScaffoldBar)
                            Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {
                                bookContentViewModel.updateBottomBarState(!uiState.bottomBarState)
                                bookContentViewModel.updateTopBarState(!uiState.topBarState)
                            }
                        else
                            Modifier
                    ),
            ) {
                LaunchedEffect(pagerState.currentPage) {
                    val currentPage = pagerState.currentPage
                    lazyListStates.keys.filter { pageIndex ->
                        pageIndex < currentPage - 2 || pageIndex > currentPage + 2
                    }.forEach { pageIndex ->
                        lazyListStates.remove(pageIndex)
                    }
                    headers.keys.filter { pageIndex ->
                        pageIndex < currentPage - 2 || pageIndex > currentPage + 2
                    }.forEach { pageIndex ->
                        headers.remove(pageIndex)
                    }
                    chapterContents.keys.filter { pageIndex ->
                        pageIndex < currentPage - 2 || pageIndex > currentPage + 2
                    }.forEach { pageIndex ->
                        chapterContents.remove(pageIndex)
                    }
                }
                LaunchedEffect(callbackLoadChapter, uiState.currentChapterIndex) {
                    if (callbackLoadChapter) {
                        triggerLoadChapter = false
                        callbackLoadChapter = false
                    }
                    currentLazyColumnState = lazyListStates[uiState.currentChapterIndex]
                    bookContentViewModel.updateCurrentChapterHeader(headers[uiState.currentChapterIndex])
                    bookContentViewModel.updateCurrentChapterContent(chapterContents[uiState.currentChapterIndex])
                }
                val beyondBoundsPageCount = 1
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize(),
                    beyondViewportPageCount = beyondBoundsPageCount,
                    userScrollEnabled = uiState.enablePagerScroll,
                    key = { page -> page}
                ) { page ->
                    val newPage by rememberUpdatedState(newValue = page)
                    val lazyListState = lazyListStates.getOrPut(newPage) { LazyListState() }
                    LaunchedEffect(key1 = Unit) {
                        snapshotFlow { Pair(pagerState.isScrollInProgress, abs(pagerState.settledPage - newPage)) }.collectLatest { (scrollInProgress, diff) ->
                            if (!scrollInProgress && (diff in 0..beyondBoundsPageCount)) {
                                if (diff > 0) delay(1000)
                                triggerLoadChapter = true
                                cancel()
                            }
                        }
                    }
                    Chapter(
                        bookContentViewModel = bookContentViewModel,
                        uiState = uiState,
                        ttsUiState = ttsState,
                        totalChapter = uiState.totalChapter,
                        triggerLoadChapter = triggerLoadChapter,
                        textStyle = textStyle,
                        page = newPage,
                        pagerState = pagerState,
                        currentLazyColumnState = currentLazyColumnState,
                        contentLazyColumnState = lazyListState,
                        headers = headers,
                        chapterContents = chapterContents,
                        currentChapter = { chapterIndex, readingIndex ->
                            bookContentViewModel.updateCurrentChapterIndex(chapterIndex)
                            bookContentViewModel.updateCurrentReadingParagraph(readingIndex)
                        },
                        callbackLoadChapter = {
                            callbackLoadChapter = it
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun Chapter(
    bookContentViewModel: BookContentViewModel,
    uiState: ContentUIState,
    ttsUiState: TTSState,
    totalChapter: Int,
    triggerLoadChapter: Boolean,
    textStyle: TextStyle,
    page: Int,
    pagerState: PagerState,
    currentLazyColumnState: LazyListState?,
    contentLazyColumnState: LazyListState,
    headers: MutableMap<Int,String>,
    chapterContents: MutableMap<Int,List<String>>,
    currentChapter: (Int, Int) -> Unit,
    callbackLoadChapter: (Boolean) -> Unit,
) {
    val chapterContent by bookContentViewModel.chapterContent
    var data by remember { mutableStateOf<ChapterContentEntity?>(null) }
    val contentList = remember { mutableStateOf(listOf<@Composable (Int, Boolean, Boolean) -> Unit>())}
    var header by remember { mutableStateOf("") }
    val density = LocalDensity.current

    LaunchedEffect(triggerLoadChapter) {
        if (triggerLoadChapter && data == null) {
            bookContentViewModel.getChapterContent(page)
            data = chapterContent
            parseListToUsableLists(textStyle, data!!.content).also{
                contentList.value = it.first
                chapterContents[page] = it.second
            }
            header = data!!.chapterTitle
            headers[page] = data!!.chapterTitle
            callbackLoadChapter(true)
        }
    }
    LaunchedEffect(pagerState.targetPage) {
        bookContentViewModel.updateFlagTriggerAdjustScroll(false)
        currentChapter(pagerState.targetPage,0)
    }
    LaunchedEffect(currentLazyColumnState) {
        if (currentLazyColumnState != null) {
            snapshotFlow { currentLazyColumnState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { index ->
                    if (index != null) {
                        bookContentViewModel.updateLastVisibleItemIndex(index)
                    }
                }
        }
    }

    LaunchedEffect(currentLazyColumnState) {
        if (currentLazyColumnState != null) {
            snapshotFlow { currentLazyColumnState.layoutInfo.visibleItemsInfo.firstOrNull()?.index }
                .collect { index ->
                    if (index != null) {
                        bookContentViewModel.updateFirstVisibleItemIndex(index)
                    }
                }
        }
    }
    LaunchedEffect(currentLazyColumnState){
        if (currentLazyColumnState != null) {
            snapshotFlow { currentLazyColumnState.isScrollInProgress && !pagerState.isScrollInProgress }.collect { scrolling ->
                if (scrolling && (ttsUiState.isSpeaking || ttsUiState.isPaused) && ttsUiState.currentReadingParagraph == ttsUiState.firstVisibleItemIndex) {
                    bookContentViewModel.updateFlagTriggerAdjustScroll(true)
                }
            }
        }
    }

    LaunchedEffect(ttsUiState.currentReadingParagraph) {
        if ((ttsUiState.currentReadingParagraph >= ttsUiState.lastVisibleItemIndex
                    || ttsUiState.currentReadingParagraph <= ttsUiState.firstVisibleItemIndex)
            && !ttsUiState.flagTriggerScrolling
        ) {
            if(ttsUiState.isSpeaking)
            {
                currentLazyColumnState?.animateScrollToItem(ttsUiState.currentReadingParagraph)
                bookContentViewModel.updateFlagTriggerAdjustScroll(false)
            }
        }
    }

    LaunchedEffect(ttsUiState.flagTriggerScrolling){
        if(ttsUiState.flagTriggerScrolling)
            bookContentViewModel.updateFlagStartScrolling(true)
    }

    LaunchedEffect(ttsUiState.flagStartScrolling){
        if(ttsUiState.flagStartScrolling){
            if(ttsUiState.currentReadingParagraph != ttsUiState.firstVisibleItemIndex){
                currentLazyColumnState?.animateScrollToItem(ttsUiState.currentReadingParagraph)
                bookContentViewModel.updateFlagTriggerAdjustScroll(false)
                bookContentViewModel.updateFlagScrollAdjusted(true)
            }else if(!ttsUiState.flagTriggerAdjustScroll){
                currentLazyColumnState?.animateScrollBy(value = uiState.screenHeight.toFloat())
                bookContentViewModel.updateFlagTriggerAdjustScroll(false)
                bookContentViewModel.updateFlagStartScrolling(false)
            }else{
                bookContentViewModel.updateFlagStartScrolling(true)
            }
        }
    }

    LaunchedEffect(ttsUiState.flagStartAdjustScroll){
        if (ttsUiState.flagStartAdjustScroll) {
            currentLazyColumnState?.animateScrollToItem(ttsUiState.currentReadingParagraph)
            bookContentViewModel.updateFlagTriggerAdjustScroll(false)
            bookContentViewModel.updateFlagStartAdjustScroll(false)
            bookContentViewModel.updateFlagScrollAdjusted(true)
        }
    }

    LaunchedEffect(ttsUiState.flagScrollAdjusted){
        if (ttsUiState.flagScrollAdjusted) {
            currentLazyColumnState?.animateScrollBy(value = uiState.screenHeight.toFloat() * ttsUiState.scrollTime)
            bookContentViewModel.updateFlagTriggerAdjustScroll(false)
            bookContentViewModel.updateFlagScrollAdjusted(false)
            bookContentViewModel.updateFlagStartScrolling(false)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                bookContentViewModel.updateScreenWidth(coordinates.size.width - (with(density) { 32.dp.toPx() }.toInt()))
                bookContentViewModel.updateScreenHeight(coordinates.size.height - (with(density) { 40.dp.toPx() }.toInt()))
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ){
            Text(
                modifier = Modifier.width(with(LocalDensity.current){uiState.screenWidth.toDp() - 100.dp}),
                text = header,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier.width(100.dp),
                text = "${pagerState.currentPage+1} / $totalChapter",
                textAlign = TextAlign.End,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ChapterContents(
            bookContentViewModel = bookContentViewModel,
            isFocused = ttsUiState.isFocused,
            currentReadingItemIndex = ttsUiState.currentReadingParagraph,
            contentLazyColumnState = contentLazyColumnState,
            contentList = contentList.value,
        )
    }
}
@Composable
fun ChapterContents(
    bookContentViewModel: BookContentViewModel,
    isFocused: Boolean,
    currentReadingItemIndex: Int,
    contentLazyColumnState: LazyListState,
    contentList: List<@Composable (Int, Boolean, Boolean) -> Unit>,
){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = contentLazyColumnState,
    ) {
        itemsIndexed(
            items = contentList,
            key = { index, _ -> index }
        ) { index, composable ->
            composable(index, index == currentReadingItemIndex, isFocused)
        }
    }
}

@SuppressLint("SdCardPath")
private fun parseListToUsableLists(
    textStyle: TextStyle,
    paragraphs: List<String>,
): Pair<List<@Composable (Int, Boolean, Boolean) -> Unit>,List<String>> {
    val composable = mutableListOf<@Composable (Int, Boolean,Boolean) -> Unit>()
    val ttsParagraph = mutableListOf<String>()
    paragraphs.forEach {
        val linkPattern = Regex("""/data/user/0/com\.capstone\.bookshelf/files/[^ ]*""")
        val headerPatten = Regex("""<h([1-6])[^>]*>(.*?)</h([1-6])>""")
        val headerLevel = Regex("""<h([1-6])>.*?</h\1>""")
        val htmlTagPattern = Regex(pattern = """<[^>]+>""")
        if(it.isNotEmpty()){
            if(linkPattern.containsMatchIn(it)) {
                composable.add{ _, _, _ ->
                    DisableSelection {
                        ImageComponent(
                            content = ImageContent(
                                content = it
                            )
                        )
                    }
                }
                ttsParagraph.add(linkPattern.replace(it, replacement = " "))
            }else if(headerPatten.containsMatchIn(it)) {
                if(htmlTagPattern.replace(it, replacement = "").isNotEmpty()){
                    composable.add {index, isHighlighted, isSpeaking ->
                        HeaderText(
                            index = index,
                            content = HeaderContent(
                                content = htmlTagPattern.replace(it, replacement = "")
                            ),
                            level = headerLevel.find(it)!!.groupValues[1].toInt(),
                            style = textStyle,
                            isHighlighted = isHighlighted,
                            isSpeaking = isSpeaking
                        )
                    }
                    ttsParagraph.add(htmlTagPattern.replace(it, replacement = ""))
                }
            } else{
                if(htmlTagPattern.replace(it, replacement = "").isNotEmpty()){
                    composable.add {index, isHighlighted,isSpeaking ->
                        ParagraphText(
                            index = index,
                            content = ParagraphContent(
                                content = it
                            ),
                            style = textStyle,
                            isHighlighted = isHighlighted,
                            isSpeaking = isSpeaking
                        )
                    }
                    ttsParagraph.add(htmlTagPattern.replace(it, replacement = ""))
                }
            }
        }
    }
    return Pair(composable,ttsParagraph)
}

private fun updateVariable(
    bookContentViewModel: BookContentViewModel,
    isPaused: Boolean,
    isSpeaking: Boolean,
    paragraphIndex: Int,
    chapterIndex: Int,
    currentPos: Int,
    triggerScroll: Boolean,
    scrollTimes: Int,
){
    bookContentViewModel.updateIsPaused(isPaused)
    bookContentViewModel.updateIsSpeaking(isSpeaking)
    bookContentViewModel.updateCurrentReadingParagraph(paragraphIndex)
    bookContentViewModel.updateCurrentChapterIndex(chapterIndex)
    bookContentViewModel.updateCurrentReadingPosition(currentPos)
    bookContentViewModel.updateFlagTriggerScrolling(triggerScroll)
    bookContentViewModel.updateScrollTime(scrollTimes)
}

@Composable
fun KeepScreenOn() = AndroidView(
    factory = {
        View(it).apply {
            keepScreenOn = true
        }
    }
)

suspend fun slowScrollToBottom(listState: LazyListState,ttsUiState: TTSState,uiState: ContentUIState) {
    val totalItems = listState.layoutInfo.totalItemsCount
    while(ttsUiState.lastVisibleItemIndex < totalItems - 1){
        listState.animateScrollBy(
            value = uiState.screenHeight.toFloat(),
            animationSpec = tween(
                durationMillis = 5000,
                delayMillis = 0,
                easing = LinearEasing
            )
        )
    }
}