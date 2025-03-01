package com.capstone.bookshelf.presentation.bookcontent.bottomBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.component.BottomBarAutoScroll
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.component.BottomBarDefault
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.component.BottomBarSetting
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.component.BottomBarTTS
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.component.BottomBarTheme
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollAction
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollState
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPaletteViewModel
import com.capstone.bookshelf.presentation.bookcontent.content.ContentAction
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBarAction
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBarViewModel
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
@UnstableApi
fun BottomBarManager(
    viewModel: ContentViewModel,
    topBarViewModel: TopBarViewModel,
    bottomBarViewModel: BottomBarViewModel,
    autoScrollViewModel: AutoScrollViewModel,
    colorPaletteViewModel: ColorPaletteViewModel,
    hazeState: HazeState,
    bottomBarState: BottomBarState,
    contentState: ContentState,
    autoScrollState: AutoScrollState,
    drawerContainerState: DrawerContainerState,
    dataStoreManager: DataStoreManager,
    colorPaletteState: ColorPalette,
    connectToService: () -> Unit,
    onSwitchChange: (Boolean) -> Unit
){
    val style = HazeMaterials.ultraThin(colorPaletteState.containerColor)
    LaunchedEffect(bottomBarState.visibility){
        if(!bottomBarState.visibility) {
            if(!contentState.isSpeaking && !contentState.isPaused && !autoScrollState.isStart && !autoScrollState.isPaused){
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarDefaultState(true))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarThemeState(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarTTSState(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarAutoScrollState(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarSettingState(false))
            }
        }
    }
    AnimatedVisibility(
        visible = bottomBarState.bottomBarDefaultState && bottomBarState.visibility,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        BottomBarDefault(
            hazeState = hazeState,
            style = style,
            contentState = contentState,
            drawerContainerState = drawerContainerState,
            colorPaletteState = colorPaletteState,
            onThemeIconClick = {
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarDefaultState(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarThemeState(true))
            },
            onTTSIconClick = {
                connectToService()
            },
            onAutoScrollIconClick = {
                topBarViewModel.onAction(TopBarAction.UpdateVisibility(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateVisibility(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarDefaultState(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarAutoScrollState(true))
                autoScrollViewModel.onAction(AutoScrollAction.UpdateIsStart(true))
                autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(false))
            },
            onSettingIconClick = {
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarDefaultState(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarSettingState(true))
            }
        )
    }
    AnimatedVisibility(
        visible = bottomBarState.bottomBarAutoScrollState && bottomBarState.visibility,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        BottomBarAutoScroll(
            contentState = contentState,
            dataStoreManager = dataStoreManager,
            hazeState = hazeState,
            style = style,
            autoScrollViewModel = autoScrollViewModel,
            bottomBarState = bottomBarState,
            autoScrollState = autoScrollState,
            colorPaletteState = colorPaletteState,
            onPlayPauseIconClick = {
                autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(!autoScrollState.isPaused))
                bottomBarViewModel.onAction(BottomBarAction.UpdateVisibility(false))
                topBarViewModel.onAction(TopBarAction.UpdateVisibility(false))
            },
            onStopIconClick = {
                autoScrollViewModel.onAction(AutoScrollAction.UpdateIsStart(false))
                autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateVisibility(false))
                topBarViewModel.onAction(TopBarAction.UpdateVisibility(false))
            },
            onSettingIconClick = {
                bottomBarViewModel.onAction(BottomBarAction.OpenAutoScrollMenu(true))
            },
            onDismissDialogRequest = {
                bottomBarViewModel.onAction(BottomBarAction.OpenAutoScrollMenu(false))
            }
        )
    }
    AnimatedVisibility(
        visible = bottomBarState.bottomBarSettingState && bottomBarState.visibility,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        BottomBarSetting(
            viewModel = viewModel,
            bottomBarViewModel = bottomBarViewModel,
            autoScrollViewModel = autoScrollViewModel,
            contentState = contentState,
            bottomBarState = bottomBarState,
            colorPaletteState = colorPaletteState,
            hazeState = hazeState,
            autoScrollState = autoScrollState,
            style = style,
            dataStoreManager = dataStoreManager,
            tts = contentState.tts!!,
            onSwitchChange = {
                viewModel.onContentAction(dataStoreManager,ContentAction.UpdateKeepScreenOn(it))
                onSwitchChange(it)
            }
        )
    }
    AnimatedVisibility(
        visible = bottomBarState.bottomBarTTSState && bottomBarState.visibility,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        BottomBarTTS(
            hazeState = hazeState,
            style = style,
            viewModel = viewModel,
            bottomBarViewModel = bottomBarViewModel,
            bottomBarState = bottomBarState,
            contentState = contentState,
            colorPaletteState = colorPaletteState,
            tts = contentState.tts!!,
            dataStoreManager = dataStoreManager,
            onPreviousChapterIconClick = {
                contentState.service?.previousChapter()
            },
            onPreviousParagraphIconClick = {
                contentState.service?.previousParagraph()
            },
            onPlayPauseIconClick = {
                contentState.service?.resumePausePlayback()
                if(contentState.isSpeaking){
                    if(!contentState.isPaused){
                        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateIsPaused(true))
                    } else {
                        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateIsPaused(false))
                    }
                }
            },
            onNextParagraphIconClick = {
                contentState.service?.nextParagraph()
            },
            onNextChapterIconClick = {
                contentState.service?.nextChapter()
            },
            onTimerIconClick = {

            },
            onStopIconClick = {
                viewModel.onContentAction(dataStoreManager,ContentAction.UpdateIsSpeaking(false))
                viewModel.onContentAction(dataStoreManager,ContentAction.UpdateIsPaused(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateVisibility(false))
                topBarViewModel.onAction(TopBarAction.UpdateVisibility(false))
                contentState.service?.cancelPlayback()
            },
            onTTSSettingIconClick = {
                bottomBarViewModel.onAction(BottomBarAction.OpenSetting(false))
                viewModel.loadTTSSetting(dataStoreManager,contentState.tts)
                bottomBarViewModel.onAction(BottomBarAction.OpenVoiceMenuSetting(true))
            }
        )
    }
    AnimatedVisibility(
        visible = bottomBarState.bottomBarThemeState && bottomBarState.visibility,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        BottomBarTheme(
            viewModel = viewModel,
            colorPaletteViewModel = colorPaletteViewModel,
            contentState = contentState,
            colorPaletteState = colorPaletteState,
            hazeState = hazeState,
            style = style,
            dataStore = dataStoreManager,
        )
    }
}