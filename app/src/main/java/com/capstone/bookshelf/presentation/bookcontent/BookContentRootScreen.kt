package com.capstone.bookshelf.presentation.bookcontent

import android.annotation.SuppressLint
import android.view.View
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.domain.wrapper.Book
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarAction
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarManager
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollAction
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPaletteViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TtsUiEvent
import com.capstone.bookshelf.presentation.bookcontent.content.ContentAction
import com.capstone.bookshelf.presentation.bookcontent.content.ContentScreen
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerAction
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerScreen
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBar
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBarAction
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBarViewModel
import com.capstone.bookshelf.presentation.bookwriter.BookWriterEdit
import com.capstone.bookshelf.presentation.bookwriter.BookWriterViewModel
import com.capstone.bookshelf.presentation.component.LoadingAnimation
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@UnstableApi
@Composable
fun BookContentScreenRoot(
    viewModel: ContentViewModel,
    colorPaletteViewModel : ColorPaletteViewModel,
    dataStoreManager : DataStoreManager,
    selectedBook : Book?,
    onBackClick: () -> Unit,
){
    var isContentLoading by remember { mutableStateOf(true) }
    val drawerContainerViewModel = koinViewModel<DrawerContainerViewModel>()
    val bookWriterViewModel = koinViewModel<BookWriterViewModel>()
    val contentState by viewModel.state.collectAsStateWithLifecycle()
    val drawerContainerState by drawerContainerViewModel.state.collectAsStateWithLifecycle()
    val colorPaletteState by colorPaletteViewModel.colorPalette.collectAsStateWithLifecycle()
    val hazeState = remember { HazeState() }
    val textMeasurer = rememberTextMeasurer()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var pagerState by remember { mutableStateOf<PagerState?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerLazyColumnState = rememberLazyListState()
    if(isContentLoading){
        LoadingAnimation()
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                viewModel.onContentAction(dataStoreManager,ContentAction.UpdateBookInfoCurrentChapterIndex(contentState.currentChapterIndex))
                viewModel.onContentAction(dataStoreManager,ContentAction.UpdateBookInfoFirstParagraphIndex(contentState.firstVisibleItemIndex))
                if(contentState.book?.isEditable == false) {
                    viewModel.stopTTSService(context)
                    viewModel.stopTTS()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(Unit) {
        if(selectedBook == null){
            viewModel.onContentAction(dataStoreManager,ContentAction.LoadBook)
        } else {
            viewModel.onContentAction(dataStoreManager,ContentAction.SelectedBook(selectedBook))
        }
        colorPaletteViewModel.updateBackgroundColor(Color(dataStoreManager.backgroundColor.first()))
        colorPaletteViewModel.updateTextColor(Color(dataStoreManager.textColor.first()))
        colorPaletteViewModel.updateSelectedColorSet(dataStoreManager.selectedColorSet.first())
        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateFontSize(dataStoreManager.fontSize.first()))
        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateTextAlign(dataStoreManager.textAlign.first()))
        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateTextIndent(dataStoreManager.textIndent.first()))
        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateLineSpacing(dataStoreManager.lineSpacing.first()))
        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateSelectedFontFamilyIndex(dataStoreManager.fontFamily.first()))
        yield()
        isContentLoading = false
    }
    DrawerScreen(
        drawerContainerState = drawerContainerState,
        contentState = contentState,
        drawerState = drawerState,
        drawerLazyColumnState = drawerLazyColumnState,
        colorPaletteState = colorPaletteState,
        hazeState = hazeState,
        onDrawerItemClick = {
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateDrawerState(false))
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateCurrentTOC(it))
            viewModel.onContentAction(dataStoreManager,ContentAction.UpdateCurrentChapterIndex(it))
            viewModel.onContentAction(dataStoreManager,ContentAction.UpdateBookInfoCurrentChapterIndex(it))
            if(contentState.isSpeaking){
                viewModel.onTtsUiEvent(TtsUiEvent.JumpToRandomChapter)
            }
        },
        onAddingChapter = { chapterTitle, headerSize->
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateDrawerState(false))
            drawerContainerViewModel.onAction(DrawerContainerAction.AddChapter(chapterTitle,headerSize))
            viewModel.onContentAction(dataStoreManager,ContentAction.UpdateCurrentChapterIndex(drawerContainerState.tableOfContents.size))
            viewModel.onContentAction(dataStoreManager,ContentAction.GetChapterContent(contentState.currentChapterIndex))
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateCurrentTOC(contentState.currentChapterIndex))
        }
    ) {
        LaunchedEffect(contentState.book) {
            if (contentState.book != null) {
                if(contentState.book?.isEditable == false){
                    viewModel.setupTTS(context)
                    viewModel.initialize(context,textMeasurer,dataStoreManager,dataStoreManager.enableBackgroundMusic.first())
                    viewModel.onContentAction(dataStoreManager,ContentAction.UpdateKeepScreenOn(dataStoreManager.keepScreenOn.first()))
                }
                viewModel.onContentAction(dataStoreManager,ContentAction.UpdateCurrentChapterIndex(contentState.book?.currentChapter!!))
            }
        }
        LaunchedEffect(contentState.isSpeaking) {
            viewModel.onContentAction(dataStoreManager,ContentAction.UpdateIsFocused(!contentState.isSpeaking && contentState.isPaused || contentState.isSpeaking && !contentState.isPaused))
            viewModel.onContentAction(dataStoreManager,ContentAction.UpdateEnablePagerScroll(!contentState.isSpeaking))
        }
        LaunchedEffect(contentState.isPaused) {
            viewModel.onContentAction(dataStoreManager,ContentAction.UpdateIsFocused(!(!contentState.isSpeaking && !contentState.isPaused)))
        }
        LaunchedEffect(drawerState.currentValue) {
            if(drawerState.currentValue == DrawerValue.Closed) {
                drawerContainerViewModel.onAction(DrawerContainerAction.UpdateDrawerState(false))
                drawerLazyColumnState.scrollToItem(contentState.currentChapterIndex)
            } else if(drawerState.currentValue == DrawerValue.Open) {
                drawerContainerViewModel.onAction(DrawerContainerAction.UpdateDrawerState(true))
            }
        }
        LaunchedEffect(contentState.currentChapterIndex) {
            drawerLazyColumnState.scrollToItem(contentState.currentChapterIndex)
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateCurrentTOC(contentState.currentChapterIndex))
            if(contentState.book?.isEditable == false) {
                pagerState?.animateScrollToPage(contentState.currentChapterIndex)
            } else {
                viewModel.onContentAction(dataStoreManager,ContentAction.GetChapterContent(contentState.currentChapterIndex))
            }
        }
        LaunchedEffect(drawerContainerState.drawerState){
            if(drawerContainerState.drawerState){
                drawerState.open()
            }else{
                drawerState.close()
                drawerLazyColumnState.animateScrollToItem(contentState.currentChapterIndex)
                pagerState?.animateScrollToPage(contentState.currentChapterIndex)
            }
        }
        LaunchedEffect(contentState.currentLanguage) {
            if(contentState.currentLanguage != null){
                viewModel.onContentAction(dataStoreManager,ContentAction.UpdateTTSLanguage(contentState.currentLanguage!!))
            }
        }
        LaunchedEffect(contentState.currentVoice) {
            if(contentState.currentVoice != null){
                viewModel.onContentAction(dataStoreManager,ContentAction.UpdateTTSVoice(contentState.currentVoice!!))
            }
        }
        LaunchedEffect(contentState.currentSpeed){
            if(contentState.currentSpeed != null){
                viewModel.onContentAction(dataStoreManager,ContentAction.UpdateTTSSpeed(contentState.currentSpeed!!))
            }
        }
        LaunchedEffect(contentState.currentPitch){
            if(contentState.currentPitch != null){
                viewModel.onContentAction(dataStoreManager,ContentAction.UpdateTTSPitch(contentState.currentPitch!!))
            }
        }
        if(contentState.book?.isEditable == true){
            drawerContainerState.currentTOC?.let {
                BookWriterEdit(
                    bookWriterViewModel = bookWriterViewModel,
                    contentViewModel = viewModel,
                    drawerContainerState = drawerContainerState,
                    contentState = contentState
                )
            }
        } else {
            val bottomBarViewModel = koinViewModel<BottomBarViewModel>()
            val topBarViewModel = koinViewModel<TopBarViewModel>()
            val autoScrollViewModel = koinViewModel<AutoScrollViewModel>()
            val topBarState by topBarViewModel.state.collectAsStateWithLifecycle()
            val bottomBarState by bottomBarViewModel.state.collectAsStateWithLifecycle()
            val autoScrollState by autoScrollViewModel.state.collectAsStateWithLifecycle()
            if(!contentState.keepScreenOn){
                if(autoScrollState.isStart)
                    KeepScreenOn(true)
                else
                    KeepScreenOn(false)
            } else {
                KeepScreenOn(true)
            }
            contentState.book?.let {
                pagerState = rememberPagerState(
                    initialPage = it.currentChapter,
                    pageCount = { it.totalChapter }
                )
            }
            Scaffold(
                topBar = {
                    TopBar(
                        hazeState = hazeState,
                        topBarState = topBarState.visibility,
                        colorPaletteState = colorPaletteState,
                        onMenuIconClick = {
                            drawerContainerViewModel.onAction(
                                DrawerContainerAction.UpdateDrawerState(
                                    true
                                )
                            )
                            topBarViewModel.onAction(TopBarAction.UpdateVisibility(false))
                            bottomBarViewModel.onAction(BottomBarAction.UpdateVisibility(false))
                        },
                        onBackIconClick = {
                            onBackClick()
                            viewModel.onContentAction(
                                dataStoreManager,
                                ContentAction.UpdateBookInfoCurrentChapterIndex(contentState.currentChapterIndex)
                            )
                            viewModel.onContentAction(
                                dataStoreManager,
                                ContentAction.UpdateBookInfoFirstParagraphIndex(contentState.firstVisibleItemIndex)
                            )
                            viewModel.stopTTSService(context)
                            viewModel.stopTTS()
                        }
                    )
                },
                bottomBar = {
                    BottomBarManager(
                        viewModel = viewModel,
                        topBarViewModel = topBarViewModel,
                        bottomBarViewModel = bottomBarViewModel,
                        autoScrollViewModel = autoScrollViewModel,
                        colorPaletteViewModel = colorPaletteViewModel,
                        hazeState = hazeState,
                        bottomBarState = bottomBarState,
                        contentState = contentState,
                        autoScrollState = autoScrollState,
                        drawerContainerState = drawerContainerState,
                        colorPaletteState = colorPaletteState,
                        dataStoreManager = dataStoreManager,
                        connectToService = {
                            scope.launch {
                                viewModel.loadTTSSetting(dataStoreManager, contentState.tts!!)
                                bottomBarViewModel.onAction(
                                    BottomBarAction.UpdateBottomBarDefaultState(
                                        false
                                    )
                                )
                                bottomBarViewModel.onAction(
                                    BottomBarAction.UpdateBottomBarTTSState(
                                        true
                                    )
                                )
                                viewModel.onContentAction(
                                    dataStoreManager,
                                    ContentAction.UpdateCurrentReadingParagraph(contentState.firstVisibleItemIndex)
                                )
                                delay(1000)
                                viewModel.onContentAction(
                                    dataStoreManager,
                                    ContentAction.UpdateIsSpeaking(true)
                                )
                                viewModel.play()
                            }
                        },
                        onSwitchChange = {
                            scope.launch {
                                dataStoreManager.setKeepScreenOn(it)
                            }
                        }
                    )
                },
                content = {
                    pagerState?.let {
                        LaunchedEffect(Unit) {
                            autoScrollViewModel.onAction(AutoScrollAction.UpdateAutoScrollSpeed(dataStoreManager.autoScrollSpeed.first()))
                            autoScrollViewModel.onAction(AutoScrollAction.UpdateDelayAtStart(dataStoreManager.delayTimeAtStart.first()))
                            autoScrollViewModel.onAction(AutoScrollAction.UpdateDelayAtEnd(dataStoreManager.delayTimeAtEnd.first()))
                            autoScrollViewModel.onAction(AutoScrollAction.UpdateAutoResumeScrollMode(dataStoreManager.autoScrollResumeMode.first()))
                            autoScrollViewModel.onAction(AutoScrollAction.UpdateDelayResume(dataStoreManager.autoScrollResumeDelayTime.first()))
                        }
                        ContentScreen(
                            viewModel = viewModel,
                            autoScrollViewModel = autoScrollViewModel,
                            hazeState = hazeState,
                            pagerState = it,
                            drawerContainerState = drawerContainerState,
                            contentState = contentState,
                            colorPaletteState = colorPaletteState,
                            autoScrollState = autoScrollState,
                            dataStoreManager = dataStoreManager,
                            updateSystemBar = {
                                topBarViewModel.onAction(TopBarAction.UpdateVisibility(!topBarState.visibility))
                                bottomBarViewModel.onAction(BottomBarAction.UpdateVisibility(!bottomBarState.visibility))
                            },
                            currentChapter = { index, pos, isInAutoScrollMode ->
                                if (isInAutoScrollMode) {
                                    viewModel.onContentAction(
                                        dataStoreManager,
                                        ContentAction.UpdatePreviousChapterIndex(
                                            contentState.currentChapterIndex
                                        )
                                    )
                                } else {
                                    viewModel.onContentAction(
                                        dataStoreManager,
                                        ContentAction.UpdatePreviousChapterIndex(
                                            index
                                        )
                                    )
                                }
                                viewModel.onContentAction(
                                    dataStoreManager,
                                    ContentAction.UpdateCurrentChapterIndex(index)
                                )
                                viewModel.onContentAction(
                                    dataStoreManager,
                                    ContentAction.UpdateBookInfoCurrentChapterIndex(index)
                                )
                                viewModel.onContentAction(
                                    dataStoreManager,
                                    ContentAction.UpdateCurrentReadingParagraph(pos)
                                )
                            }
                        )
                    }
                }
            )
        }
    }
}
@Composable
fun KeepScreenOn(isKeepScreenOn: Boolean) = AndroidView(
    factory = {
        View(it).apply {
            keepScreenOn = isKeepScreenOn
        }
    }
)