package com.capstone.bookshelf.presentation.bookcontent.bottomBar.component

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarAction
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarState
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.component.dialog.VoiceMenuDialog
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSState
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSViewModel
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild

@Composable
fun BottomBarSetting(
    hazeState: HazeState,
    style: HazeStyle,
    bottomBarViewModel: BottomBarViewModel,
    ttsViewModel : TTSViewModel,
    bottomBarState: BottomBarState,
    ttsState: TTSState,
    colorPaletteState: ColorPalette,
    textToSpeech: TextToSpeech,
    dataStoreManager: DataStoreManager,
    context: Context,
    onSwitchChange: (Boolean) -> Unit
) {
    if(bottomBarState.openTTSVoiceMenu){
        VoiceMenuDialog(
            ttsViewModel = ttsViewModel,
            bottomBarState = bottomBarState,
            ttsState = ttsState,
            colorPaletteState = colorPaletteState,
            textToSpeech = textToSpeech,
            dataStoreManager = dataStoreManager,
            onDismiss = {
                bottomBarViewModel.onAction(BottomBarAction.OpenSetting(false))
                bottomBarViewModel.onAction(BottomBarAction.OpenVoiceMenuSetting(false))
            },
            testVoiceButtonClicked = {
                textToSpeech.speak("This is an example of a voice", TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            modifier = Modifier
                .padding(4.dp),
            text = "Setting",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorPaletteState.textColor
            )
        )
        HorizontalDivider(thickness = 2.dp)
        Row(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Keep Screen On",
                style = TextStyle(
                    color = colorPaletteState.textColor
                )
            )
            Switch(
                checked = bottomBarState.screenShallBeKeptOn,
                onCheckedChange = {
//                    viewModel.updateKeepScreenOn(it)
//                    viewModel.updateBookSettingKeepScreenOn(it)
                    onSwitchChange(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorPaletteState.textColor,
                    checkedTrackColor = colorPaletteState.textColor.copy(0.5f),
                    checkedBorderColor = colorPaletteState.textColor,
                    uncheckedThumbColor = colorPaletteState.textColor,
                    uncheckedTrackColor = colorPaletteState.textColor.copy(0.5f),
                    uncheckedBorderColor = colorPaletteState.textColor,
                )
            )
        }
        HorizontalDivider(thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable {
                    bottomBarViewModel.onAction(BottomBarAction.OpenSetting(true))
                    ttsViewModel.loadTTSSetting(dataStoreManager,textToSpeech)
                    bottomBarViewModel.onAction(BottomBarAction.OpenVoiceMenuSetting(true))
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Text to Speech",
                style = TextStyle(
                    color = colorPaletteState.textColor
                )
            )
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(id = R.drawable.ic_setting),
                tint = colorPaletteState.textColor,
                contentDescription = "text to speech"
            )
        }
        HorizontalDivider(thickness = 1.dp)
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(50.dp)
                .clickable{

                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Auto Scroll Up",
                style = TextStyle(
                    color = colorPaletteState.textColor
                )
            )
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(id = R.drawable.ic_setting),
                tint = colorPaletteState.textColor,
                contentDescription = "auto scroll up"
            )
        }
    }
}