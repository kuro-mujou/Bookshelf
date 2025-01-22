package com.capstone.bookshelf.presentation.bookcontent.bottomBar

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.capstone.bookshelf.presentation.bookcontent.component.font.FontState
import com.capstone.bookshelf.presentation.bookcontent.component.font.FontViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSAction
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSState
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBarAction
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBarViewModel
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun BottomBarManager(
    topBarViewModel: TopBarViewModel,
    bottomBarViewModel: BottomBarViewModel,
    autoScrollViewModel: AutoScrollViewModel,
    ttsViewModel: TTSViewModel,
    colorPaletteViewModel: ColorPaletteViewModel,
    fontViewModel: FontViewModel,
    hazeState: HazeState,
    bottomBarState : BottomBarState,
    ttsState: TTSState,
    autoScrollState: AutoScrollState,
    drawerContainerState: DrawerContainerState,
    dataStoreManager : DataStoreManager,
    colorPaletteState: ColorPalette,
    fontState: FontState,
    textToSpeech: TextToSpeech,
    context: Context,
){
    val style = HazeMaterials.ultraThin(colorPaletteState.containerColor)
    LaunchedEffect(bottomBarState.visibility){
        if(!bottomBarState.visibility) {
            if(!ttsState.isSpeaking && !ttsState.isPaused && !autoScrollState.isStart && !autoScrollState.isPaused){
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
            drawerContainerState = drawerContainerState,
            colorPaletteState = colorPaletteState,
            onThemeIconClick = {
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarDefaultState(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarThemeState(true))
            },
            onTTSIconClick = {
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarDefaultState(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateBottomBarTTSState(true))
                ttsViewModel.onAction(dataStoreManager,TTSAction.UpdateIsSpeaking(true))
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
            hazeState = hazeState,
            style = style,
            autoScrollViewModel = autoScrollViewModel,
            bottomBarState = bottomBarState,
            autoScrollState = autoScrollState,
            colorPaletteState = colorPaletteState,
            onPreviousChapterIconClick = {

            },
            onPlayPauseIconClick = {
                autoScrollViewModel.onAction(AutoScrollAction.UpdateIsPaused(!autoScrollState.isPaused))
//                bottomBarViewModel.onAction(BottomBarAction.UpdateVisibility(false))
//                topBarViewModel.onAction(TopBarAction.UpdateVisibility(false))
            },
            onNextChapterIconClick = {

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
            hazeState = hazeState,
            style = style,
            bottomBarViewModel = bottomBarViewModel,
            ttsViewModel = ttsViewModel,
            bottomBarState = bottomBarState,
            ttsState = ttsState,
            colorPaletteState = colorPaletteState,
            textToSpeech = textToSpeech,
            dataStoreManager = dataStoreManager,
            context = context,
            onSwitchChange = {
                bottomBarViewModel.onAction(BottomBarAction.UpdateKeepScreenOn(it))
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
            ttsViewModel = ttsViewModel,
            bottomBarState = bottomBarState,
            ttsState = ttsState,
            colorPaletteState = colorPaletteState,
            textToSpeech = textToSpeech,
            dataStoreManager = dataStoreManager,
            onPreviousChapterIconClick = {

            },
            onPreviousParagraphIconClick = {

            },
            onPlayPauseIconClick = {

            },
            onNextParagraphIconClick = {

            },
            onNextChapterIconClick = {

            },
            onTimerIconClick = {

            },
            onStopIconClick = {
                ttsViewModel.onAction(dataStoreManager,TTSAction.UpdateIsSpeaking(false))
                bottomBarViewModel.onAction(BottomBarAction.UpdateVisibility(false))
                topBarViewModel.onAction(TopBarAction.UpdateVisibility(false))
            },
            onTTSSettingIconClick = {

            }
        )
    }
    AnimatedVisibility(
        visible = bottomBarState.bottomBarThemeState && bottomBarState.visibility,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        BottomBarTheme(
            hazeState = hazeState,
            style = style,
            colorPaletteViewModel = colorPaletteViewModel,
            fontViewModel = fontViewModel,
            bottomBarState = bottomBarState,
            dataStore = dataStoreManager,
            colorPaletteState = colorPaletteState,
            fontState = fontState
        )
    }
//
//    when(state.bottomBarIndex) {
//        0 -> {
//            BottomBarDefault(
//                state = state,
//                onThemeIconClick = {
//                    scope.launch {
////                                viewModel.updateBottomBarState(false)
//                        delay(200)
////                                viewModel.updateBottomBarIndex(1)
////                                viewModel.updateBottomBarState(true)
//                    }
//                },
//                onTTSIconClick = {
//                    scope.launch {
////                                viewModel.loadTTSSetting(textToSpeech)
////                                viewModel.updateIsPaused(false)
////                                viewModel.updateIsSpeaking(true)
////                                viewModel.updateBottomBarState(false)
//                        delay(200)
////                                viewModel.updateBottomBarIndex(3)
////                                viewModel.updateBottomBarState(true)
////                                viewModel.updateEnablePagerScroll(false)
//                        readNextParagraph(
//                            tts = textToSpeech,
//                            state = state,
//                            chapterContents = chapterContents,
//                            targetParagraphIndex = currentLazyColumnState?.firstVisibleItemIndex!!,
//                            currentChapterIndex = state.currentChapterIndex,
//                            currentPosition = 0,
//                            isReading = true,
//                            maxWidth = state.screenWidth,
//                            maxHeight = state.screenHeight,
//                            textStyle = textStyle,
//                            textMeasurer = textMeasurer,
//                            shouldScroll = state.flagTriggerScrolling
//                        ) { index,chapterIndex,currentPos,scroll,times,stopReading ->
//                            updateVariable(
//                                false,
//                                stopReading,
//                                index,
//                                chapterIndex,
//                                currentPos,
//                                scroll,
//                                times
//                            )
//                        }
//                    }
//                },
//                onAutoScrollIconClick = {
//                    scope.launch {
////                                viewModel.loadTTSSetting(textToSpeech)
////                                viewModel.updateBottomBarState(false)
//                        delay(200)
////                                viewModel.updateBottomBarIndex(4)
////                                viewModel.updateBottomBarState(true)
////                                viewModel.updateIsAutoScroll(true)
//                    }
//                },
//                onSettingIconClick = {
//                    scope.launch {
////                                viewModel.loadTTSSetting(textToSpeech)
////                                viewModel.updateBottomBarState(false)
//                        delay(200)
////                                viewModel.updateBottomBarIndex(2)
////                                viewModel.updateBottomBarState(true)
////                                viewModel.changeMenuTriggerSetting(true)
//                    }
//                },
//            )
//        }
//
//        1 -> {
//            BottomBarTheme(
//                state = state,
//            )
//        }
//        2 -> {
//            BottomBarSetting(
//                viewModel = viewModel,
//                state = state,
//                textToSpeech = textToSpeech,
//                context = context
//            )
//        }
//        3 -> {
//            BottomBarTTS(
//                viewModel = viewModel,
//                textToSpeech = textToSpeech,
//                state = state,
//                onPreviousChapterIconClick = {
//                    stopReading(textToSpeech)
//                    updateVariable(
//                        true,
//                        false,
//                        0,
//                        maxOf(state.currentChapterIndex-1,0),
//                        0,
//                        false,
//                        0
//                    )
//                },
//                onPreviousParagraphIconClick = {
//                    if (state.isSpeaking) {
////                                viewModel.updateIsSpeaking(true)
////                                viewModel.updateCurrentReadingPosition(0)
//                        readNextParagraph(
//                            tts = textToSpeech,
//                            state = state,
//                            chapterContents = chapterContents,
//                            targetParagraphIndex = maxOf(state.currentReadingParagraph - 1,0),
//                            currentChapterIndex = state.currentChapterIndex,
//                            currentPosition = 0,
//                            isReading = true,
//                            maxWidth = state.screenWidth,
//                            maxHeight = state.screenHeight,
//                            textStyle = textStyle,
//                            textMeasurer = textMeasurer,
//                            shouldScroll = state.flagTriggerScrolling,
//                        ) { index, chapterIndex, currentPos,scroll,times,stopReading ->
//                            updateVariable(
//                                false,
//                                stopReading,
//                                index,
//                                chapterIndex,
//                                currentPos,
//                                scroll,
//                                times
//                            )
//                        }
//                    } else if(state.isPaused){
//                        updateVariable(
//                            true,
//                            false,
//                            maxOf(state.currentReadingParagraph - 1,0),
//                            state.currentChapterIndex,
//                            0,
//                            state.flagTriggerScrolling,
//                            0
//                        )
//                    }
//                },
//                onPlayPauseIconClick = {
//                    if (state.isSpeaking){
//                        stopReading(tts = textToSpeech)
//                        updateVariable(
//                            true,
//                            false,
//                            state.currentReadingParagraph,
//                            state.currentChapterIndex,
//                            state.currentReadingPosition,
//                            state.flagTriggerScrolling,
//                            0
//                        )
//                    } else if(state.isPaused){
////                                viewModel.updateIsSpeaking(true)
////                                viewModel.updateIsPaused(false)
//                        readNextParagraph(
//                            tts = textToSpeech,
//                            state = state,
//                            chapterContents = chapterContents,
//                            targetParagraphIndex = state.currentReadingParagraph,
//                            currentChapterIndex = state.currentChapterIndex,
//                            currentPosition = state.currentReadingPosition,
//                            isReading = true,
//                            maxWidth = state.screenWidth,
//                            maxHeight = state.screenHeight,
//                            textStyle = textStyle,
//                            textMeasurer = textMeasurer,
//                            shouldScroll = state.flagTriggerScrolling
//                        ) { index, chapterIndex, currentPos,scroll,times,stopReading ->
//                            updateVariable(
//                                false,
//                                stopReading,
//                                index,
//                                chapterIndex,
//                                currentPos,
//                                scroll,
//                                times
//                            )
//                        }
//                    }
//                },
//                onNextParagraphIconClick = {
//                    if (state.isSpeaking) {
////                                viewModel.updateIsPaused(false)
////                                viewModel.updateIsSpeaking(true)
//                        readNextParagraph(
//                            tts = textToSpeech,
//                            state = state,
//                            chapterContents = chapterContents,
//                            targetParagraphIndex = minOf(state.currentReadingParagraph + 1,currentLazyColumnState?.layoutInfo?.totalItemsCount!! - 1),
//                            currentChapterIndex = state.currentChapterIndex,
//                            currentPosition = 0,
//                            isReading = true,
//                            maxWidth = state.screenWidth,
//                            maxHeight = state.screenHeight,
//                            textStyle = textStyle,
//                            textMeasurer = textMeasurer,
//                            shouldScroll = state.flagTriggerScrolling
//                        ) { index, chapterIndex, currentPos,scroll,times,stopReading ->
//                            updateVariable(
//                                false,
//                                stopReading,
//                                index,
//                                chapterIndex,
//                                currentPos,
//                                scroll,
//                                times
//                            )
//                        }
//                    }else if (state.isPaused){
//                        updateVariable(
//                            true,
//                            false,
//                            minOf(state.currentReadingParagraph + 1,currentLazyColumnState?.layoutInfo?.totalItemsCount!! - 1),
//                            state.currentChapterIndex,
//                            0,
//                            state.flagTriggerScrolling,
//                            0
//                        )
//                    }
//                },
//                onNextChapterIconClick = {
//                    stopReading(textToSpeech)
//                    updateVariable(
//                        true,
//                        false,
//                        0,
//                        minOf(state.currentChapterIndex+1,state.totalChapter-1),
//                        0,
//                        false,
//                        0
//                    )
//                },
//                onTimerIconClick = {
//
//                },
//                onStopIconClick = {
//                    stopReading(textToSpeech)
//                    updateVariable(
//                        false,
//                        false,
//                        state.currentReadingParagraph,
//                        state.currentChapterIndex,
//                        0,
//                        state.flagTriggerScrolling,
//                        0
//                    )
////                            viewModel.updateEnablePagerScroll(true)
////                            viewModel.updateBottomBarState(false)
////                            viewModel.updateTopBarState(false)
//                },
//                onTTSSettingIconClick = {
////                            viewModel.changeMenuTriggerVoice(!state.openTTSVoiceMenu)
//                    stopReading(tts = textToSpeech)
//                    updateVariable(
//                        true,
//                        false,
//                        state.currentReadingParagraph,
//                        state.currentChapterIndex,
//                        state.currentReadingPosition,
//                        state.flagTriggerScrolling,
//                        0
//                    )
//                }
//            )
//        }
//        4 ->{
//            BottomBarAutoScroll(
//                viewModel = viewModel,
//                state = state,
//                onPreviousChapterIconClick ={
////                            viewModel.updateCurrentChapterIndex(maxOf(state.currentChapterIndex-1,0))
//                },
//                onPlayPauseIconClick = {
//                    if(state.isAutoScroll){
////                                viewModel.updateIsAutoScroll(false)
////                                viewModel.updateIsAutoScrollPaused(true)
//                    }else if(state.isAutoScrollPaused){
////                                viewModel.updateIsAutoScroll(true)
////                                viewModel.updateIsAutoScrollPaused(false)
//                    }
//                },
//                onNextChapterIconClick = {
////                            viewModel.updateCurrentChapterIndex(minOf(state.currentChapterIndex+1,state.totalChapter-1))
//                },
//                onStopIconClick = {
////                            viewModel.updateIsAutoScroll(false)
////                            viewModel.updateIsAutoScrollPaused(false)
////                            viewModel.updateBottomBarState(false)
////                            viewModel.updateTopBarState(false)
//                },
//                onSettingIconClick = {
////                            viewModel.changeMenuTriggerAutoScroll(true)
//                },
//            )
//        }
//    }
}