package com.capstone.bookshelf.feature.readbook.presentation.component.bottomBar

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.bookshelf.R
import com.capstone.bookshelf.feature.readbook.presentation.BookContentViewModel
import com.capstone.bookshelf.feature.readbook.presentation.component.dialog.MusicMenuDialog
import com.capstone.bookshelf.feature.readbook.presentation.component.dialog.VoiceMenuDialog
import com.capstone.bookshelf.feature.readbook.presentation.state.ContentUIState
import com.capstone.bookshelf.feature.readbook.presentation.state.TTSState

@Composable
fun BottomBarTheme(
    uiState : ContentUIState,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                text = "Theme",
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun BottomBarSetting(
    bookContentViewModel: BookContentViewModel,
    uiState : ContentUIState,
    textToSpeech: TextToSpeech,
    ttsState: TTSState
) {
    if(uiState.openTTSVoiceMenu){
        VoiceMenuDialog(
            bookContentViewModel = bookContentViewModel,
            textToSpeech = textToSpeech,
            ttsState = ttsState,
            uiState = uiState,
            testVoiceButtonClicked = {
                textToSpeech.speak("This is an example of a voice", TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
            }
        )
    }
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(300.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            modifier = Modifier
                .padding(4.dp),
            text = "Setting",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
        HorizontalDivider(thickness = 2.dp)
        Row(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Keep Screen On")
            RadioButton(
                selected = uiState.screenShallBeKeptOn,
                onClick = {
                    bookContentViewModel.updateKeepScreenOn(!uiState.screenShallBeKeptOn)
                    bookContentViewModel.updateBookSettingKeepScreenOn(!uiState.screenShallBeKeptOn)
                }
            )
        }
        HorizontalDivider(thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable {
                    bookContentViewModel.changeMenuTriggerVoice(true)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Text to Speech")
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(id = R.drawable.ic_setting),
                contentDescription = "text to speech"
            )
        }
        HorizontalDivider(thickness = 1.dp)
        Row(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Keep Screen On")

        }
    }
}

@Composable
fun BottomBarTTS(
    bookContentViewModel: BookContentViewModel,
    textToSpeech: TextToSpeech,
    uiState : ContentUIState,
    ttsState : TTSState,
    onPreviousChapterIconClick: () -> Unit,
    onPreviousParagraphIconClick: () -> Unit,
    onPlayPauseIconClick: () -> Unit,
    onNextParagraphIconClick: () -> Unit,
    onNextChapterIconClick: () -> Unit,
    onTimerIconClick: () -> Unit,
    onStopIconClick: () -> Unit,
    onBackgroundMusicIconClick: () -> Unit,
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
            .background(MaterialTheme.colorScheme.surfaceContainer),
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
                        contentDescription = "play/pause"
                    )
                }else{
                    Icon(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = iconList[2]),
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
                    contentDescription = "next chapter"
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    contentDescription = "stop tts"
                )
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onBackgroundMusicIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[7]),
                    contentDescription = "background music"
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
                    contentDescription = "tts setting"
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
    }

    if(uiState.openTTSMusicMenu){
        MusicMenuDialog(
            bookContentViewModel = bookContentViewModel
        )
    }
    if(uiState.openTTSVoiceMenu){
        VoiceMenuDialog(
            bookContentViewModel = bookContentViewModel,
            textToSpeech = textToSpeech,
            ttsState = ttsState,
            uiState = uiState,
            testVoiceButtonClicked = {

            }
        )
    }
}

@Composable
fun BottomBarDefault(
    uiState : ContentUIState,
    onThemeIconClick: () -> Unit,
    onPreviousChapterIconClick: () -> Unit,
    onTTSIconClick: () -> Unit,
    onNextChapterIconClick: () -> Unit,
    onSettingIconClick: () -> Unit,
){
    val iconList = listOf(
        R.drawable.ic_theme,
        R.drawable.ic_previous,
        R.drawable.ic_headphone,
        R.drawable.ic_next,
        R.drawable.ic_setting
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Spacer(modifier = Modifier.height(10.dp))
        uiState.currentChapterHeader?.let {
            Text(
                modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                text = it,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(MaterialTheme.colorScheme.surfaceContainer),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onThemeIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[0]),
                    contentDescription = "theme"
                )
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onPreviousChapterIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[1]),
                    contentDescription = "previous chapter"
                )
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onTTSIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[2]),
                    contentDescription = "start tts"
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
                    painter = painterResource(id = iconList[3]),
                    contentDescription = "next chapter"
                )
            }

            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onSettingIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[4]),
                    contentDescription = "setting"
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun BottomBarAutoScroll(
    bookContentViewModel: BookContentViewModel,
    ttsState : TTSState,
    uiState : ContentUIState,
    onPreviousChapterIconClick: () -> Unit,
    onPlayPauseIconClick: () -> Unit,
    onNextChapterIconClick: () -> Unit,
    onMusicIconClick: () -> Unit,
    onStopIconClick: () -> Unit,
    onSettingIconClick: () -> Unit,
){
    val iconList = listOf(
        R.drawable.ic_previous_chapter,
        R.drawable.ic_play,
        R.drawable.ic_pause,
        R.drawable.ic_next_chapter,
        //R.drawable.ic_music
        R.drawable.ic_stop,
        //R.drawable.ic_setting
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                            .wrapContentHeight()
                            .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onPreviousChapterIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[0]),
                    contentDescription = "previous chapter"
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
                        painter = painterResource(id = iconList[1]),
                        contentDescription = "play/pause"
                    )
                }else{
                    Icon(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = iconList[2]),
                        contentDescription = "play/pause"
                    )
                }
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onNextChapterIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[3]),
                    contentDescription = "next chapter"
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth()
                            .wrapContentHeight()
                            .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onMusicIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[4]),
                    contentDescription = "music"
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
                    painter = painterResource(id = iconList[4]),
                    contentDescription = "stop"
                )
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onSettingIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[4]),
                    contentDescription = "setting"
                )
            }
        }
        if(uiState.openTTSMusicMenu){
            MusicMenuDialog(
                bookContentViewModel = bookContentViewModel
            )
        }
        // if(uiState.openAutoScrollMenu){
        //     AutoScrollMenuDialog(
        //         bookContentViewModel = bookContentViewModel
        //     )
        // }
    }
}
