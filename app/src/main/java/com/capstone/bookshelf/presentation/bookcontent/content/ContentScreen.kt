package com.capstone.bookshelf.presentation.bookcontent.content

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.domain.wrapper.Chapter
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollAction
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollState
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.Content
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerViewModel
import com.capstone.bookshelf.presentation.component.LoadingAnimation
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@UnstableApi
@Composable
fun ContentScreen(
    viewModel: ContentViewModel,
    autoScrollViewModel: AutoScrollViewModel,
    drawerContainerViewModel: DrawerContainerViewModel,
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
        LaunchedEffect(callbackLoadChapter, contentState.currentChapterIndex,contentState.flagTriggerScrollForNote) {
            if (callbackLoadChapter) {
                triggerLoadChapter = false
                callbackLoadChapter = false
            }
            viewModel.onContentAction(ContentAction.UpdateChapterHeader(drawerContainerState.currentTOC?.title?:""))
            if(autoScrollState.isStart && autoScrollState.isPaused){
                delay(autoScrollState.delayAtStart.toLong())
                autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(false))
            }
            if(contentState.flagTriggerScrollForNote != -1){
                lazyListStates[contentState.currentChapterIndex]?.animateScrollToItem(contentState.flagTriggerScrollForNote)
                viewModel.onContentAction(ContentAction.UpdateFlagTriggerScrollForNote(-1))
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
            var header by remember{ mutableStateOf("") }
            var hasPrintedAtEnd by remember { mutableStateOf(false) }
            var isAnimationRunning by remember { mutableStateOf(false) }
            var animationJob by remember { mutableStateOf<Job?>(null) }
            var originalOffset by remember { mutableStateOf(Offset.Zero) }
            var size by remember { mutableStateOf(IntSize.Zero) }
            var originalZoom by remember { mutableFloatStateOf(1f) }
            val density = LocalDensity.current

            if(data == null && contentState.currentChapterIndex == page){
                LoadingAnimation(
                    contentState = contentState,
                    colorPaletteState = colorPaletteState
                )
            }

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
                    data?.let { chapterData ->
                        header = chapterData.chapterTitle
                        chapterContents[page] = chapterData.content
                    }
                    callbackLoadChapter = true
                }
            }

            LaunchedEffect(pagerState.targetPage) {
                viewModel.onContentAction(ContentAction.UpdateFlagTriggerAdjustScroll(false))
                currentChapter(pagerState.targetPage,0,autoScrollState.isStart)
            }

            LaunchedEffect(contentState.isSpeaking) {
                if(contentState.isSpeaking){
                    lazyListStates[contentState.currentChapterIndex]?.animateScrollToItem(contentState.currentReadingParagraph)
                }
            }

            LaunchedEffect(lazyListStates[contentState.currentChapterIndex]) {
                lazyListStates[contentState.currentChapterIndex]?.let {
                    snapshotFlow { it.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                        .collect { index ->
                            if (index != null) {
                                viewModel.onContentAction(ContentAction.UpdateLastVisibleItemIndex(index))
                            }
                        }
                }
            }

            LaunchedEffect(lazyListStates[contentState.currentChapterIndex]) {
                lazyListStates[contentState.currentChapterIndex]?.let {
                    snapshotFlow { it.layoutInfo.visibleItemsInfo.firstOrNull()?.index }
                        .collect { index ->
                            if (index != null) {
                                viewModel.onContentAction(ContentAction.UpdateFirstVisibleItemIndex(index))
                            }
                        }
                }
            }

            LaunchedEffect(lazyListStates[contentState.currentChapterIndex]){
                lazyListStates[contentState.currentChapterIndex]?.let {
                    snapshotFlow { it.isScrollInProgress && !pagerState.isScrollInProgress }.collect { scrolling ->
                        if (scrolling && (contentState.isSpeaking || contentState.isPaused) && contentState.currentReadingParagraph == contentState.firstVisibleItemIndex) {
                            viewModel.onContentAction(ContentAction.UpdateFlagTriggerAdjustScroll(true))
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
                            viewModel.onContentAction(ContentAction.UpdateFlagTriggerAdjustScroll(false))
                        }
                    }
                }
            }

            LaunchedEffect(contentState.flagTriggerScrolling){
                if(contentState.flagTriggerScrolling)
                    viewModel.onContentAction(ContentAction.UpdateFlagStartScrolling(true))
            }

            LaunchedEffect(contentState.flagStartScrolling){
                lazyListStates[contentState.currentChapterIndex]?.let {
                    if (contentState.flagStartScrolling) {
                        if (contentState.currentReadingParagraph != contentState.firstVisibleItemIndex) {
                            it.animateScrollToItem(contentState.currentReadingParagraph)
                            viewModel.onContentAction(ContentAction.UpdateFlagTriggerAdjustScroll(false))
                            viewModel.onContentAction(ContentAction.UpdateFlagScrollAdjusted(true))
                        } else if (!contentState.flagTriggerAdjustScroll) {
                            it.animateScrollBy(value = contentState.screenHeight.toFloat())
                            viewModel.onContentAction(ContentAction.UpdateFlagTriggerAdjustScroll(false))
                            viewModel.onContentAction(ContentAction.UpdateFlagStartScrolling(false))
                        } else {
                            viewModel.onContentAction(ContentAction.UpdateFlagStartScrolling(true))
                        }
                    }
                }
            }

            LaunchedEffect(contentState.flagStartAdjustScroll){
                lazyListStates[contentState.currentChapterIndex]?.let {
                    if (contentState.flagStartAdjustScroll) {
                        it.animateScrollToItem(contentState.currentReadingParagraph)
                        viewModel.onContentAction(ContentAction.UpdateFlagTriggerAdjustScroll(false))
                        viewModel.onContentAction(ContentAction.UpdateFlagStartAdjustScroll(false))
                        viewModel.onContentAction(ContentAction.UpdateFlagScrollAdjusted(true))
                    }
                }
            }

            LaunchedEffect(contentState.flagScrollAdjusted){
                lazyListStates[contentState.currentChapterIndex]?.let {
                    if (contentState.flagScrollAdjusted) {
                        it.animateScrollBy(value = contentState.screenHeight.toFloat() * contentState.scrollTime)
                        viewModel.onContentAction(ContentAction.UpdateFlagTriggerAdjustScroll(false))
                        viewModel.onContentAction(ContentAction.UpdateFlagScrollAdjusted(false))
                        viewModel.onContentAction(ContentAction.UpdateFlagStartScrolling(false))
                    }
                }
            }

            LaunchedEffect(autoScrollState.currentSpeed, autoScrollState.isStart, autoScrollState.isPaused, autoScrollState.stopAutoScroll) {
                if (autoScrollState.stopAutoScroll || autoScrollState.isPaused) {
                    animationJob?.cancel()
                    animationJob = null
                    return@LaunchedEffect
                }
                lazyListStates[contentState.currentChapterIndex]?.let { lazyListState ->
                    if (autoScrollState.isStart) {
                        flow {
                            while (true) {
                                emit(Unit)
                                delay(autoScrollState.currentSpeed.toLong())
                            }
                        }.collect {
                            isAnimationRunning = true
                            animationJob?.cancel()
                            animationJob = coroutineScope.launch {
                                lazyListState.animateScrollBy(
                                    value = contentState.screenHeight.toFloat(),
                                    animationSpec = tween(
                                        durationMillis = autoScrollState.currentSpeed,
                                        delayMillis = 0,
                                        easing = LinearEasing
                                    )
                                )
                            }
                            animationJob?.invokeOnCompletion {
                                isAnimationRunning = false
                                hasPrintedAtEnd = false
                                animationJob = null
                            }
                        }
                    } else {
                        animationJob?.cancel()
                        animationJob = null
                    }
                } ?: run {
                    animationJob?.cancel()
                    animationJob = null
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
                        snapshotFlow { it.layoutInfo }.collect { layoutInfo ->
                            if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
                                val lastVisibleItem = layoutInfo.visibleItemsInfo.last()
                                if (lastVisibleItem.index == layoutInfo.totalItemsCount - 1 &&
                                    lastVisibleItem.offset + lastVisibleItem.size <= layoutInfo.viewportEndOffset + 1
                                ) {
                                    if (!isAnimationRunning && !hasPrintedAtEnd && contentState.previousChapterIndex <= contentState.currentChapterIndex) {
                                        delay(autoScrollState.delayAtEnd.toLong())
                                        autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(true))
                                        if(contentState.currentChapterIndex + 1 < contentState.book?.totalChapter!!){
                                            currentChapter(contentState.currentChapterIndex + 1,0,true)
                                        } else if (contentState.currentChapterIndex + 1 == contentState.book.totalChapter){
                                            autoScrollViewModel.onAction(AutoScrollAction.UpdateIsStart(false))
                                            autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(false))
                                            autoScrollViewModel.onAction(AutoScrollAction.UpdateStopAutoScroll(true))
                                        }
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

            LaunchedEffect(originalZoom) {
                if (originalZoom > 1f) {
                    viewModel.onContentAction(ContentAction.UpdateEnablePagerScroll(false))
                    viewModel.onContentAction(ContentAction.UpdateEnableUndoButton(true))
                } else {
                    viewModel.onContentAction(ContentAction.UpdateEnablePagerScroll(true))
                    viewModel.onContentAction(ContentAction.UpdateEnableUndoButton(false))
                }
            }

            LaunchedEffect(contentState.enableUndoButton) {
                if (!contentState.enableUndoButton) {
                    originalZoom = 1f
                    originalOffset = Offset.Zero
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if(!autoScrollState.isStart){
                            Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    if(!contentState.enableUndoButton) {
                                        updateSystemBar()
                                    }
                                },
                            )
                        } else {
                            Modifier.combinedClickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    if(!contentState.enableUndoButton) {
                                        updateSystemBar()
                                    }
                                },
                                onDoubleClick = {
                                    autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(!autoScrollState.isPaused))
                                }
                            )
                        }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(color = colorPaletteState.containerColor),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(start = 4.dp, end = 4.dp)
                            .weight(1f),
                        text = header,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                        )
                    )
                    Text(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(start = 4.dp, end = 4.dp)
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
                        .graphicsLayer {
                            val zoom = originalZoom
                            val offset = originalOffset
                            translationX = offset.x
                            translationY = offset.y
                            scaleX = zoom
                            scaleY = zoom
                        }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown()
                                do {
                                    val event = awaitPointerEvent()
                                    var zoom = originalZoom
                                    zoom *= event.calculateZoom()
                                    zoom = zoom.coerceIn(1f, 3f)
                                    originalZoom = zoom
                                    val pan = event.calculatePan()
                                    val currentOffset = if (zoom == 1f) {
                                        Offset.Zero
                                    } else {
                                        val temp = originalOffset + pan.times(zoom)
                                        val maxX = (size.width * (zoom - 1) / 2f)
                                        val maxY = (size.height * (zoom - 1) / 2f)
                                        Offset(
                                            temp.x.coerceIn(-maxX, maxX),
                                            temp.y.coerceIn(-maxY, maxY)
                                        )
                                    }
                                    originalOffset = currentOffset
                                } while (event.changes.any { it.pressed })
                            }
                        }
                        .onSizeChanged {
                            size = it
                        }
                        .onGloballyPositioned { coordinates ->
                            viewModel.onContentAction(ContentAction.UpdateScreenWidth(coordinates.size.width - (with(density) { 32.dp.toPx() }.toInt())))
                            viewModel.onContentAction(ContentAction.UpdateScreenHeight(coordinates.size.height))
                        },
                    state = listState,
                ) {
                    chapterContents[page]?.let {
                        itemsIndexed(
                            items = it,
                            key = { index:Int, _:String -> index }
                        ) {  index, content ->
                            Content(
                                drawerContainerViewModel = drawerContainerViewModel,
                                content = content,
                                index = index,
                                isHighlighted = index == contentState.currentReadingParagraph,
                                isSpeaking = contentState.isSpeaking,
                                colorPaletteState = colorPaletteState,
                                contentState = contentState
                            )
                        }
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
                        text = "${contentState.lastVisibleItemIndex + 1} / ${chapterContents[page]?.size}",
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