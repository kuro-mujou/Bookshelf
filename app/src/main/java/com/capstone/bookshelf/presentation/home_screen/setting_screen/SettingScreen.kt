package com.capstone.bookshelf.presentation.home_screen.setting_screen

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.home_screen.setting_screen.component.AutoScrollSetting
import com.capstone.bookshelf.presentation.home_screen.setting_screen.component.BookmarkMenu
import com.capstone.bookshelf.presentation.home_screen.setting_screen.component.MusicMenu
import com.capstone.bookshelf.presentation.home_screen.setting_screen.component.VoiceSetting
import com.capstone.bookshelf.util.DataStoreManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    settingState: SettingState,
    dataStoreManager: DataStoreManager,
    onAction: (SettingAction) -> Unit,
) {
    val context = LocalContext.current
    val musicMenuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val bookmarkMenuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var openBackgroundMusicMenu by remember { mutableStateOf(false) }
    var openBookmarkThemeMenu by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (settingState.tts == null) {
            onAction(SettingAction.SetupTTS(context))
        } else {
            onAction(SettingAction.LoadTTSSetting)
        }
    }
    if (settingState.openTTSVoiceMenu) {
        VoiceSetting(
            tts = settingState.tts,
            settingState = settingState,
            dataStoreManager = dataStoreManager,
            onDismiss = {
                onAction(SettingAction.OpenTTSVoiceMenu(false))
                if(settingState.currentVoice == null) {
                    settingState.tts?.let {
                        onAction(SettingAction.FixNullVoice(it))
                    }
                }
            },
            testVoiceButtonClicked = {
                settingState.tts?.language = settingState.currentLanguage
                settingState.tts?.voice = settingState.currentVoice
                settingState.currentPitch.let { settingState.tts?.setPitch(it) }
                settingState.currentSpeed.let { settingState.tts?.setSpeechRate(it) }
                settingState.tts?.speak("xin chào", TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
            },
            onAction = onAction
        )
    }
    if (settingState.openAutoScrollMenu) {
        AutoScrollSetting(
            settingState = settingState,
            onDismissRequest = {
                onAction(SettingAction.OpenAutoScrollMenu(false))
            },
            onAction = onAction,
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp),
            text = "Setting",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        )
        HorizontalDivider(thickness = 2.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(24.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_keep_screen_on),
                contentDescription = "background music"
            )
            Text(
                text = "Keep Screen On",
                style = TextStyle(
                    fontSize = 16.sp,
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = settingState.keepScreenOn,
                onCheckedChange = {
                    onAction(SettingAction.KeepScreenOn(it))
                },
            )
        }
        HorizontalDivider(thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable {
                    openBackgroundMusicMenu = true
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(24.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_music_background),
                contentDescription = "background music"
            )
            Text(
                text = "Background Music",
                style = TextStyle(
                    fontSize = 16.sp,
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(30.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_right),
                contentDescription = "background music"
            )
        }
        HorizontalDivider(thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable {
                    openBookmarkThemeMenu = true
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(24.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_tag),
                contentDescription = "Bookmark theme"
            )
            Text(
                text = "Bookmark theme",
                style = TextStyle(
                    fontSize = 16.sp,
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(30.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_right),
                contentDescription = "Bookmark theme"
            )
        }
        HorizontalDivider(thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable {
                    onAction(SettingAction.OpenTTSVoiceMenu(true))
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(24.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_headphones),
                contentDescription = "text to speech"
            )
            Text(
                text = "Text to Speech",
                style = TextStyle(
                    fontSize = 16.sp,
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(30.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_right),
                contentDescription = "text to speech"
            )
        }
        HorizontalDivider(thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable {
                    onAction(SettingAction.OpenAutoScrollMenu(true))
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(24.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_scroll),
                contentDescription = "auto scroll up"
            )
            Text(
                text = "Auto Scroll Up",
                style = TextStyle(
                    fontSize = 16.sp,
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .size(30.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_right),
                contentDescription = "auto scroll up"
            )
        }
    }
    if (openBackgroundMusicMenu) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
            sheetState = musicMenuSheetState,
            onDismissRequest = { openBackgroundMusicMenu = false },
        ) {
            MusicMenu(
                settingState = settingState,
                onAction = onAction
            )
        }
    }
    if (openBookmarkThemeMenu) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
            sheetState = bookmarkMenuSheetState,
            onDismissRequest = { openBookmarkThemeMenu = false },
        ) {
            BookmarkMenu(
                settingState = settingState,
                onAction = onAction
            )
        }
    }
}

