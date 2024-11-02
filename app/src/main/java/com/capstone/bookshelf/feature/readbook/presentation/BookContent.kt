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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
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
import androidx.navigation.NavController
import com.capstone.bookshelf.core.domain.ChapterContentEntity
import com.capstone.bookshelf.core.presentation.component.ErrorView
import com.capstone.bookshelf.core.presentation.component.LoadingAnimation
import com.capstone.bookshelf.core.util.DisplayResult
import com.capstone.bookshelf.feature.readbook.presentation.component.bottomBar.BottomBar
import com.capstone.bookshelf.feature.readbook.presentation.component.drawer.NavigationDrawer
import com.capstone.bookshelf.feature.readbook.presentation.component.textToolBar.CustomTextToolbar
import com.capstone.bookshelf.feature.readbook.presentation.component.topBar.TopBar
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun BookContent(
    navController: NavController,
    bookId: Int,
    previousChapterIndex: Int,
){

    val bookContentViewModel = koinViewModel<BookContentViewModel>(
        parameters = { parametersOf(bookId) }
    )
    val book by bookContentViewModel.book
    val currentChapterIndex = rememberSaveable { mutableIntStateOf(-1) }
    val bottomBarState = rememberSaveable { (mutableStateOf(false)) }
    val topBarState = rememberSaveable { (mutableStateOf(false)) }
    val isSpeaking by rememberSaveable { mutableStateOf(false) }
    var isFocused by rememberSaveable { mutableStateOf(false) }
    var isPaused by rememberSaveable { mutableStateOf(false) }
    var toggleDrawerState by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val enableScaffoldBar = rememberSaveable { mutableStateOf(true) }
    val drawerLazyColumnState = rememberLazyListState()
    val lazyListStates = remember { mutableStateMapOf<Int, LazyListState>() }
    val currentReadingItemIndex = remember { mutableIntStateOf(0) }
    var pagerState by remember { mutableStateOf<PagerState?>(null) }
//    val tts = rememberTextToSpeech()
//    val textMeasurer = rememberTextMeasurer()
//    val maxWidth = with(LocalDensity.current) { (LocalConfiguration.current.screenWidthDp.dp).toPx() }.toInt()
    val maxHeight = with(LocalDensity.current) { (LocalConfiguration.current.screenHeightDp.dp-16.dp-20.dp).toPx() }.toInt()
    var scrollTimes by remember {mutableIntStateOf(0) }
//    var currentPosition by remember { mutableIntStateOf(0) }
    var flagTriggerScrolling by remember { mutableStateOf(false) }
    var currentLazyColumnState by remember { mutableStateOf<LazyListState?>(null) }

    val textStyle = TextStyle(
        textIndent = TextIndent(firstLine = 40.sp),
        textAlign = TextAlign.Justify,
        fontSize = 24.sp,
        background = Color(0x80e2e873),
        lineBreak = LineBreak.Paragraph,
    )
    LaunchedEffect(Unit) {
        currentChapterIndex.intValue = previousChapterIndex
    }
    book.DisplayResult(
        onLoading = { LoadingAnimation() },
        onError = { ErrorView(it) },
        onSuccess = {data->
            NavigationDrawer(
                bookContentViewModel = bookContentViewModel,
                book = data,
                bookId = bookId,
                currentChapterIndex = currentChapterIndex,
                drawerState = drawerState,
                drawerLazyColumnState = drawerLazyColumnState,
                toggleDrawerState = toggleDrawerState,
                onDrawerItemClick = { chapterIndex ->
                    currentChapterIndex.intValue = chapterIndex
                    scope.launch {
                        drawerState.close()
                        drawerLazyColumnState.animateScrollToItem(chapterIndex)
                        pagerState?.animateScrollToPage(chapterIndex)
                    }
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
                        toggleDrawerState = !toggleDrawerState
                        drawerLazyColumnState.scrollToItem(currentChapterIndex.intValue)
                        topBarState.value = false
                        bottomBarState.value = false
                    }
                }
                LaunchedEffect(currentChapterIndex.intValue) {
                    drawerLazyColumnState.scrollToItem(currentChapterIndex.intValue)
                    pagerState?.animateScrollToPage(currentChapterIndex.intValue)

                }

                Scaffold(
                    topBar = {
                        TopBar(
                            topBarState = topBarState,
                            onMenuIconClick ={
                                scope.launch {
                                    drawerState.open()
                                }
                            },
                            onBackIconClick = {
                                navController.navigateUp()
                                bookContentViewModel.saveBookInfo(bookId,currentChapterIndex.intValue+1)
                            }
                        )
                    },
                    bottomBar = {
                        BottomBar(
                            bottomBarState= bottomBarState,
                            isSpeaking = isSpeaking
                        )
                    }
                ){
//                    pagerState = rememberPagerState(
//                        initialPage = currentChapterIndex.intValue,
//                        pageCount = { data.totalChapter }
//                    )
                    pagerState = rememberPagerState{
                        data.totalChapter
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (enableScaffoldBar.value)
                                    Modifier.clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) {
                                        bottomBarState.value = !bottomBarState.value
                                        topBarState.value = !topBarState.value
                                    }
                                else
                                    Modifier
                            ),
                    ) {
                        val userScrollPager = rememberSaveable { mutableStateOf(true) }
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
                            userScrollEnabled = userScrollPager.value
                        ) { page ->
                            val lazyListState = lazyListStates.getOrPut(page) { LazyListState() }

                            LaunchedEffect(currentChapterIndex.intValue) {
                                currentLazyColumnState = lazyListStates[currentChapterIndex.intValue]
                            }
                            Chapter(
                                bookContentViewModel = bookContentViewModel,
                                totalChapter = data.totalChapter,
                                trigger = trigger,
                                textStyle = textStyle,
                                page = page,
                                maxHeight = maxHeight,
                                scrollTimes = scrollTimes,
                                flagTriggerScrolling = flagTriggerScrolling,
                                isFocused = isFocused,
                                isSpeaking = isSpeaking,
                                isPaused = isPaused,
                                pagerState = pagerState!!,
                                currentReadingItemIndex = currentReadingItemIndex.intValue,
                                currentLazyColumnState = currentLazyColumnState,
                                contentLazyColumnState = lazyListState,
                                userScrollPager = userScrollPager,
                                enableScaffoldBar = enableScaffoldBar,
                                currentChapter = { chapterIndex, readingIndex ->
                                    currentChapterIndex.intValue = chapterIndex
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
    totalChapter: Int,
    trigger: Boolean,
    textStyle: TextStyle,
    maxHeight: Int,
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
    userScrollPager : MutableState<Boolean>,
    enableScaffoldBar : MutableState<Boolean>,
    currentChapter: (Int,Int) -> Unit,
    callback : (Boolean) -> Unit
) {
    val contentList = remember { mutableStateOf<List<@Composable (Boolean, Boolean) -> Unit>>(emptyList()) }
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
                Log.d("test","test")
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
                Log.d("test","test1")
                flagTriggerAdjustScroll = false
                flagScrollAdjusted = true
            }else if(!flagTriggerAdjustScroll){
                currentLazyColumnState?.animateScrollBy(value = maxHeight.toFloat())
                Log.d("test","test2")
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
            Log.d("test","test3")
            flagTriggerAdjustScroll = false
            flagStartAdjustScroll = false
            flagScrollAdjusted = true
        }
    }

    LaunchedEffect(flagScrollAdjusted){
        if (flagScrollAdjusted) {
            currentLazyColumnState?.animateScrollBy(value = maxHeight.toFloat() * scrollTimes)
            Log.d("test","test4")
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
                text = (pagerState.currentPage+1).toString() + "/" + totalChapter.toString(),
                textAlign = TextAlign.End
            )
        }
        ChapterContents(
            isFocused = isFocused,
            currentReadingItemIndex = currentReadingItemIndex,
            userScrollPager = userScrollPager,
            enableScaffoldBar = enableScaffoldBar,
            contentLazyColumnState = contentLazyColumnState,
            contentList = contentList.value,
        )
    }
}
@Composable
fun ChapterContents(
    isFocused: Boolean,
    currentReadingItemIndex: Int,
    userScrollPager: MutableState<Boolean>,
    enableScaffoldBar: MutableState<Boolean>,
    contentLazyColumnState: LazyListState,
    contentList: List<@Composable (Boolean, Boolean) -> Unit>,
){
    val view = LocalView.current
    CompositionLocalProvider(LocalTextToolbar provides CustomTextToolbar(view,userScrollPager,enableScaffoldBar)) {
        SelectionContainer{
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = contentLazyColumnState,
            ) {
                itemsIndexed(contentList) { index, composable ->
                    composable(index == currentReadingItemIndex, isFocused)
                }
            }
        }
    }
}