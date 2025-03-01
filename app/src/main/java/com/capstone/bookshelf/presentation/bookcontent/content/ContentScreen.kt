package com.capstone.bookshelf.presentation.bookcontent.content

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.domain.wrapper.Chapter
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollAction
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollState
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.HeaderContent
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.HeaderText
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.ImageComponent
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.ImageContent
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.ParagraphContent
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.ParagraphText
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@UnstableApi
@Composable
fun ContentScreen(
    viewModel: ContentViewModel,
    autoScrollViewModel: AutoScrollViewModel,
    hazeState: HazeState,
    pagerState : PagerState,
    drawerContainerState: DrawerContainerState,
    contentState : ContentState,
    colorPaletteState: ColorPalette,
    autoScrollState: AutoScrollState,
    dataStoreManager: DataStoreManager,
    updateSystemBar: () -> Unit,
    currentChapter : (Int,Int,Boolean) -> Unit,
){
    val lazyListStates = rememberSaveable(
        saver = mapSaver(
            save = { map -> map.mapKeys { it.key.toString() }.mapValues { it.value.firstVisibleItemIndex } },
            restore = { savedMap ->
                savedMap.mapKeys { it.key.toInt() }
                    .mapValues { LazyListState(firstVisibleItemIndex = it.value as Int) }
                    .toMutableMap()
            }
        )
    ) { mutableStateMapOf() }
    val chapterContents = remember { mutableStateMapOf<Int, List<String>>() }
    var triggerLoadChapter by remember { mutableStateOf(false) }
    var callbackLoadChapter by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isInitial by rememberSaveable { mutableStateOf(true) }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    Modifier.hazeSource(hazeState)
                else
                    Modifier
            ),
        color = colorPaletteState.backgroundColor
    ) {
        LaunchedEffect(pagerState.currentPage) {
            val currentPage = pagerState.currentPage
            lazyListStates.keys.filter { pageIndex ->
                pageIndex < currentPage - 2 || pageIndex > currentPage + 2
            }.forEach { pageIndex ->
                lazyListStates.remove(pageIndex)
            }
            chapterContents.keys.filter { pageIndex ->
                pageIndex < currentPage - 2 || pageIndex > currentPage + 2
            }.forEach { pageIndex ->
                chapterContents.remove(pageIndex)
            }
        }
        LaunchedEffect(callbackLoadChapter, contentState.currentChapterIndex) {
            if (callbackLoadChapter) {
                triggerLoadChapter = false
                callbackLoadChapter = false
            }
            if(autoScrollState.isStart && autoScrollState.isPaused){
                delay(autoScrollState.delayAtStart.toLong())
                autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(false))
            }
        }
        val beyondBoundsPageCount = 1
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize(),
            beyondViewportPageCount = beyondBoundsPageCount,
            userScrollEnabled = contentState.enablePagerScroll,
            key = { page -> page }
        ) { page ->
            val newPage by rememberUpdatedState(newValue = page)
            val chapterContent by viewModel.chapterContent
            var data by remember { mutableStateOf<Chapter?>(null) }
            val listState = lazyListStates.getOrPut(newPage) {
                if (page == contentState.book?.currentChapter && isInitial) {
                    LazyListState(firstVisibleItemIndex = contentState.book.currentParagraph)
                } else {
                    LazyListState()
                }
            }.also {
                isInitial = false
            }
            val contentList = remember { mutableStateOf(listOf<@Composable (Boolean, Boolean,ColorPalette,ContentState) -> Unit>())}
            val density = LocalDensity.current
            var hasPrintedAtEnd by remember { mutableStateOf(false) }
            var isAnimationRunning by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                snapshotFlow {
                    Pair(
                        pagerState.isScrollInProgress,
                        abs(pagerState.settledPage - newPage)
                    )
                }.collectLatest { (scrollInProgress, diff) ->
                    if (!scrollInProgress && (diff in 0..beyondBoundsPageCount)) {
                        if (diff > 0) delay(1000)
                        triggerLoadChapter = true
                        isAnimationRunning = false
                        hasPrintedAtEnd = false
                        cancel()
                    }
                }
            }
            LaunchedEffect(triggerLoadChapter) {
                if (triggerLoadChapter && data == null) {
                    viewModel.getChapter((page))
                    data = chapterContent
                    parseListToUsableLists(data!!.content).also{
                        contentList.value = it.first
                        chapterContents[page] = it.second
                    }
                    callbackLoadChapter = true
                }
            }
            LaunchedEffect(pagerState.targetPage) {
                viewModel.onContentAction(dataStoreManager,ContentAction.UpdateFlagTriggerAdjustScroll(false))
                currentChapter(pagerState.targetPage,0,autoScrollState.isStart)
            }
            LaunchedEffect(lazyListStates[contentState.currentChapterIndex]) {
                lazyListStates[contentState.currentChapterIndex]?.let {
                    snapshotFlow { it.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                        .collect { index ->
                            if (index != null) {
                                viewModel.onContentAction(dataStoreManager,ContentAction.UpdateLastVisibleItemIndex(index))
                            }
                        }
                }
            }

            LaunchedEffect(lazyListStates[contentState.currentChapterIndex]) {
                lazyListStates[contentState.currentChapterIndex]?.let {
                    snapshotFlow { it.layoutInfo.visibleItemsInfo.firstOrNull()?.index }
                        .collect { index ->
                            if (index != null) {
                                viewModel.onContentAction(dataStoreManager,ContentAction.UpdateFirstVisibleItemIndex(index))
                            }
                        }
                }

            }
            LaunchedEffect(lazyListStates[contentState.currentChapterIndex]){
                lazyListStates[contentState.currentChapterIndex]?.let {
                    snapshotFlow { it.isScrollInProgress && !pagerState.isScrollInProgress }.collect { scrolling ->
                        if (scrolling && (contentState.isSpeaking || contentState.isPaused) && contentState.currentReadingParagraph == contentState.firstVisibleItemIndex) {
                            viewModel.onContentAction(
                                dataStoreManager,
                                ContentAction.UpdateFlagTriggerAdjustScroll(
                                    true
                                )
                            )
                        }
                    }
                }
            }

            LaunchedEffect(contentState.currentReadingParagraph) {
                lazyListStates[contentState.currentChapterIndex]?.let {
                    if ((contentState.currentReadingParagraph >= contentState.lastVisibleItemIndex
                                || contentState.currentReadingParagraph <= contentState.firstVisibleItemIndex)
                        && !contentState.flagTriggerScrolling
                    ) {
                        if (contentState.isSpeaking) {
                            it.animateScrollToItem(contentState.currentReadingParagraph)
                            viewModel.onContentAction(
                                dataStoreManager,
                                ContentAction.UpdateFlagTriggerAdjustScroll(
                                    false
                                )
                            )
                        }
                    }
                }
            }

            LaunchedEffect(contentState.flagTriggerScrolling){
                if(contentState.flagTriggerScrolling)
                    viewModel.onContentAction(dataStoreManager,ContentAction.UpdateFlagStartScrolling(true))
            }

            LaunchedEffect(contentState.flagStartScrolling){
                lazyListStates[contentState.currentChapterIndex]?.let {
                    if (contentState.flagStartScrolling) {
                        if (contentState.currentReadingParagraph != contentState.firstVisibleItemIndex) {
                            it.animateScrollToItem(contentState.currentReadingParagraph)
                            viewModel.onContentAction(
                                dataStoreManager,
                                ContentAction.UpdateFlagTriggerAdjustScroll(
                                    false
                                )
                            )
                            viewModel.onContentAction(dataStoreManager,ContentAction.UpdateFlagScrollAdjusted(true))

                        } else if (!contentState.flagTriggerAdjustScroll) {
                            it.animateScrollBy(value = contentState.screenHeight.toFloat())
                            viewModel.onContentAction(dataStoreManager,
                                ContentAction.UpdateFlagTriggerAdjustScroll(
                                    false
                                )
                            )
                            viewModel.onContentAction(dataStoreManager,ContentAction.UpdateFlagStartScrolling(false))
                        } else {
                            viewModel.onContentAction(dataStoreManager,ContentAction.UpdateFlagStartScrolling(true))
                        }
                    }
                }
            }

            LaunchedEffect(contentState.flagStartAdjustScroll){
                lazyListStates[contentState.currentChapterIndex]?.let {
                    if (contentState.flagStartAdjustScroll) {
                        it.animateScrollToItem(contentState.currentReadingParagraph)
                        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateFlagTriggerAdjustScroll(false))
                        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateFlagStartAdjustScroll(false))
                        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateFlagScrollAdjusted(true))
                    }
                }
            }

            LaunchedEffect(contentState.flagScrollAdjusted){
                lazyListStates[contentState.currentChapterIndex]?.let {
                    if (contentState.flagScrollAdjusted) {
                        it.animateScrollBy(value = contentState.screenHeight.toFloat() * contentState.scrollTime)
                        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateFlagTriggerAdjustScroll(false))
                        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateFlagScrollAdjusted(false))
                        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateFlagStartScrolling(false))
                    }
                }
            }
            LaunchedEffect(autoScrollState.currentSpeed, autoScrollState.isStart, autoScrollState.isPaused) {
                lazyListStates[contentState.currentChapterIndex]?.let { lazyListState ->
                    if(autoScrollState.isStart) {
                        flow {
                            while (!autoScrollState.isPaused) {
                                emit(Unit)
                                delay(autoScrollState.currentSpeed.toLong())
                            }
                        }.filter { !autoScrollState.isPaused }
                            .collect {
                                isAnimationRunning = true
                                coroutineScope.launch {
                                    lazyListState.animateScrollBy(
                                        value = contentState.screenHeight.toFloat(),
                                        animationSpec = tween(
                                            durationMillis = autoScrollState.currentSpeed,
                                            delayMillis = 0,
                                            easing = LinearEasing
                                        )
                                    )
                                }.invokeOnCompletion {
                                    isAnimationRunning = false
                                    hasPrintedAtEnd = false
                                }
                            }
                    }
                }
            }
            LaunchedEffect(lazyListStates[contentState.currentChapterIndex]?.isScrollInProgress) {
                lazyListStates[contentState.currentChapterIndex]?.let {
                    if (!it.isScrollInProgress && autoScrollState.isStart && !autoScrollState.isPaused) {
                        autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(true))
                        if(autoScrollState.isAutoResumeScrollMode) {
                            delay(autoScrollState.delayResumeMode.toLong())
                            autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(false))
                        }
                    }
                }
            }
            LaunchedEffect(lazyListStates[contentState.currentChapterIndex]?.isScrollInProgress,autoScrollState.isPaused){
                if(autoScrollState.isStart && !autoScrollState.isPaused) {
                    lazyListStates[contentState.currentChapterIndex]?.let {
                        snapshotFlow { it.layoutInfo }
                            .collect { layoutInfo ->
                                if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                                    val lastVisibleItem = layoutInfo.visibleItemsInfo.last()
                                    if (lastVisibleItem.index == layoutInfo.totalItemsCount - 1 &&
                                        lastVisibleItem.offset + lastVisibleItem.size <= layoutInfo.viewportEndOffset + 1
                                    ) {
                                        if (!isAnimationRunning && !hasPrintedAtEnd && contentState.previousChapterIndex <= contentState.currentChapterIndex) {
                                            delay(autoScrollState.delayAtEnd.toLong())
                                            autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(true))
                                            currentChapter(minOf(contentState.currentChapterIndex + 1,contentState.book?.totalChapter!!),0,true)
                                            hasPrintedAtEnd = true
                                        }
                                    } else {
                                        hasPrintedAtEnd = false
                                    }
                                }
                            }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(color = colorPaletteState.containerColor),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    drawerContainerState.currentTOC?.title?.let {
                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .statusBarsPadding(),
                            text = it,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = TextStyle(
                                color = colorPaletteState.textColor,
                                fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                            )
                        )
                        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateChapterHeader(it))
                    }
                    Text(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(start = 12.dp)
                            .wrapContentWidth(),
                        text = "${pagerState.currentPage + 1} / ${contentState.book?.totalChapter}",
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            textAlign = TextAlign.Right,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                        ),
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .then(
                            if(!autoScrollState.isStart){
                                Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        updateSystemBar()
                                    },
                                )
                            }else{
                                Modifier.combinedClickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        updateSystemBar()
                                    },
                                    onDoubleClick = {
                                        autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(!autoScrollState.isPaused))
                                    }
                                )
                            }
                        )
                        .onGloballyPositioned { coordinates ->
                            viewModel.onContentAction(dataStoreManager,ContentAction.UpdateScreenWidth(coordinates.size.width - (with(density) { 32.dp.toPx() }.toInt())))
                            viewModel.onContentAction(dataStoreManager,ContentAction.UpdateScreenHeight(coordinates.size.height))
                        },
                    state = listState,
                ) {
                    itemsIndexed(
                        items = contentList.value,
                        key = { index, _ -> index }
                    ) { index, composable ->
                        composable(
                            index == contentState.currentReadingParagraph,
                            contentState.isFocused,
                            colorPaletteState,
                            contentState
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(color = colorPaletteState.containerColor),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(start = 4.dp),
                        text = "${contentState.lastVisibleItemIndex + 1} / ${contentList.value.size}",
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            textAlign = TextAlign.Right,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                        ),
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
@UnstableApi
@SuppressLint("SdCardPath")
fun parseListToUsableLists(
    paragraphs: List<String>,
): Pair<List<@Composable (Boolean, Boolean, ColorPalette, ContentState) -> Unit>,List<String>> {
    val composable = mutableListOf<@Composable (Boolean,Boolean,ColorPalette,ContentState) -> Unit>()
    val ttsParagraph = mutableListOf<String>()
    paragraphs.forEach {
        val linkPattern = Regex("""/data/user/0/com\.capstone\.bookshelf/files/[^ ]*""")
        val headerPatten = Regex("""<h([1-6])[^>]*>(.*?)</h([1-6])>""")
        val headerLevel = Regex("""<h([1-6])>.*?</h\1>""")
        val htmlTagPattern = Regex(pattern = """<[^>]+>""")
        if(it.isNotEmpty()){
            if(linkPattern.containsMatchIn(it)) {
                composable.add{ _,_,_,_->
                    ImageComponent(
                        content = ImageContent(
                            content = it
                        )
                    )
                }
                ttsParagraph.add(" ")
            }else if(headerPatten.containsMatchIn(it)) {
                if(htmlTagPattern.replace(it, replacement = "").isNotEmpty()){
                    composable.add {isHighlighted, isSpeaking, colorPaletteState, fontState ->
                        HeaderText(
                            colorPaletteState = colorPaletteState,
                            contentState = fontState,
                            content = HeaderContent(
                                content = htmlTagPattern.replace(it, replacement = ""),
                                contentState = fontState,
                                level = headerLevel.find(it)!!.groupValues[1].toInt(),
                            ),
                            isHighlighted = isHighlighted,
                            isSpeaking = isSpeaking
                        )
                    }
                    ttsParagraph.add(htmlTagPattern.replace(it, replacement = ""))
                }
            } else{
                if(htmlTagPattern.replace(it, replacement = "").isNotEmpty()){
                    composable.add { isHighlighted,isSpeaking, colorPaletteState, fontState->
                        ParagraphText(
                            colorPaletteState = colorPaletteState,
                            contentState = fontState,
                            content = ParagraphContent(
                                content = it,
                                contentState = fontState,
                            ),
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