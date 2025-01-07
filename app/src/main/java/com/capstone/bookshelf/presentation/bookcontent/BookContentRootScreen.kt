package com.capstone.bookshelf.presentation.bookcontent


import android.annotation.SuppressLint
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarAction
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarManager
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPaletteViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSAction
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.tts.rememberTextToSpeech
import com.capstone.bookshelf.presentation.bookcontent.content.ContentAction
import com.capstone.bookshelf.presentation.bookcontent.content.ContentScreen
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerAction
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerScreen
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBar
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBarAction
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBarViewModel
import com.capstone.bookshelf.util.DataStoreManger
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BookContentScreenRoot(
    viewModel: BookContentRootViewModel,
    colorPaletteViewModel : ColorPaletteViewModel,
    dataStore : DataStoreManger,
    onBackClick: () -> Unit
){
    val bottomBarViewModel = koinViewModel<BottomBarViewModel>()
    val topBarViewModel = koinViewModel<TopBarViewModel>()
    val drawerContainerViewModel = koinViewModel<DrawerContainerViewModel>()
    val contentViewModel = koinViewModel<ContentViewModel>()
    val ttsViewModel = koinViewModel<TTSViewModel>()
    val autoScrollViewModel = koinViewModel<AutoScrollViewModel>()

    val bookContentRootState by viewModel.state.collectAsStateWithLifecycle()
    val topBarState by topBarViewModel.state.collectAsStateWithLifecycle()
    val bottomBarState by bottomBarViewModel.state.collectAsStateWithLifecycle()
    val autoScrollState by autoScrollViewModel.state.collectAsStateWithLifecycle()
    val ttsState by ttsViewModel.state.collectAsStateWithLifecycle()
    val contentState by contentViewModel.state.collectAsStateWithLifecycle()
    val drawerContainerState by drawerContainerViewModel.state.collectAsStateWithLifecycle()
    val colorPaletteState by colorPaletteViewModel.colorPalette.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val textToSpeech = rememberTextToSpeech(context,ttsState)
    val textMeasurer = rememberTextMeasurer()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val pagerState = rememberPagerState(
        initialPage = contentState.currentChapterIndex,
        pageCount = { bookContentRootState.book?.totalChapter ?: 0 }
    )

    val drawerLazyColumnState = rememberLazyListState()
    val textStyle = TextStyle(
        textIndent = TextIndent(firstLine = 40.sp),
        textAlign = TextAlign.Left,
        fontSize = 20.sp,
        background = Color(0x80e2e873),
        lineBreak = LineBreak.Paragraph,
    )
    LaunchedEffect(Unit){
    }
    DrawerScreen(
        drawerContainerState = drawerContainerState,
        contentState = contentState,
        drawerState = drawerState,
        drawerLazyColumnState = drawerLazyColumnState,
        colorPaletteState = colorPaletteState,
        book = bookContentRootState.book,
        onDrawerItemClick ={
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateDrawerState(false))
            contentViewModel.onAction(ContentAction.UpdateCurrentChapterIndex(it))
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateCurrentTOC(it))
        },
    ) {
        LaunchedEffect(ttsState.isSpeaking) {
            ttsViewModel.onAction(TTSAction.UpdateIsFocused(!ttsState.isSpeaking && ttsState.isPaused || ttsState.isSpeaking && !ttsState.isPaused))
        }
        LaunchedEffect(ttsState.isPaused) {
            ttsViewModel.onAction(TTSAction.UpdateIsFocused(!(!ttsState.isSpeaking && !ttsState.isPaused)))
        }
        LaunchedEffect(drawerState.currentValue) {
            if(drawerState.currentValue == DrawerValue.Closed) {
                drawerContainerViewModel.onAction(DrawerContainerAction.UpdateDrawerState(false))
                drawerLazyColumnState.scrollToItem(contentState.currentChapterIndex)
            }
        }
        LaunchedEffect(contentState.currentChapterIndex) {
            drawerLazyColumnState.scrollToItem(contentState.currentChapterIndex)
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateCurrentTOC(contentState.currentChapterIndex))
            pagerState.animateScrollToPage(contentState.currentChapterIndex)
        }
        LaunchedEffect(drawerContainerState.drawerState){
            if(drawerContainerState.drawerState){
                drawerState.open()
            }else{
                drawerState.close()
                drawerLazyColumnState.animateScrollToItem(contentState.currentChapterIndex)
                pagerState.animateScrollToPage(contentState.currentChapterIndex)
            }
        }
        LaunchedEffect(ttsState.currentLanguage) {
            if(ttsState.currentLanguage != null){
                textToSpeech.setLanguage(ttsState.currentLanguage)
                ttsViewModel.onAction(TTSAction.UpdateTTSLanguage(ttsState.currentLanguage!!))
            }
        }
        LaunchedEffect(ttsState.currentVoice) {
            if(ttsState.currentVoice != null){
                textToSpeech.setVoice(ttsState.currentVoice)
                ttsViewModel.onAction(TTSAction.UpdateTTSVoice(ttsState.currentVoice!!))
            }
        }
        LaunchedEffect(ttsState.currentSpeed){
            if(ttsState.currentSpeed != null){
                textToSpeech.setSpeechRate(ttsState.currentSpeed!!)
                ttsViewModel.onAction(TTSAction.UpdateTTSSpeed(ttsState.currentSpeed!!))
            }
        }
        LaunchedEffect(ttsState.currentPitch){
            if(ttsState.currentPitch != null){
                textToSpeech.setPitch(ttsState.currentPitch!!)
                ttsViewModel.onAction(TTSAction.UpdateTTSPitch(ttsState.currentPitch!!))
            }
        }
        Scaffold(
            topBar = {
                TopBar(
                    topBarState = topBarState.visibility,
                    colorPaletteState = colorPaletteState,
                    onMenuIconClick ={
                        drawerContainerViewModel.onAction(DrawerContainerAction.UpdateDrawerState(true))
                        topBarViewModel.onAction(TopBarAction.UpdateVisibility(false))
                        bottomBarViewModel.onAction(BottomBarAction.UpdateVisibility(false))
                    },
                    onBackIconClick = {
                        onBackClick();
                        //                onBackIconClick(state.currentChapterIndex+1)
                        textToSpeech.stop()
                        textToSpeech.shutdown()
                    }
                )
            },
            bottomBar = {
                BottomBarManager(
                    topBarViewModel = topBarViewModel,
                    bottomBarViewModel = bottomBarViewModel,
                    autoScrollViewModel = autoScrollViewModel,
                    ttsViewModel = ttsViewModel,
                    colorPaletteViewModel = colorPaletteViewModel,
                    bottomBarState = bottomBarState,
                    ttsState = ttsState,
                    autoScrollState = autoScrollState,
                    drawerContainerState = drawerContainerState,
                    colorPaletteState = colorPaletteState,
                    dataStore = dataStore,
                    textToSpeech = textToSpeech,
                    context =  context
                )
            },
            content = {
                ContentScreen(
                    ttsViewModel =  ttsViewModel,
                    contentViewModel = contentViewModel,
                    pagerState = pagerState,
                    bookContentRootState = bookContentRootState,
                    drawerContainerState = drawerContainerState,
                    contentState = contentState,
                    ttsState = ttsState,
                    colorPaletteState = colorPaletteState,
                    textStyle = textStyle,
                    updateSystemBar = {
                        topBarViewModel.onAction(TopBarAction.UpdateVisibility(!topBarState.visibility))
                        bottomBarViewModel.onAction(BottomBarAction.UpdateVisibility(!bottomBarState.visibility))
                    },
                    currentChapter = { index,pos ->
                        contentViewModel.onAction(ContentAction.UpdateCurrentChapterIndex(index))
                        ttsViewModel.onAction(TTSAction.UpdateCurrentReadingParagraph(pos))
                    }
                )
            }
        )
    }
}
//    LaunchedEffect(Unit){
//        drawerLazyColumnState.scrollToItem(uiState.currentChapterIndex)
//        pagerState.animateScrollToPage(uiState.currentChapterIndex)
//    }
//    if (uiState.screenShallBeKeptOn) {
//        KeepScreenOn()
//    }
//    NavigationDrawer(
//        viewModel = viewModel,
//        uiState = uiState,
//        book = book,
//        drawerState = drawerState,
//        drawerLazyColumnState = drawerLazyColumnState,
//        onDrawerItemClick = { chapterIndex ->
//            viewModel.updateCurrentChapterIndex(chapterIndex)
//            viewModel.updateDrawerState(false)
//        }
//    ){
//
//        Scaffold(
//            floatingActionButton = {
//                if(uiState.isSelectedParagraph){
//                    FloatingActionButton(
//                        onClick = {
//                            viewModel.updateCommentButtonClicked(true)
//                            Log.d("test","FloatingActionButton")
//                        },
//                        content = {
//                            Icon(
//                                imageVector = Icons.Default.Create,
//                                contentDescription = "Comment"
//                            )
//                        }
//                    )
//                }
//            },
//            topBar = {
//                TopBar(
//                    topBarState = uiState.topBarState,
//                    onMenuIconClick ={
//                        viewModel.updateDrawerState(true)
//                        viewModel.updateTopBarState(false)
//                        viewModel.updateBottomBarState(false)
//                    },
//                    onBackIconClick = {
//                        onBackIconClick(uiState.currentChapterIndex+1)
//                        textToSpeech.stop()
//                        textToSpeech.shutdown()
//                    }
//                )
//            },
//            bottomBar = {
//                AnimatedVisibility(
//                    visible = uiState.bottomBarState,
//                    enter = slideInVertically(initialOffsetY = { it }),
//                    exit = slideOutVertically(targetOffsetY = { it }),
//                ) {
//                    when(uiState.bottomBarIndex) {
//                        0 -> {
//                            BottomBarDefault(
//                                uiState = uiState,
//                                onThemeIconClick = {
//                                    scope.launch {
//                                        viewModel.updateBottomBarState(false)
//                                        delay(200)
//                                        viewModel.updateBottomBarIndex(1)
//                                        viewModel.updateBottomBarState(true)
//                                    }
//                                },
//                                onTTSIconClick = {
//                                    scope.launch {
//                                        viewModel.loadTTSSetting(textToSpeech)
//                                        viewModel.updateIsPaused(false)
//                                        viewModel.updateIsSpeaking(true)
//                                        viewModel.updateBottomBarState(false)
//                                        delay(200)
//                                        viewModel.updateBottomBarIndex(3)
//                                        viewModel.updateBottomBarState(true)
//                                        viewModel.updateEnablePagerScroll(false)
//                                        readNextParagraph(
//                                            tts = textToSpeech,
//                                            uiState = uiState,
//                                            ttsState = ttsState,
//                                            chapterContents = chapterContents,
//                                            targetParagraphIndex = currentLazyColumnState?.firstVisibleItemIndex!!,
//                                            currentChapterIndex = uiState.currentChapterIndex,
//                                            currentPosition = 0,
//                                            isReading = true,
//                                            maxWidth = uiState.screenWidth,
//                                            maxHeight = uiState.screenHeight,
//                                            textStyle = textStyle,
//                                            textMeasurer = textMeasurer,
//                                            shouldScroll = ttsState.flagTriggerScrolling
//                                        ) { index,chapterIndex,currentPos,scroll,times,stopReading ->
//                                            updateVariable(
//                                                viewModel = viewModel,
//                                                isPaused = false,
//                                                isSpeaking = stopReading,
//                                                paragraphIndex = index,
//                                                chapterIndex = chapterIndex,
//                                                currentPos = currentPos,
//                                                triggerScroll = scroll,
//                                                scrollTimes = times
//                                            )
//                                        }
//                                    }
//                                },
//                                onAutoScrollIconClick = {
//                                    scope.launch {
//                                        viewModel.loadTTSSetting(textToSpeech)
//                                        viewModel.updateBottomBarState(false)
//                                        delay(200)
//                                        viewModel.updateBottomBarIndex(4)
//                                        viewModel.updateBottomBarState(true)
//                                        viewModel.updateIsAutoScroll(true)
//                                    }
//                                },
//                                onSettingIconClick = {
//                                    scope.launch {
//                                        viewModel.loadTTSSetting(textToSpeech)
//                                        viewModel.updateBottomBarState(false)
//                                        delay(200)
//                                        viewModel.updateBottomBarIndex(2)
//                                        viewModel.updateBottomBarState(true)
//                                        viewModel.changeMenuTriggerSetting(true)
//                                    }
//                                },
//                            )
//                        }
//
//                        1 -> {
//                            BottomBarTheme(
//                                uiState = uiState,
//                            )
//                        }
//                        2 -> {
//                            BottomBarSetting(
//                                viewModel = viewModel,
//                                uiState = uiState,
//                                textToSpeech = textToSpeech,
//                                ttsState = ttsState,
//                                context = context
//                            )
//                        }
//                        3 -> {
//                            BottomBarTTS(
//                                viewModel = viewModel,
//                                textToSpeech = textToSpeech,
//                                uiState = uiState,
//                                ttsState = ttsState,
//                                onPreviousChapterIconClick = {
//                                    stopReading(textToSpeech)
//                                    updateVariable(
//                                        viewModel = viewModel,
//                                        isPaused = true,
//                                        isSpeaking = false,
//                                        paragraphIndex = 0,
//                                        chapterIndex = maxOf(uiState.currentChapterIndex-1,0),
//                                        currentPos = 0,
//                                        triggerScroll = false,
//                                        scrollTimes = 0
//                                    )
//                                },
//                                onPreviousParagraphIconClick = {
//                                    if (ttsState.isSpeaking) {
//                                        viewModel.updateIsSpeaking(true)
//                                        viewModel.updateCurrentReadingPosition(0)
//                                        readNextParagraph(
//                                            tts = textToSpeech,
//                                            uiState = uiState,
//                                            ttsState = ttsState,
//                                            chapterContents = chapterContents,
//                                            targetParagraphIndex = maxOf(ttsState.currentReadingParagraph - 1,0),
//                                            currentChapterIndex = uiState.currentChapterIndex,
//                                            currentPosition = 0,
//                                            isReading = true,
//                                            maxWidth = uiState.screenWidth,
//                                            maxHeight = uiState.screenHeight,
//                                            textStyle = textStyle,
//                                            textMeasurer = textMeasurer,
//                                            shouldScroll = ttsState.flagTriggerScrolling,
//                                        ) { index, chapterIndex, currentPos,scroll,times,stopReading ->
//                                            updateVariable(
//                                                viewModel = viewModel,
//                                                isPaused = false,
//                                                isSpeaking = stopReading,
//                                                paragraphIndex = index,
//                                                chapterIndex = chapterIndex,
//                                                currentPos = currentPos,
//                                                triggerScroll = scroll,
//                                                scrollTimes = times
//                                            )
//                                        }
//                                    } else if(ttsState.isPaused){
//                                        updateVariable(
//                                            viewModel = viewModel,
//                                            isPaused = true,
//                                            isSpeaking = false,
//                                            paragraphIndex = maxOf(ttsState.currentReadingParagraph - 1,0),
//                                            chapterIndex = uiState.currentChapterIndex,
//                                            currentPos = 0,
//                                            triggerScroll = ttsState.flagTriggerScrolling,
//                                            scrollTimes = 0
//                                        )
//                                    }
//                                },
//                                onPlayPauseIconClick = {
//                                    if (ttsState.isSpeaking){
//                                        stopReading(tts = textToSpeech)
//                                        updateVariable(
//                                            viewModel = viewModel,
//                                            isPaused = true,
//                                            isSpeaking = false,
//                                            paragraphIndex = ttsState.currentReadingParagraph,
//                                            chapterIndex = uiState.currentChapterIndex,
//                                            currentPos = uiState.currentReadingPosition,
//                                            triggerScroll = ttsState.flagTriggerScrolling,
//                                            scrollTimes = 0
//                                        )
//                                    } else if(ttsState.isPaused){
//                                        viewModel.updateIsSpeaking(true)
//                                        viewModel.updateIsPaused(false)
//                                        readNextParagraph(
//                                            tts = textToSpeech,
//                                            uiState = uiState,
//                                            ttsState = ttsState,
//                                            chapterContents = chapterContents,
//                                            targetParagraphIndex = ttsState.currentReadingParagraph,
//                                            currentChapterIndex = uiState.currentChapterIndex,
//                                            currentPosition = uiState.currentReadingPosition,
//                                            isReading = true,
//                                            maxWidth = uiState.screenWidth,
//                                            maxHeight = uiState.screenHeight,
//                                            textStyle = textStyle,
//                                            textMeasurer = textMeasurer,
//                                            shouldScroll = ttsState.flagTriggerScrolling
//                                        ) { index, chapterIndex, currentPos,scroll,times,stopReading ->
//                                            updateVariable(
//                                                viewModel = viewModel,
//                                                isPaused = false,
//                                                isSpeaking = stopReading,
//                                                paragraphIndex = index,
//                                                chapterIndex = chapterIndex,
//                                                currentPos = currentPos,
//                                                triggerScroll = scroll,
//                                                scrollTimes = times
//                                            )
//                                        }
//                                    }
//                                },
//                                onNextParagraphIconClick = {
//                                    if (ttsState.isSpeaking) {
//                                        viewModel.updateIsPaused(false)
//                                        viewModel.updateIsSpeaking(true)
//                                        readNextParagraph(
//                                            tts = textToSpeech,
//                                            uiState = uiState,
//                                            ttsState = ttsState,
//                                            chapterContents = chapterContents,
//                                            targetParagraphIndex = minOf(ttsState.currentReadingParagraph + 1,currentLazyColumnState?.layoutInfo?.totalItemsCount!! - 1),
//                                            currentChapterIndex = uiState.currentChapterIndex,
//                                            currentPosition = 0,
//                                            isReading = true,
//                                            maxWidth = uiState.screenWidth,
//                                            maxHeight = uiState.screenHeight,
//                                            textStyle = textStyle,
//                                            textMeasurer = textMeasurer,
//                                            shouldScroll = ttsState.flagTriggerScrolling
//                                        ) { index, chapterIndex, currentPos,scroll,times,stopReading ->
//                                            updateVariable(
//                                                viewModel = viewModel,
//                                                isPaused = false,
//                                                isSpeaking = stopReading,
//                                                paragraphIndex = index,
//                                                chapterIndex = chapterIndex,
//                                                currentPos = currentPos,
//                                                triggerScroll = scroll,
//                                                scrollTimes = times
//                                            )
//                                        }
//                                    }else if (ttsState.isPaused){
//                                        updateVariable(
//                                            viewModel = viewModel,
//                                            isPaused = true,
//                                            isSpeaking = false,
//                                            paragraphIndex = minOf(ttsState.currentReadingParagraph + 1,currentLazyColumnState?.layoutInfo?.totalItemsCount!! - 1),
//                                            chapterIndex = uiState.currentChapterIndex,
//                                            currentPos = 0,
//                                            triggerScroll = ttsState.flagTriggerScrolling,
//                                            scrollTimes = 0
//                                        )
//                                    }
//                                },
//                                onNextChapterIconClick = {
//                                    stopReading(textToSpeech)
//                                    updateVariable(
//                                        viewModel = viewModel,
//                                        isPaused = true,
//                                        isSpeaking = false,
//                                        paragraphIndex = 0,
//                                        chapterIndex = minOf(uiState.currentChapterIndex+1,uiState.totalChapter-1),
//                                        currentPos = 0,
//                                        triggerScroll = false,
//                                        scrollTimes = 0
//                                    )
//                                },
//                                onTimerIconClick = {
//
//                                },
//                                onStopIconClick = {
//                                    stopReading(textToSpeech)
//                                    updateVariable(
//                                        viewModel = viewModel,
//                                        isPaused = false,
//                                        isSpeaking = false,
//                                        paragraphIndex = ttsState.currentReadingParagraph,
//                                        chapterIndex = uiState.currentChapterIndex,
//                                        currentPos = 0,
//                                        triggerScroll = ttsState.flagTriggerScrolling,
//                                        scrollTimes = 0
//                                    )
//                                    viewModel.updateEnablePagerScroll(true)
//                                    viewModel.updateBottomBarState(false)
//                                    viewModel.updateTopBarState(false)
//                                },
//                                onTTSSettingIconClick = {
//                                    viewModel.changeMenuTriggerVoice(!uiState.openTTSVoiceMenu)
//                                    stopReading(tts = textToSpeech)
//                                    updateVariable(
//                                        viewModel = viewModel,
//                                        isPaused = true,
//                                        isSpeaking = false,
//                                        paragraphIndex = ttsState.currentReadingParagraph,
//                                        chapterIndex = uiState.currentChapterIndex,
//                                        currentPos = uiState.currentReadingPosition,
//                                        triggerScroll = ttsState.flagTriggerScrolling,
//                                        scrollTimes = 0
//                                    )
//                                }
//                            )
//                        }
//                        4 ->{
//                            BottomBarAutoScroll(
//                                viewModel = viewModel,
//                                uiState = uiState,
//                                ttsState = ttsState,
//                                onPreviousChapterIconClick ={
//                                    viewModel.updateCurrentChapterIndex(maxOf(uiState.currentChapterIndex-1,0))
//                                },
//                                onPlayPauseIconClick = {
//                                    if(ttsState.isAutoScroll){
//                                        viewModel.updateIsAutoScroll(false)
//                                        viewModel.updateIsAutoScrollPaused(true)
//                                    }else if(ttsState.isAutoScrollPaused){
//                                        viewModel.updateIsAutoScroll(true)
//                                        viewModel.updateIsAutoScrollPaused(false)
//                                    }
//                                },
//                                onNextChapterIconClick = {
//                                    viewModel.updateCurrentChapterIndex(minOf(uiState.currentChapterIndex+1,uiState.totalChapter-1))
//                                },
//                                onStopIconClick = {
//                                    viewModel.updateIsAutoScroll(false)
//                                    viewModel.updateIsAutoScrollPaused(false)
//                                    viewModel.updateBottomBarState(false)
//                                    viewModel.updateTopBarState(false)
//                                },
//                                onSettingIconClick = {
//                                    viewModel.changeMenuTriggerAutoScroll(true)
//                                },
//                            )
//                        }
//                    }
//                }
//            }
//        ){

//                    Chapter(
//                        viewModel = viewModel,
//                        uiState = uiState,
//                        ttsUiState = ttsState,
//                        totalChapter = uiState.totalChapter,
//                        triggerLoadChapter = triggerLoadChapter,
//                        textStyle = textStyle,
//                        page = newPage,
//                        pagerState = pagerState,
//                        currentLazyColumnState = currentLazyColumnState,
//                        contentLazyColumnState = lazyListState,
//                        headers = headers,
//                        chapterContents = chapterContents,
//                        currentChapter = { chapterIndex, readingIndex ->
//                            viewModel.updateCurrentChapterIndex(chapterIndex)
//                            viewModel.updateCurrentReadingParagraph(readingIndex)
//                        },
//                        callbackLoadChapter = {
//                            callbackLoadChapter = it
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//@Composable
//fun Chapter(
//    viewModel: viewModel,
//    uiState: ContentUIState,
//    ttsUiState: TTSState,
//    totalChapter: Int,
//    triggerLoadChapter: Boolean,
//    textStyle: TextStyle,
//    page: Int,
//    pagerState: PagerState,
//    currentLazyColumnState: LazyListState?,
//    contentLazyColumnState: LazyListState,
//    headers: MutableMap<Int,String>,
//    chapterContents: MutableMap<Int,List<String>>,
//    currentChapter: (Int, Int) -> Unit,
//    callbackLoadChapter: (Boolean) -> Unit,
//) {
//        ChapterContents(
//            viewModel = viewModel,
//            isFocused = ttsUiState.isFocused,
//            currentReadingItemIndex = ttsUiState.currentReadingParagraph,
//            contentLazyColumnState = contentLazyColumnState,
//            contentList = contentList.value,
//        )
//    }
//}
//@Composable
//fun ChapterContents(
//    viewModel: viewModel,
//    isFocused: Boolean,
//    currentReadingItemIndex: Int,
//    contentLazyColumnState: LazyListState,
//    contentList: List<@Composable (Int, Boolean, Boolean) -> Unit>,
//){
//}
//
//private fun updateVariable(
//    viewModel: BookContentRootViewModel,
//    isPaused: Boolean,
//    isSpeaking: Boolean,
//    paragraphIndex: Int,
//    chapterIndex: Int,
//    currentPos: Int,
//    triggerScroll: Boolean,
//    scrollTimes: Int,
//){
//    viewModel.updateIsPaused(isPaused)
//    viewModel.updateIsSpeaking(isSpeaking)
//    viewModel.updateCurrentReadingParagraph(paragraphIndex)
//    viewModel.updateCurrentChapterIndex(chapterIndex)
//    viewModel.updateCurrentReadingPosition(currentPos)
//    viewModel.updateFlagTriggerScrolling(triggerScroll)
//    viewModel.updateScrollTime(scrollTimes)
//}
//
//@Composable
//fun KeepScreenOn() = AndroidView(
//    factory = {
//        View(it).apply {
//            keepScreenOn = true
//        }
//    }
//)
//
//suspend fun slowScrollToBottom(listState: LazyListState, ttsUiState: TTSState, uiState: ContentUIState) {
//    val totalItems = listState.layoutInfo.totalItemsCount
//    while(ttsUiState.lastVisibleItemIndex < totalItems - 1){
//        listState.animateScrollBy(
//            value = uiState.screenHeight.toFloat(),
//            animationSpec = tween(
//                durationMillis = 5000,
//                delayMillis = 0,
//                easing = LinearEasing
//            )
//        )
//    }
//}