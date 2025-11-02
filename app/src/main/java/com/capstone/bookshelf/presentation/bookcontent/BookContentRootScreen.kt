package com.capstone.bookshelf.presentation.bookcontent

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarAction
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarManager
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollAction
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPaletteViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.fab.CustomFab
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
import com.capstone.bookshelf.presentation.bookwriter.BookWriterAction
import com.capstone.bookshelf.presentation.bookwriter.BookWriterEdit
import com.capstone.bookshelf.presentation.bookwriter.BookWriterViewModel
import com.capstone.bookshelf.util.DataStoreManager
import com.capstone.bookshelf.util.isDark
import com.capstone.bookshelf.util.updateFocusIndex
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
    colorPaletteViewModel: ColorPaletteViewModel,
    dataStoreManager: DataStoreManager,
    onBackClick: (Boolean) -> Unit,
) {
    val drawerContainerViewModel = koinViewModel<DrawerContainerViewModel>()
    val bookWriterViewModel = koinViewModel<BookWriterViewModel>()
    val contentState by viewModel.state.collectAsStateWithLifecycle()
    val drawerContainerState by drawerContainerViewModel.state.collectAsStateWithLifecycle()
    val colorPaletteState by colorPaletteViewModel.colorPalette.collectAsStateWithLifecycle()
    val hazeState = remember { HazeState() }
    val textMeasurer = rememberTextMeasurer()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var pagerState by remember { mutableStateOf<PagerState?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerLazyColumnState = rememberLazyListState()
    val isSystemLight = !isSystemInDarkTheme()
    val currentFontSize = MaterialTheme.typography.bodyMedium.fontSize
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                if (contentState.book?.isEditable == false) {
                    viewModel.stopTTSService(context)
                    viewModel.stopTTS()
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                if (contentState.isSpeaking) {
                    viewModel.onContentAction(
                        ContentAction.UpdateBookInfoFirstParagraphIndex(
                            contentState.currentReadingParagraph
                        )
                    )
                } else {
                    viewModel.onContentAction(
                        ContentAction.UpdateBookInfoFirstParagraphIndex(
                            contentState.firstVisibleItemIndex
                        )
                    )
                }
                viewModel.onContentAction(
                    ContentAction.UpdateBookInfoCurrentChapterIndex(
                        contentState.currentChapterIndex
                    )
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.onContentAction(ContentAction.LoadBook)
        yield()
        colorPaletteViewModel.updateBackgroundColor(Color(dataStoreManager.backgroundColor.first()))
        colorPaletteViewModel.updateTextColor(Color(dataStoreManager.textColor.first()))
        colorPaletteViewModel.updateSelectedColorSet(dataStoreManager.selectedColorSet.first())
        viewModel.onContentAction(ContentAction.UpdateFontSize(dataStoreManager.fontSize.first()))
        viewModel.onContentAction(ContentAction.UpdateTextAlign(dataStoreManager.textAlign.first()))
        viewModel.onContentAction(ContentAction.UpdateTextIndent(dataStoreManager.textIndent.first()))
        viewModel.onContentAction(ContentAction.UpdateLineSpacing(dataStoreManager.lineSpacing.first()))
        viewModel.onContentAction(ContentAction.UpdateSelectedFontFamilyIndex(dataStoreManager.fontFamily.first()))
        viewModel.onContentAction(ContentAction.UpdateImagePaddingState(dataStoreManager.imagePaddingState.first()))
        viewModel.onContentAction(ContentAction.UpdateSelectedBookmarkStyle(dataStoreManager.bookmarkStyle.first()))
        viewModel.onContentAction(ContentAction.UpdateUnlockSpecialCodeStatus(dataStoreManager.unlockSpecialCodeStatus.first()))
        yield()
    }
    DrawerScreen(
        drawerContainerViewModel = drawerContainerViewModel,
        dataStoreManager = dataStoreManager,
        contentViewModel = viewModel,
        drawerContainerState = drawerContainerState,
        contentState = contentState,
        drawerState = drawerState,
        drawerLazyColumnState = drawerLazyColumnState,
        colorPaletteState = colorPaletteState,
        hazeState = hazeState,
        onTocItemClick = {
            if (contentState.book?.isEditable == true) {
                bookWriterViewModel.onAction(BookWriterAction.SaveChapter(contentState.currentChapterIndex))
                bookWriterViewModel.onAction(BookWriterAction.UpdateTriggerLoadChapter(true))
            } else {
                drawerContainerViewModel.onAction(DrawerContainerAction.UpdateCurrentTOC(it))
            }
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateDrawerState(false))
            viewModel.onContentAction(ContentAction.UpdateCurrentChapterIndex(it))
            viewModel.onContentAction(ContentAction.UpdateBookInfoCurrentChapterIndex(it))
            if (contentState.isSpeaking) {
                viewModel.onTtsUiEvent(TtsUiEvent.JumpToRandomChapter)
            }
        },
        onAddTocItem = { chapterTitle, headerSize ->
            bookWriterViewModel.onAction(BookWriterAction.SaveChapter(contentState.currentChapterIndex))
            bookWriterViewModel.onAction(
                BookWriterAction.AddChapter(
                    chapterTitle = chapterTitle,
                    headerSize = headerSize,
                    totalTocSize = drawerContainerState.tableOfContents.size,
                    currentFontSize = currentFontSize.value,
                    currentTocIndex = contentState.currentChapterIndex
                )
            )
            viewModel.onContentAction(ContentAction.UpdateCurrentChapterIndex(
                if(drawerContainerState.tableOfContents.isEmpty()) 0
                else contentState.currentChapterIndex + 1
            ))
        },
        onDeleteBookmark = {
            drawerContainerViewModel.onAction(DrawerContainerAction.DeleteBookmark(it))
        },
        onUndoDeleteBookmark = {
            drawerContainerViewModel.onAction(DrawerContainerAction.UndoDeleteBookmark)
        },
        onNoteClicked = { tocId, contentId ->
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateDrawerState(false))
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateCurrentTOC(tocId))
            viewModel.onContentAction(ContentAction.UpdateCurrentChapterIndex(tocId))
            viewModel.onContentAction(ContentAction.UpdateBookInfoCurrentChapterIndex(tocId))
            viewModel.onContentAction(ContentAction.UpdateFlagTriggerScrollForNote(contentId))
            if (contentState.isSpeaking) {
                viewModel.onTtsUiEvent(TtsUiEvent.JumpToRandomChapter)
            }
        },
        onNoteSelected = {
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateSelectedNote(it))
        },
        onEditNote = { note, newInput ->
            drawerContainerViewModel.onAction(DrawerContainerAction.EditNote(note, newInput))
        },
        onDeleteNote = {
            drawerContainerViewModel.onAction(DrawerContainerAction.DeleteNote(it))
        },
        onUndoDeleteNote = {
            drawerContainerViewModel.onAction(DrawerContainerAction.UndoDeleteNote)
        },
        onTabItemClick = {
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateSelectedNote(-1))
        },
        onEditBookTitle = {
            viewModel.onContentAction(ContentAction.UpdateBookTitle(it))
            viewModel.onContentAction(ContentAction.LoadBook)
        },
        onEditBookAuthor = {
            viewModel.onContentAction(ContentAction.UpdateBookAuthors(it))
            viewModel.onContentAction(ContentAction.LoadBook)
        },
        onEditBookCover = { uri, originalPath ->
            viewModel.onContentAction(ContentAction.UpdateCoverImage(context, uri, originalPath))
            viewModel.onContentAction(ContentAction.LoadBook)
        },
        onDeleteTocItem = {
            scope.launch {
                drawerContainerViewModel.onAction(DrawerContainerAction.DeleteTocItem(it))
                yield()
                val currentChapterIndex = contentState.currentChapterIndex
                if (it.index == currentChapterIndex) {
                    viewModel.onContentAction(ContentAction.UpdateCurrentChapterIndex((it.index - 1).coerceAtLeast(0)))
                    viewModel.onContentAction(ContentAction.UpdateBookInfoCurrentChapterIndex((it.index - 1).coerceAtLeast(0)))
                    bookWriterViewModel.onAction(BookWriterAction.UpdateTriggerLoadChapter(true))
                } else if (it.index < currentChapterIndex) {
                    viewModel.onContentAction(ContentAction.UpdateCurrentChapterIndex((currentChapterIndex - 1).coerceAtLeast(0)))
                    viewModel.onContentAction(ContentAction.UpdateBookInfoCurrentChapterIndex((currentChapterIndex - 1).coerceAtLeast(0)))
                }
            }
        },
        onMoveTocItem = {from, to->
            val updatedIndex = updateFocusIndex(
                focusedIndex = contentState.currentChapterIndex,
                startIndex = from,
                endIndex = to
            )
            viewModel.onContentAction(ContentAction.UpdateCurrentChapterIndex(updatedIndex))
        }
    ) {
        LaunchedEffect(contentState.book) {
            yield()
            if (contentState.book != null) {
                viewModel.onContentAction(ContentAction.UpdateCurrentChapterIndex(contentState.book?.currentChapter!!))
                if (contentState.book?.isEditable == false) {
                    viewModel.setupTTS(context)
                    viewModel.initialize(
                        context,
                        textMeasurer,
                        dataStoreManager.enableBackgroundMusic.first(),
                        contentState.book?.currentChapter!!
                    )
                    viewModel.onContentAction(ContentAction.UpdateKeepScreenOn(dataStoreManager.keepScreenOn.first()))
                } else {
                    bookWriterViewModel.onAction(BookWriterAction.UpdateTriggerLoadChapter(true))
                }
            }
        }
        LaunchedEffect(contentState.isSpeaking) {
            viewModel.onContentAction(ContentAction.UpdateIsFocused(!contentState.isSpeaking && contentState.isPaused || contentState.isSpeaking && !contentState.isPaused))
            viewModel.onContentAction(ContentAction.UpdateEnablePagerScroll(!contentState.isSpeaking))
        }
        LaunchedEffect(contentState.isPaused) {
            viewModel.onContentAction(ContentAction.UpdateIsFocused(!(!contentState.isSpeaking && !contentState.isPaused)))
        }
        LaunchedEffect(drawerState.currentValue) {
            if (drawerState.currentValue == DrawerValue.Closed) {
                drawerContainerViewModel.onAction(DrawerContainerAction.UpdateDrawerState(false))
                drawerContainerViewModel.onAction(DrawerContainerAction.UpdateSelectedNote(-1))
                drawerContainerViewModel.onAction(DrawerContainerAction.DisableUndoDeleteBookmark)
                drawerContainerViewModel.onAction(DrawerContainerAction.DisableUndoDeleteNote)
                drawerLazyColumnState.scrollToItem(contentState.currentChapterIndex)
            } else if (drawerState.currentValue == DrawerValue.Open) {
                drawerContainerViewModel.onAction(DrawerContainerAction.UpdateDrawerState(true))
            }
        }
        LaunchedEffect(contentState.currentChapterIndex) {
            drawerLazyColumnState.scrollToItem(contentState.currentChapterIndex)
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateCurrentTOC(contentState.currentChapterIndex))
            if (contentState.currentChapterIndex != 0)
                viewModel.onContentAction(
                    ContentAction.UpdateBookInfoCurrentChapterIndex(
                        contentState.currentChapterIndex
                    )
                )
            if (contentState.book?.isEditable == false) {
                if (contentState.flagTriggerScrollForNote != -1)
                    pagerState?.scrollToPage(contentState.currentChapterIndex)
                else
                    pagerState?.animateScrollToPage(contentState.currentChapterIndex)
            } else {
                viewModel.onContentAction(ContentAction.GetChapterContent(contentState.currentChapterIndex))
            }
        }
        LaunchedEffect(drawerContainerState.drawerState) {
            if (drawerContainerState.drawerState) {
                drawerState.open()
            } else {
                drawerState.close()
                drawerLazyColumnState.animateScrollToItem(contentState.currentChapterIndex)
                pagerState?.animateScrollToPage(contentState.currentChapterIndex)
            }
        }
        LaunchedEffect(contentState.currentLanguage) {
            if (contentState.currentLanguage != null) {
                viewModel.onContentAction(ContentAction.UpdateTTSLanguage(contentState.currentLanguage!!))
            }
        }
        LaunchedEffect(contentState.currentVoice) {
            if (contentState.currentVoice != null) {
                viewModel.onContentAction(ContentAction.UpdateTTSVoice(contentState.currentVoice!!))
            }
        }
        LaunchedEffect(contentState.currentSpeed) {
            if (contentState.currentSpeed != null) {
                viewModel.onContentAction(ContentAction.UpdateTTSSpeed(contentState.currentSpeed!!))
            }
        }
        LaunchedEffect(contentState.currentPitch) {
            if (contentState.currentPitch != null) {
                viewModel.onContentAction(ContentAction.UpdateTTSPitch(contentState.currentPitch!!))
            }
        }
        if (contentState.book?.isEditable == true) {
            BackHandler(
                onBack = {
                    onBackClick(true)
                }
            )
            BookWriterEdit(
                contentState = contentState,
                onNavigateBack = {
                    onBackClick(true)
                },
                onDrawerClick = {
                    drawerContainerViewModel.onAction(
                        DrawerContainerAction.UpdateDrawerState(
                            true
                        )
                    )
                }
            )
        } else {
            val bottomBarViewModel = koinViewModel<BottomBarViewModel>()
            val topBarViewModel = koinViewModel<TopBarViewModel>()
            val autoScrollViewModel = koinViewModel<AutoScrollViewModel>()
            val topBarState by topBarViewModel.state.collectAsStateWithLifecycle()
            val bottomBarState by bottomBarViewModel.state.collectAsStateWithLifecycle()
            val autoScrollState by autoScrollViewModel.state.collectAsStateWithLifecycle()
            if (!contentState.keepScreenOn) {
                if (autoScrollState.isStart)
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
            BackHandler(
                onBack = {
                    if (bottomBarState.visibility){
                        bottomBarViewModel.onAction(BottomBarAction.UpdateVisibility(false))
                        topBarViewModel.onAction(TopBarAction.UpdateVisibility(false))
                    }
                    if (drawerState.isOpen){
                        drawerContainerViewModel.onAction(DrawerContainerAction.UpdateDrawerState(false))
                    }
                }
            )
            DisposableEffect(contentState.book, colorPaletteState.backgroundColor, isSystemLight) {
                val window = (context as? Activity)?.window ?: return@DisposableEffect onDispose {}
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars =
                    !colorPaletteState.backgroundColor.isDark()
                insetsController.isAppearanceLightNavigationBars =
                    !colorPaletteState.backgroundColor.isDark()

                onDispose {
                    insetsController.isAppearanceLightStatusBars = isSystemLight
                    insetsController.isAppearanceLightNavigationBars = isSystemLight
                }
            }
            LaunchedEffect(contentState.isSpeaking) {
                if (!contentState.isSpeaking && bottomBarState.visibility && bottomBarState.bottomBarTTSState) {
                    bottomBarViewModel.onAction(BottomBarAction.UpdateVisibility(false))
                    topBarViewModel.onAction(TopBarAction.UpdateVisibility(false))
                }
            }
            Scaffold(
                floatingActionButton = {
                    if (contentState.enableUndoButton) {
                        CustomFab(
                            colorPaletteState = colorPaletteState,
                            onFabClick = {
                                viewModel.onContentAction(ContentAction.UpdateEnableUndoButton(false))
                            }
                        )
                    }
                },
                topBar = {
                    TopBar(
                        contentState = contentState,
                        drawerContainerState = drawerContainerState,
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
                            onBackClick(false)
                            viewModel.onContentAction(
                                ContentAction.UpdateBookInfoCurrentChapterIndex(
                                    contentState.currentChapterIndex
                                )
                            )
                            viewModel.onContentAction(
                                ContentAction.UpdateBookInfoFirstParagraphIndex(
                                    contentState.firstVisibleItemIndex
                                )
                            )
                            viewModel.stopTTSService(context)
                            viewModel.stopTTS()
                        },
                        onBookmarkIconClick = {
                            drawerContainerViewModel.onAction(
                                DrawerContainerAction.UpdateIsFavorite(
                                    !drawerContainerState
                                        .tableOfContents[contentState.currentChapterIndex]
                                        .isFavorite
                                )
                            )
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
                                viewModel.loadTTSSetting(contentState.tts!!)
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
                                    ContentAction.UpdateCurrentReadingParagraph(
                                        contentState.firstVisibleItemIndex
                                    )
                                )
                                delay(1000)
                                viewModel.onContentAction(ContentAction.UpdateIsSpeaking(true))
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
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                content = {
                    pagerState?.let {
                        LaunchedEffect(Unit) {
                            autoScrollViewModel.onAction(
                                AutoScrollAction.UpdateAutoScrollSpeed(
                                    dataStoreManager.autoScrollSpeed.first()
                                )
                            )
                            autoScrollViewModel.onAction(
                                AutoScrollAction.UpdateDelayAtStart(
                                    dataStoreManager.delayTimeAtStart.first()
                                )
                            )
                            autoScrollViewModel.onAction(
                                AutoScrollAction.UpdateDelayAtEnd(
                                    dataStoreManager.delayTimeAtEnd.first()
                                )
                            )
                            autoScrollViewModel.onAction(
                                AutoScrollAction.UpdateAutoResumeScrollMode(
                                    dataStoreManager.autoScrollResumeMode.first()
                                )
                            )
                            autoScrollViewModel.onAction(
                                AutoScrollAction.UpdateDelayResume(
                                    dataStoreManager.autoScrollResumeDelayTime.first()
                                )
                            )
                            viewModel.onContentAction(ContentAction.UpdateBookAsRecentRead)
                        }
                        ContentScreen(
                            viewModel = viewModel,
                            autoScrollViewModel = autoScrollViewModel,
                            drawerContainerViewModel = drawerContainerViewModel,
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
                                        ContentAction.UpdatePreviousChapterIndex(
                                            contentState.currentChapterIndex
                                        )
                                    )
                                } else {
                                    viewModel.onContentAction(
                                        ContentAction.UpdatePreviousChapterIndex(
                                            index
                                        )
                                    )
                                }
                                viewModel.onContentAction(
                                    ContentAction.UpdateCurrentChapterIndex(
                                        index
                                    )
                                )
                                viewModel.onContentAction(
                                    ContentAction.UpdateCurrentReadingParagraph(
                                        pos
                                    )
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