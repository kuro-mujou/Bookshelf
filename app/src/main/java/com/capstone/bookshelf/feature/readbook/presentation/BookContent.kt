package com.capstone.bookshelf.feature.readbook.presentation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.bookshelf.core.domain.ChapterContentEntity
import com.capstone.bookshelf.core.presentation.component.ErrorView
import com.capstone.bookshelf.core.presentation.component.LoadingAnimation
import com.capstone.bookshelf.core.util.DisplayResult
import com.capstone.bookshelf.feature.readbook.presentation.component.bottomBar.BottomBar
import com.capstone.bookshelf.feature.readbook.presentation.component.drawer.NavigationDrawer
import com.capstone.bookshelf.feature.readbook.presentation.component.textToolBar.CustomTextToolbar
import com.capstone.bookshelf.feature.readbook.presentation.component.topBar.TopBar
import com.capstone.bookshelf.feature.readbook.presentation.state.ContentUIState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BookContent(
    bookId: Int,
    onBackIconClick: (Int) -> Unit
){
    val bookContentViewModel = koinViewModel<BookContentViewModel>(
        parameters = { parametersOf(bookId) }
    )
    val book by bookContentViewModel.book
    val uiState by bookContentViewModel.contentUIState.collectAsState()

    val isSpeaking by rememberSaveable { mutableStateOf(false) }
    var isFocused by rememberSaveable { mutableStateOf(false) }
    var isPaused by rememberSaveable { mutableStateOf(false) }
    var scrollTimes by remember {mutableIntStateOf(0) }
    val currentReadingItemIndex = remember { mutableIntStateOf(0) }

    var flagTriggerScrolling by remember { mutableStateOf(false) }

//    val tts = rememberTextToSpeech()
//    val textMeasurer = rememberTextMeasurer()
//    var currentPosition by remember { mutableIntStateOf(0) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var pagerState by remember { mutableStateOf<PagerState?>(null) }
    val lazyListStates = remember { mutableStateMapOf<Int, LazyListState>() }
    var currentLazyColumnState by remember { mutableStateOf<LazyListState?>(null) }
    val drawerLazyColumnState = rememberLazyListState()

    val textStyle = TextStyle(
        textIndent = TextIndent(firstLine = 40.sp),
        textAlign = TextAlign.Justify,
        fontSize = 24.sp,
        background = Color(0x80e2e873),
        lineBreak = LineBreak.Paragraph,
    )
    book.DisplayResult(
        onLoading = { LoadingAnimation() },
        onError = { ErrorView(it) },
        onSuccess = {data->
            NavigationDrawer(
                bookContentViewModel = bookContentViewModel,
                uiState = uiState,
                book = data,
                drawerState = drawerState,
                drawerLazyColumnState = drawerLazyColumnState,
                onDrawerItemClick = { chapterIndex ->
                    bookContentViewModel.updateCurrentChapterIndex(chapterIndex)
                    bookContentViewModel.updateDrawerState(false)
                }
            ){
                LaunchedEffect(isSpeaking) {
                    isFocused = !isSpeaking && isPaused || isSpeaking && !isPaused
                }
                LaunchedEffect(isPaused) {
                    isFocused = !(!isSpeaking && !isPaused)
                }
                LaunchedEffect(drawerState.currentValue) {
                    if(drawerState.currentValue == DrawerValue.Closed) {
                        bookContentViewModel.updateDrawerState(false)
                        bookContentViewModel.updateTopBarState(false)
                        bookContentViewModel.updateBottomBarState(false)
                        drawerLazyColumnState.scrollToItem(uiState.currentChapterIndex)
                    }
                }
                LaunchedEffect(uiState.currentChapterIndex) {
                    drawerLazyColumnState.scrollToItem(uiState.currentChapterIndex)
                    pagerState?.animateScrollToPage(uiState.currentChapterIndex)
                }
                LaunchedEffect(uiState.drawerState){
                    if(uiState.drawerState){
                        drawerState.open()
                    }else{
                        drawerState.close()
                        drawerLazyColumnState.animateScrollToItem(uiState.currentChapterIndex)
                        pagerState?.animateScrollToPage(uiState.currentChapterIndex)
                    }
                }
                Scaffold(
                    topBar = {
                        TopBar(
                            topBarState = uiState.topBarState,
                            onMenuIconClick ={
                                bookContentViewModel.updateDrawerState(true)
                            },
                            onBackIconClick = {
                                onBackIconClick(uiState.currentChapterIndex+1)
                            }
                        )
                    },
                    bottomBar = {
                        BottomBar(
                            bottomBarState = uiState.bottomBarState,
                            isSpeaking = isSpeaking
                        )
                    }
                ){
                    pagerState = rememberPagerState{
                        data.totalChapter
                    }
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
                        var trigger by remember { mutableStateOf(false) }
                        var callback by remember { mutableStateOf(false) }
                        LaunchedEffect(pagerState!!.currentPage) {
                            val currentPage = pagerState!!.currentPage
                            lazyListStates.keys.filter { pageIndex ->
                                pageIndex < currentPage - 2 || pageIndex > currentPage + 2
                            }.forEach { pageIndex ->
                                lazyListStates.remove(pageIndex)
                            }
                        }
                        LaunchedEffect(pagerState) {
                            snapshotFlow { pagerState!!.settledPage }
                                .distinctUntilChanged() // Only emit unique page changes
                                .collect { _ ->
                                    trigger = true
                                }
                        }
                        LaunchedEffect(callback) {
                            if (callback) {
                                trigger = false
                                callback = false
                            }
                        }
                        HorizontalPager(
                            state = pagerState!!,
                            modifier = Modifier
                                .fillMaxSize(),
                            beyondViewportPageCount = 1,
                        ) { page ->
                            val lazyListState = lazyListStates.getOrPut(page) { LazyListState() }

                            LaunchedEffect(uiState.currentChapterIndex) {
                                currentLazyColumnState = lazyListStates[uiState.currentChapterIndex]
                            }
                            Chapter(
                                bookContentViewModel = bookContentViewModel,
                                uiState = uiState,
                                totalChapter = data.totalChapter,
                                trigger = trigger,
                                textStyle = textStyle,
                                page = page,
                                scrollTimes = scrollTimes,
                                flagTriggerScrolling = flagTriggerScrolling,
                                isFocused = isFocused,
                                isSpeaking = isSpeaking,
                                isPaused = isPaused,
                                pagerState = pagerState!!,
                                currentReadingItemIndex = currentReadingItemIndex.intValue,
                                currentLazyColumnState = currentLazyColumnState,
                                contentLazyColumnState = lazyListState,
                                currentChapter = { chapterIndex, readingIndex ->
                                    bookContentViewModel.updateCurrentChapterIndex(chapterIndex)
                                    currentReadingItemIndex.intValue = readingIndex
                                },
                                callback = {
                                    callback = it
                                }
                            )
                        }
                    }
                }
            }

        }
    )
}
@Composable
fun Chapter(
    bookContentViewModel: BookContentViewModel,
    uiState: ContentUIState,
    totalChapter: Int,
    trigger: Boolean,
    textStyle: TextStyle,
    page: Int,
    scrollTimes: Int,
    flagTriggerScrolling: Boolean,
    isFocused: Boolean,
    isSpeaking: Boolean,
    isPaused: Boolean,
    pagerState: PagerState,
    currentReadingItemIndex: Int,
    currentLazyColumnState: LazyListState?,
    contentLazyColumnState: LazyListState,
    currentChapter: (Int, Int) -> Unit,
    callback: (Boolean) -> Unit
) {
    val contentList = remember {
        mutableStateOf(listOf<@Composable (Boolean, Boolean) -> Unit>())
    }
    var firstVisibleItemIndex by remember { mutableIntStateOf(0) }
    var lastVisibleItemIndex by remember {mutableIntStateOf(0) }
    var flagStartScrolling by remember { mutableStateOf(false) }
    var flagScrollAdjusted by remember { mutableStateOf(false) }
    var flagTriggerAdjustScroll by remember { mutableStateOf(false) }
    var flagStartAdjustScroll by remember { mutableStateOf(false) }
    var header by remember { mutableStateOf("") }
    val chapterContent by bookContentViewModel.chapterContent
    var data by remember { mutableStateOf<ChapterContentEntity?>(null) }

    LaunchedEffect(trigger) {
        if (trigger && data == null) {
            bookContentViewModel.getChapterContent(page)
            data = chapterContent
            contentList.value =
                bookContentViewModel.parseListToComposableList(textStyle, data!!.content)
            header = data!!.chapterTitle
            callback(true)
        }
    }
    LaunchedEffect(pagerState.targetPage) {
        flagTriggerAdjustScroll = false
        currentChapter(pagerState.targetPage,0)
    }
    LaunchedEffect(currentLazyColumnState) {
        if (currentLazyColumnState != null) {
            snapshotFlow { currentLazyColumnState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { index ->
                    if (index != null) {
                        lastVisibleItemIndex = index
                    }
                }
        }
    }

    LaunchedEffect(currentLazyColumnState) {
        if (currentLazyColumnState != null) {
            snapshotFlow { currentLazyColumnState.layoutInfo.visibleItemsInfo.firstOrNull()?.index }
                .collect { index ->
                    if (index != null) {
                        firstVisibleItemIndex = index
                    }
                }
        }
    }
    LaunchedEffect(currentLazyColumnState){
        if (currentLazyColumnState != null) {
            snapshotFlow { currentLazyColumnState.isScrollInProgress && !pagerState.isScrollInProgress }.collect { scrolling ->
                if (scrolling && (isSpeaking || isPaused) && currentReadingItemIndex == firstVisibleItemIndex) {
                    flagTriggerAdjustScroll = true
                }
            }
        }
    }

    LaunchedEffect(currentReadingItemIndex) {
        if ((currentReadingItemIndex >= lastVisibleItemIndex || currentReadingItemIndex <= firstVisibleItemIndex) && !flagTriggerScrolling) {
            if(isSpeaking)
            {
                currentLazyColumnState?.animateScrollToItem(currentReadingItemIndex)
                flagTriggerAdjustScroll = false
            }
        }
    }

    LaunchedEffect(flagTriggerScrolling){
        if(flagTriggerScrolling)
            flagStartScrolling = true
    }

    LaunchedEffect(flagStartScrolling){
        if(flagStartScrolling){
            if(currentReadingItemIndex != firstVisibleItemIndex){
                currentLazyColumnState?.animateScrollToItem(currentReadingItemIndex)
                flagTriggerAdjustScroll = false
                flagScrollAdjusted = true
            }else if(!flagTriggerAdjustScroll){
                currentLazyColumnState?.animateScrollBy(value = uiState.screenHeight.toFloat())
                flagTriggerAdjustScroll = false
                flagStartScrolling = false
            }else{
                flagStartAdjustScroll = true
            }
        }
    }

    LaunchedEffect(flagStartAdjustScroll){
        if (flagStartAdjustScroll) {
            currentLazyColumnState?.animateScrollToItem(currentReadingItemIndex)
            flagTriggerAdjustScroll = false
            flagStartAdjustScroll = false
            flagScrollAdjusted = true
        }
    }

    LaunchedEffect(flagScrollAdjusted){
        if (flagScrollAdjusted) {
            currentLazyColumnState?.animateScrollBy(value = uiState.screenHeight.toFloat() * scrollTimes)
            flagTriggerAdjustScroll = false
            flagScrollAdjusted = false
            flagStartScrolling = false
        }
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(Color.Blue),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(
                modifier = Modifier.weight(1f),
                text = header,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier.weight(0.3f),
                text = "${pagerState.currentPage+1} / $totalChapter",
                textAlign = TextAlign.End
            )
        }
        ChapterContents(
            bookContentViewModel = bookContentViewModel,
            uiState = uiState,
            isFocused = isFocused,
            currentReadingItemIndex = currentReadingItemIndex,
            contentLazyColumnState = contentLazyColumnState,
            contentList = contentList.value,
        )
    }
}
@Composable
fun ChapterContents(
    bookContentViewModel: BookContentViewModel,
    uiState: ContentUIState,
    isFocused: Boolean,
    currentReadingItemIndex: Int,
    contentLazyColumnState: LazyListState,
    contentList: List<@Composable (Boolean, Boolean) -> Unit>,
){
    val view = LocalView.current
    CompositionLocalProvider(
        value = LocalTextToolbar provides
            CustomTextToolbar(
                view = view,
                scrollingPager = uiState.enablePagerScroll,
                enableScaffoldBar = uiState.enableScaffoldBar,
                output = { test->
                    Log.d("test",test)
                },
                updateState = {pager,scaffold->
                    bookContentViewModel.updateEnableScaffoldBar(pager)
                    bookContentViewModel.updateEnablePagerScroll(scaffold)
                }
            )
    ) {
        SelectionContainer{
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        bookContentViewModel.updateScreenHeight(coordinates.size.height)
                        bookContentViewModel.updateScreenWidth(coordinates.size.width)
                    },
                state = contentLazyColumnState,
            ) {
                itemsIndexed(contentList) { index, composable ->
                    composable(index == currentReadingItemIndex, isFocused)
                }
            }
        }
    }
}

//val state = rememberLazyListState()  LazyRow(     modifier = Modifier. fillMaxSize(),     verticalAlignment = Alignment. CenterVertically,     state = state,     flingBehavior = rememberSnapFlingBehavior(lazyListState = state) ) {     items(200) {         Box(             modifier = Modifier                 .height(400.dp)                 .width(200.dp)                 .padding(8.dp)                 .background(Color. Gray),             contentAlignment = Alignment. Center         ) {             Text(it. toString(), fontSize = 32.sp)         }     } }
//androidx. compose. foundation. samples. SnapFlingBehaviorCustomizedSample
//val state = rememberLazyListState()  // If you'd like to customize either the snap behavior or the layout provider val snappingLayout = remember(state) { SnapLayoutInfoProvider(state) } val flingBehavior = rememberSnapFlingBehavior(snappingLayout)  LazyRow(     modifier = Modifier. fillMaxSize(),     verticalAlignment = Alignment. CenterVertically,     state = state,     flingBehavior = flingBehavior ) {     items(200) {         Box(             modifier = Modifier                 .height(400.dp)                 .width(200.dp)                 .padding(8.dp)                 .background(Color. Gray),             contentAlignment = Alignment. Center         ) {             Text(it. toString(), fontSize = 32.sp)         }     } }