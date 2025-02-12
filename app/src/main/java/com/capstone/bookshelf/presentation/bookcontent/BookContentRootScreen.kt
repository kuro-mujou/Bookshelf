package com.capstone.bookshelf.presentation.bookcontent


//import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSMediaService
//import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSMediaViewModel
//import com.capstone.bookshelf.presentation.bookcontent.component.tts.UIEventTTS
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarAction
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarManager
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollAction
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPaletteViewModel
import com.capstone.bookshelf.presentation.bookcontent.content.ContentAction
import com.capstone.bookshelf.presentation.bookcontent.content.ContentScreen
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerAction
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerScreen
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBar
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBarAction
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBarViewModel
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@UnstableApi
@Composable
fun BookContentScreenRoot(
    viewModel: ContentViewModel,
    colorPaletteViewModel : ColorPaletteViewModel,
    dataStoreManager : DataStoreManager,
    onBackClick: () -> Unit,
){
    val bottomBarViewModel = koinViewModel<BottomBarViewModel>()
    val topBarViewModel = koinViewModel<TopBarViewModel>()
    val drawerContainerViewModel = koinViewModel<DrawerContainerViewModel>()
    val autoScrollViewModel = koinViewModel<AutoScrollViewModel>()
//    val ttsViewModel = koinViewModel<TTSMediaViewModel>()

    val topBarState by topBarViewModel.state.collectAsStateWithLifecycle()
    val bottomBarState by bottomBarViewModel.state.collectAsStateWithLifecycle()
    val autoScrollState by autoScrollViewModel.state.collectAsStateWithLifecycle()
    val contentState by viewModel.state.collectAsStateWithLifecycle()
    val drawerContainerState by drawerContainerViewModel.state.collectAsStateWithLifecycle()
    val colorPaletteState by colorPaletteViewModel.colorPalette.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hazeState = remember { HazeState() }
    val textMeasurer = rememberTextMeasurer()
    val scope = rememberCoroutineScope()

    var pagerState by remember { mutableStateOf<PagerState?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    contentState.book?.let {
        pagerState = rememberPagerState(
            initialPage = it.currentChapter,
            pageCount = { it.totalChapter }
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onContentAction(dataStoreManager,ContentAction.UpdateBookInfoFirstParagraphIndex(contentState.firstVisibleItemIndex))
            viewModel.stopTTSService(context)
            viewModel.stopTTS()
        }
    }
    val drawerLazyColumnState = rememberLazyListState()
    if(!contentState.keepScreenOn){
        if(autoScrollState.isStart)
            KeepScreenOn(true)
        else
            KeepScreenOn(false)
    } else {
        KeepScreenOn(true)
    }
    DrawerScreen(
        drawerContainerState = drawerContainerState,
        contentState = contentState,
        drawerState = drawerState,
        drawerLazyColumnState = drawerLazyColumnState,
        colorPaletteState = colorPaletteState,
        hazeState = hazeState,
        onDrawerItemClick ={
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateDrawerState(false))
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateCurrentTOC(it))
            viewModel.onContentAction(dataStoreManager,ContentAction.UpdateCurrentChapterIndex(it))
        },
    ) {
        LaunchedEffect(contentState.book) {
            if (contentState.book != null) {
                viewModel.setupTTS(context)
//                viewModel.initialize(context)
                viewModel.startTTSService(context,textMeasurer)
                autoScrollViewModel.onAction(AutoScrollAction.UpdateAutoScrollSpeed(dataStoreManager.autoScrollSpeed.first()))
                viewModel.onContentAction(dataStoreManager,ContentAction.UpdateKeepScreenOn(dataStoreManager.keepScreenOn.first()))
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
            }
        }
        LaunchedEffect(contentState.currentChapterIndex) {
            drawerLazyColumnState.scrollToItem(contentState.currentChapterIndex)
            drawerContainerViewModel.onAction(DrawerContainerAction.UpdateCurrentTOC(contentState.currentChapterIndex))
            pagerState?.animateScrollToPage(contentState.currentChapterIndex)
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
        Scaffold(
            topBar = {
                TopBar(
                    hazeState = hazeState,
                    topBarState = topBarState.visibility,
                    colorPaletteState = colorPaletteState,
                    onMenuIconClick ={
                        drawerContainerViewModel.onAction(DrawerContainerAction.UpdateDrawerState(true))
                        topBarViewModel.onAction(TopBarAction.UpdateVisibility(false))
                        bottomBarViewModel.onAction(BottomBarAction.UpdateVisibility(false))
                    },
                    onBackIconClick = {
                        onBackClick()
                        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateBookInfoCurrentChapterIndex(contentState.currentChapterIndex))
                        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateBookInfoFirstParagraphIndex(contentState.firstVisibleItemIndex))
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
                            bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarDefaultState(false))
                            bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarTTSState(true))
                            viewModel.onContentAction(dataStoreManager, ContentAction.UpdateIsSpeaking(true))
                            viewModel.onContentAction(
                                dataStoreManager,
                                ContentAction.UpdateCurrentReadingParagraph(contentState.firstVisibleItemIndex)
                            )
                            delay(1000)
                            contentState.service?.startPlayback()
//                            delay(1000)
//                            viewModel.play(
//                                MediaItem.Builder()
//                                    .setMediaMetadata(
//                                        MediaMetadata.Builder()
//                                            .setTitle("title from media item")
//                                            .setArtist("artist from media item")
//                                            .build()
//                                    )
////                                  .setUri("")
//                                    .setUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3")
//                                    .build()
//                            )
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
                                viewModel.onContentAction(dataStoreManager,
                                    ContentAction.UpdatePreviousChapterIndex(
                                        contentState.currentChapterIndex
                                    )
                                )
                            } else {
                                viewModel.onContentAction(dataStoreManager,
                                    ContentAction.UpdatePreviousChapterIndex(
                                        index
                                    )
                                )
                            }
                            viewModel.onContentAction(dataStoreManager,ContentAction.UpdateCurrentChapterIndex(index))
                            viewModel.onContentAction(dataStoreManager,ContentAction.UpdateBookInfoCurrentChapterIndex(index))
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
@Composable
fun KeepScreenOn(isKeepScreenOn: Boolean) = AndroidView(
    factory = {
        View(it).apply {
            keepScreenOn = isKeepScreenOn
        }
    }
)