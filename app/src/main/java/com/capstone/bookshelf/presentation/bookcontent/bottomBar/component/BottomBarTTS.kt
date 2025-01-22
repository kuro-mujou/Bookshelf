package com.capstone.bookshelf.presentation.bookcontent.bottomBar.component

import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarState
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.component.dialog.VoiceMenuDialog
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSState
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSViewModel
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild

@Composable
fun BottomBarTTS(
    hazeState: HazeState,
    style: HazeStyle,
    ttsViewModel: TTSViewModel,
    bottomBarState: BottomBarState,
    ttsState: TTSState,
    colorPaletteState: ColorPalette,
    textToSpeech: TextToSpeech,
    dataStoreManager: DataStoreManager,
    onPreviousChapterIconClick: () -> Unit,
    onPreviousParagraphIconClick: () -> Unit,
    onPlayPauseIconClick: () -> Unit,
    onNextParagraphIconClick: () -> Unit,
    onNextChapterIconClick: () -> Unit,
    onTimerIconClick: () -> Unit,
    onStopIconClick: () -> Unit,
    onTTSSettingIconClick: () -> Unit,
) {
    val iconList = listOf(
        R.drawable.ic_previous_chapter,
        R.drawable.ic_previous,
        R.drawable.ic_play,
        R.drawable.ic_pause,
        R.drawable.ic_next,
        R.drawable.ic_next_chapter,
        R.drawable.ic_timer,
        R.drawable.ic_stop,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                    Modifier.hazeChild(
                        state = hazeState,
                        style = style
                    )
                }else{
                    Modifier.background(colorPaletteState.containerColor)
                }
            ),
    ) {
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
                if(ttsState.isSpeaking) {
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
                    onTimerIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[6]),
                    tint = colorPaletteState.textColor,
                    contentDescription = "timer"
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
                    painter = painterResource(id = iconList[7]),
                    tint = colorPaletteState.textColor,
                    contentDescription = "tts setting"
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
    }

    if(bottomBarState.openTTSVoiceMenu){
        VoiceMenuDialog(
            ttsViewModel = ttsViewModel,
            bottomBarState = bottomBarState,
            ttsState = ttsState,
            colorPaletteState = colorPaletteState,
            textToSpeech = textToSpeech,
            dataStoreManager = dataStoreManager,
            onDismiss = {

            },
            testVoiceButtonClicked = {

            }
        )
    }
}
