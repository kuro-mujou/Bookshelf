package com.capstone.bookshelf.presentation.bookcontent.bottomBar.component

import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarAction
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarState
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.component.dialog.VoiceMenuDialog
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

@Composable
@UnstableApi
fun BottomBarTTS(
    viewModel : ContentViewModel,
    bottomBarViewModel: BottomBarViewModel,
    contentState : ContentState,
    hazeState: HazeState,
    style: HazeStyle,
    bottomBarState: BottomBarState,
    colorPaletteState: ColorPalette,
    drawerContainerState : DrawerContainerState,
    tts: TextToSpeech,
    dataStoreManager: DataStoreManager,
    onPreviousChapterIconClick: () -> Unit,
    onPreviousParagraphIconClick: () -> Unit,
    onPlayPauseIconClick: () -> Unit,
    onNextParagraphIconClick: () -> Unit,
    onNextChapterIconClick: () -> Unit,
    onMusicIconClick: () -> Unit,
    onStopIconClick: () -> Unit,
    onTTSSettingIconClick: () -> Unit,
) {
    val iconList = listOf(
        R.drawable.ic_skip_to_back,
        R.drawable.ic_backward,
        R.drawable.ic_play,
        R.drawable.ic_pause,
        R.drawable.ic_forward,
        R.drawable.ic_skip_to_next,
        R.drawable.ic_music_background,
        R.drawable.ic_stop,
        R.drawable.ic_setting
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                    Modifier.hazeEffect(
                        state = hazeState,
                        style = style
                    )
                }else{
                    Modifier.background(colorPaletteState.containerColor)
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        drawerContainerState.currentTOC?.let {
            Text(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .basicMarquee(
                        animationMode = MarqueeAnimationMode.Immediately,
                        initialDelayMillis = 0,
                        repeatDelayMillis = 0
                    ),
                text = it.title,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = colorPaletteState.textColor,
                    textAlign = TextAlign.Center,
                    fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                ),
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onPreviousChapterIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[0]),
                    tint = colorPaletteState.textColor,
                    contentDescription = "previous chapter"
                )
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onPreviousParagraphIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[1]),
                    tint = colorPaletteState.textColor,
                    contentDescription = "previous paragraph"
                )
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onPlayPauseIconClick()
                }
            ) {
                if(contentState.isSpeaking && !contentState.isPaused) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = iconList[3]),
                        contentDescription = "play/pause",
                        tint = colorPaletteState.textColor
                    )
                }else{
                    Icon(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = iconList[2]),
                        tint = colorPaletteState.textColor,
                        contentDescription = "play/pause"
                    )
                }
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onNextParagraphIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[4]),
                    tint = colorPaletteState.textColor,
                    contentDescription = "next paragraph"
                )
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onNextChapterIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[5]),
                    tint = colorPaletteState.textColor,
                    contentDescription = "next chapter"
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onMusicIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[6]),
                    tint = colorPaletteState.textColor,
                    contentDescription = "background music"
                )
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onStopIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[7]),
                    tint = colorPaletteState.textColor,
                    contentDescription = "stop tts"
                )
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onTTSSettingIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[8]),
                    tint = colorPaletteState.textColor,
                    contentDescription = "tts setting"
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
    }

    if(bottomBarState.openTTSVoiceMenu){
        VoiceMenuDialog(
            viewModel = viewModel,
            bottomBarState = bottomBarState,
            contentState = contentState,
            colorPaletteState = colorPaletteState,
            tts = tts,
            dataStoreManager = dataStoreManager,
            onDismiss = {
                bottomBarViewModel.onAction(BottomBarAction.OpenSetting(false))
                bottomBarViewModel.onAction(BottomBarAction.OpenVoiceMenuSetting(false))
            },
            testVoiceButtonClicked = {

            }
        )
    }
}
