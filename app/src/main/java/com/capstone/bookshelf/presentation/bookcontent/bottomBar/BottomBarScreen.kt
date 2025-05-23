package com.capstone.bookshelf.presentation.bookcontent.bottomBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.capstone.bookshelf.presentation.bookcontent.component.music.MusicMenu
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TtsUiEvent
import com.capstone.bookshelf.presentation.bookcontent.content.ContentAction
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import com.capstone.bookshelf.presentation.bookcontent.drawer.component.bookmark.BookmarkMenu
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBarAction
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBarViewModel
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class)
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
) {
    var openBackgroundMusicMenu by remember { mutableStateOf(false) }
    var openBookmarkThemeMenu by remember { mutableStateOf(false) }
    val style = HazeMaterials.thin(colorPaletteState.containerColor)
    val musicMenuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val bookmarkMenuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    LaunchedEffect(
        bottomBarState.visibility,
        contentState.isSpeaking,
        autoScrollState.stopAutoScroll
    ) {
        if (!bottomBarState.visibility) {
            if (contentState.isSpeaking) {
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarDefaultState(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarThemeState(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarTTSState(true))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarAutoScrollState(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarSettingState(false))
            } else if (!autoScrollState.stopAutoScroll) {
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarDefaultState(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarThemeState(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarTTSState(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarAutoScrollState(true))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarSettingState(false))
            } else {
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
                autoScrollViewModel.onAction(AutoScrollAction.UpdateStopAutoScroll(false))
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
                autoScrollViewModel.onAction(AutoScrollAction.UpdateStopAutoScroll(true))
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
            onKeepScreenOnChange = {
                viewModel.onContentAction(ContentAction.UpdateKeepScreenOn(it))
                onSwitchChange(it)
            },
            onEnableSpecialArtChange = {
                viewModel.onContentAction(ContentAction.UpdateEnableSpecialArt(it))
            },
            onBackgroundMusicSetting = {
                openBackgroundMusicMenu = true
            },
            onBookmarkThemeSetting = {
                openBookmarkThemeMenu = true
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
            drawerContainerState = drawerContainerState,
            tts = contentState.tts!!,
            onPreviousChapterIconClick = {
                viewModel.onTtsUiEvent(TtsUiEvent.SkipToBack)
            },
            onPreviousParagraphIconClick = {
                viewModel.onTtsUiEvent(TtsUiEvent.Backward)
            },
            onPlayPauseIconClick = {
                if (contentState.isSpeaking) {
                    if (!contentState.isPaused) {
                        viewModel.onContentAction(ContentAction.UpdateIsPaused(true))
                    } else {
                        viewModel.onContentAction(ContentAction.UpdateIsPaused(false))
                    }
                }
            },
            onNextParagraphIconClick = {
                viewModel.onTtsUiEvent(TtsUiEvent.Forward)
            },
            onNextChapterIconClick = {
                viewModel.onTtsUiEvent(TtsUiEvent.SkipToNext)
            },
            onMusicIconClick = {
                openBackgroundMusicMenu = true
            },
            onStopIconClick = {
                viewModel.onContentAction(ContentAction.UpdateIsSpeaking(false))
                viewModel.onContentAction(ContentAction.UpdateIsPaused(false))
                viewModel.onTtsUiEvent(TtsUiEvent.Stop)
            },
            onTTSSettingIconClick = {
                bottomBarViewModel.onAction(BottomBarAction.OpenSetting(false))
                viewModel.loadTTSSetting(contentState.tts)
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
    if (openBackgroundMusicMenu) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
            dragHandle = {
                Surface(
                    modifier = Modifier
                        .padding(vertical = 22.dp),
                    color = colorPaletteState.textColor,
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Box(Modifier.size(width = 32.dp, height = 4.dp))
                }
            },
            sheetState = musicMenuSheetState,
            onDismissRequest = { openBackgroundMusicMenu = false },
            containerColor = colorPaletteState.backgroundColor
        ) {
            MusicMenu(
                contentViewModel = viewModel,
                dataStoreManager = dataStoreManager,
                colorPalette = colorPaletteState,
                contentState = contentState
            )
        }
    }
    if (openBookmarkThemeMenu) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
            dragHandle = {
                Surface(
                    modifier = Modifier
                        .padding(vertical = 22.dp),
                    color = colorPaletteState.textColor,
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Box(Modifier.size(width = 32.dp, height = 4.dp))
                }
            },
            sheetState = bookmarkMenuSheetState,
            onDismissRequest = { openBookmarkThemeMenu = false },
            containerColor = colorPaletteState.backgroundColor
        ) {
            BookmarkMenu(
                contentViewModel = viewModel,
                dataStoreManager = dataStoreManager,
                colorPalette = colorPaletteState,
                contentState = contentState
            )
        }
    }
}