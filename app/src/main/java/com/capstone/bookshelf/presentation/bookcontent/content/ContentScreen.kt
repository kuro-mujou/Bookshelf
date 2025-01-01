package com.capstone.bookshelf.presentation.bookcontent.content

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.rememberUpdatedState
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
import com.capstone.bookshelf.domain.wrapper.Chapter
import com.capstone.bookshelf.presentation.bookcontent.BookContentRootState
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSAction
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSState
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSViewModel
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.HeaderContent
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.HeaderText
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.ImageComponent
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.ImageContent
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.ParagraphContent
import com.capstone.bookshelf.presentation.bookcontent.content.content_component.ParagraphText
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs

@Composable
fun ContentScreen(
    ttsViewModel: TTSViewModel,
    contentViewModel: ContentViewModel,
    pagerState : PagerState,
    bookContentRootState : BookContentRootState,
    drawerContainerState: DrawerContainerState,
    contentState : ContentState,
    ttsState : TTSState,
    colorPaletteState: ColorPalette,
    textStyle: TextStyle,
    updateSystemBar: () -> Unit,
    currentChapter : (Int,Int) -> Unit
){
    val lazyListStates = remember { mutableStateMapOf<Int, LazyListState>() }
    val chapterContents = remember { mutableStateMapOf<Int, List<String>>() }
    var triggerLoadChapter by remember { mutableStateOf(false) }
    var callbackLoadChapter by remember { mutableStateOf(false) }

    var currentLazyColumnState by remember { mutableStateOf<LazyListState?>(null) }
//    val currentReadingItemIndex by rememberUpdatedState(newValue = contentState.firstVisibleItemIndex)
//    val isFocused by rememberUpdatedState(newValue = ttsState.isFocused)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (bookContentRootState.enableScaffoldBar)
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        updateSystemBar()
                    }
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
            currentLazyColumnState = lazyListStates[contentState.currentChapterIndex]
            ttsViewModel.onAction(TTSAction.UpdateCurrentChapterContent(chapterContents[contentState.currentChapterIndex]))
        }
        val beyondBoundsPageCount = 1
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize(),
            beyondViewportPageCount = beyondBoundsPageCount,
            userScrollEnabled = bookContentRootState.enablePagerScroll,
            key = { page -> page }
        ) { page ->
            val newPage by rememberUpdatedState(newValue = page)
            val lazyListState = lazyListStates.getOrPut(newPage) { LazyListState() }
            val chapterContent by contentViewModel.chapterContent
            var data by remember { mutableStateOf<Chapter?>(null) }
            val contentList = remember { mutableStateOf(listOf<@Composable (Boolean, Boolean,ColorPalette) -> Unit>())}
            val density = LocalDensity.current
            LaunchedEffect(key1 = Unit) {
                snapshotFlow {
                    Pair(
                        pagerState.isScrollInProgress,
                        abs(pagerState.settledPage - newPage)
                    )
                }.collectLatest { (scrollInProgress, diff) ->
                    if (!scrollInProgress && (diff in 0..beyondBoundsPageCount)) {
                        if (diff > 0) delay(1000)
                        triggerLoadChapter = true
                        cancel()
                    }
                }
            }
            LaunchedEffect(triggerLoadChapter) {
                if (triggerLoadChapter && data == null) {
                    contentViewModel.getChapter((page))
                    data = chapterContent
                    parseListToUsableLists(textStyle, data!!.content).also{
                        contentList.value = it.first
                        chapterContents[page] = it.second
                    }
                    callbackLoadChapter = true
                }
            }
            LaunchedEffect(pagerState.targetPage) {
                contentViewModel.onAction(ContentAction.UpdateFlagTriggerAdjustScroll(false))
                currentChapter(pagerState.targetPage,0)
            }
            LaunchedEffect(currentLazyColumnState) {
                if (currentLazyColumnState != null) {
                    snapshotFlow { currentLazyColumnState!!.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                        .collect { index ->
                            if (index != null) {
                                contentViewModel.onAction(ContentAction.UpdateLastVisibleItemIndex(index))
                            }
                        }
                }
            }

            LaunchedEffect(currentLazyColumnState) {
                if (currentLazyColumnState != null) {
                    snapshotFlow { currentLazyColumnState!!.layoutInfo.visibleItemsInfo.firstOrNull()?.index }
                        .collect { index ->
                            if (index != null) {
                                contentViewModel.onAction(ContentAction.UpdateFirstVisibleItemIndex(index))
                            }
                        }
                }
            }
            LaunchedEffect(currentLazyColumnState){
                if (currentLazyColumnState != null) {
                    snapshotFlow { currentLazyColumnState!!.isScrollInProgress && !pagerState.isScrollInProgress }.collect { scrolling ->
                        if (scrolling && (ttsState.isSpeaking || ttsState.isPaused) && ttsState.currentReadingParagraph == contentState.firstVisibleItemIndex) {
                            contentViewModel.onAction(ContentAction.UpdateFlagTriggerAdjustScroll(true))
                        }
                    }
                }
            }

            LaunchedEffect(ttsState.currentReadingParagraph) {
                if ((ttsState.currentReadingParagraph >= contentState.lastVisibleItemIndex
                            || ttsState.currentReadingParagraph <= contentState.firstVisibleItemIndex)
                    && !contentState.flagTriggerScrolling
                ) {
                    if(ttsState.isSpeaking)
                    {
                        currentLazyColumnState?.animateScrollToItem(ttsState.currentReadingParagraph)
                        contentViewModel.onAction(ContentAction.UpdateFlagTriggerAdjustScroll(false))
                    }
                }
            }

            LaunchedEffect(contentState.flagTriggerScrolling){
                if(contentState.flagTriggerScrolling)
                    contentViewModel.onAction(ContentAction.UpdateFlagStartScrolling(true))
            }

            LaunchedEffect(contentState.flagStartScrolling){
                if(contentState.flagStartScrolling){
                    if(ttsState.currentReadingParagraph != contentState.firstVisibleItemIndex){
                        currentLazyColumnState?.animateScrollToItem(ttsState.currentReadingParagraph)
                        contentViewModel.onAction(ContentAction.UpdateFlagTriggerAdjustScroll(false))
                        contentViewModel.onAction(ContentAction.UpdateFlagScrollAdjusted(true))

                    }else if(!contentState.flagTriggerAdjustScroll){
                        currentLazyColumnState?.animateScrollBy(value = contentState.screenHeight.toFloat())
                        contentViewModel.onAction(ContentAction.UpdateFlagTriggerAdjustScroll(false))
                        contentViewModel.onAction(ContentAction.UpdateFlagStartScrolling(false))
                    }else{
                        contentViewModel.onAction(ContentAction.UpdateFlagStartScrolling(true))
                    }
                }
            }

            LaunchedEffect(contentState.flagStartAdjustScroll){
                if (contentState.flagStartAdjustScroll) {
                    currentLazyColumnState?.animateScrollToItem(ttsState.currentReadingParagraph)
                    contentViewModel.onAction(ContentAction.UpdateFlagTriggerAdjustScroll(false))
                    contentViewModel.onAction(ContentAction.UpdateFlagStartAdjustScroll(false))
                    contentViewModel.onAction(ContentAction.UpdateFlagScrollAdjusted(true))
                }
            }

            LaunchedEffect(contentState.flagScrollAdjusted){
                if (contentState.flagScrollAdjusted) {
                    currentLazyColumnState?.animateScrollBy(value = contentState.screenHeight.toFloat() * ttsState.scrollTime)
                    contentViewModel.onAction(ContentAction.UpdateFlagTriggerAdjustScroll(false))
                    contentViewModel.onAction(ContentAction.UpdateFlagScrollAdjusted(false))
                    contentViewModel.onAction(ContentAction.UpdateFlagStartScrolling(false))
                }
            }
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        contentViewModel.onAction(ContentAction.UpdateScreenWidth(coordinates.size.width - (with(density) { 32.dp.toPx() }.toInt())))
                        contentViewModel.onAction(ContentAction.UpdateScreenHeight(coordinates.size.height - (with(density) { 40.dp.toPx() }.toInt())))
                    },
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .height(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    drawerContainerState.currentTOC?.title?.let {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = it,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        modifier = Modifier.wrapContentWidth(),
                        text = "${pagerState.currentPage + 1} / ${drawerContainerState.tableOfContents.size}",
                        style = TextStyle(
                            textAlign = TextAlign.Right
                        ),
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    state = lazyListState,
                ) {
                    itemsIndexed(
                        items = contentList.value,
                        key = { index, _ -> index }
                    ) { index, composable ->
                        composable(index == ttsState.currentReadingParagraph, ttsState.isFocused,colorPaletteState)
                    }
                }
            }
        }
    }
}
@SuppressLint("SdCardPath")
private fun parseListToUsableLists(
    textStyle: TextStyle,
    paragraphs: List<String>,
): Pair<List<@Composable (Boolean, Boolean, ColorPalette) -> Unit>,List<String>> {
    val composable = mutableListOf<@Composable (Boolean,Boolean,ColorPalette) -> Unit>()
    val ttsParagraph = mutableListOf<String>()
    paragraphs.forEach {
        val linkPattern = Regex("""/data/user/0/com\.capstone\.bookshelf/files/[^ ]*""")
        val headerPatten = Regex("""<h([1-6])[^>]*>(.*?)</h([1-6])>""")
        val headerLevel = Regex("""<h([1-6])>.*?</h\1>""")
        val htmlTagPattern = Regex(pattern = """<[^>]+>""")
        if(it.isNotEmpty()){
            if(linkPattern.containsMatchIn(it)) {
                composable.add{ _, _,_ ->
                    ImageComponent(
                        content = ImageContent(
                            content = it
                        )
                    )
                }
                ttsParagraph.add(linkPattern.replace(it, replacement = " "))
            }else if(headerPatten.containsMatchIn(it)) {
                if(htmlTagPattern.replace(it, replacement = "").isNotEmpty()){
                    composable.add {isHighlighted, isSpeaking, colorPaletteState ->
                        HeaderText(
                            colorPaletteState = colorPaletteState,
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
                    composable.add { isHighlighted,isSpeaking, colorPaletteState->
                        ParagraphText(
                            colorPaletteState = colorPaletteState,
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