package com.capstone.bookshelf.presentation.home_screen.setting_screen

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.home_screen.setting_screen.component.AutoScrollSetting
import com.capstone.bookshelf.presentation.home_screen.setting_screen.component.BookCategoryMenu
import com.capstone.bookshelf.presentation.home_screen.setting_screen.component.BookmarkMenu
import com.capstone.bookshelf.presentation.home_screen.setting_screen.component.MusicMenu
import com.capstone.bookshelf.presentation.home_screen.setting_screen.component.SpecialCodeDialog
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
    val categoryMenuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var openBackgroundMusicMenu by remember { mutableStateOf(false) }
    var openBookmarkThemeMenu by remember { mutableStateOf(false) }
    var openCategoryMenu by remember { mutableStateOf(false) }
    var openSpecialCodeDialog by remember { mutableStateOf(false) }
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
                if (settingState.currentVoice == null) {
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
            .fillMaxSize()
            .padding(
                top = WindowInsets.safeContent
                    .only(WindowInsetsSides.Top)
                    .asPaddingValues()
                    .calculateTopPadding(),
                end = WindowInsets.safeContent
                    .only(WindowInsetsSides.End)
                    .asPaddingValues()
                    .calculateRightPadding(LayoutDirection.Ltr)
            ),
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
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable {
                        onAction(SettingAction.KeepScreenOn(!settingState.keepScreenOn))
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(24.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_keep_screen_on),
                    contentDescription = "keep screen on"
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
                Spacer(modifier = Modifier.width(8.dp))
            }
            HorizontalDivider(thickness = 1.dp)
            if (settingState.unlockSpecialCodeStatus) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            onAction(SettingAction.UpdateEnableSpecialArt(!settingState.enableSpecialArt))
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp)
                            .size(24.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_favourite_music),
                        contentDescription = "enableSpecialArt"
                    )
                    Text(
                        text = "Enable special Art",
                        style = TextStyle(
                            fontSize = 16.sp,
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = settingState.enableSpecialArt,
                        onCheckedChange = {
                            onAction(SettingAction.UpdateEnableSpecialArt(it))
                        },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                HorizontalDivider(thickness = 1.dp)
            }
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
                    contentDescription = null
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
                    contentDescription = null
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
                    contentDescription = null
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
                    contentDescription = null
                )
            }
            HorizontalDivider(thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable {
                        openCategoryMenu = true
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(24.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_grid_view),
                    contentDescription = "book category"
                )
                Text(
                    text = "Book Category",
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
                    contentDescription = null
                )
            }
            HorizontalDivider(thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable {
                        openSpecialCodeDialog = true
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(24.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_favourite_music),
                    contentDescription = "special code"
                )
                Text(
                    text = "Coupon Code",
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
                    contentDescription = null
                )
            }
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
    if (openCategoryMenu) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
            sheetState = categoryMenuSheetState,
            onDismissRequest = {
                openCategoryMenu = false
                onAction(SettingAction.ResetChipState)
            },
        ) {
            BookCategoryMenu(
                settingState = settingState,
                onAction = onAction
            )
        }
    }
    if (openSpecialCodeDialog) {
        SpecialCodeDialog(
            onSuccess = {
                onAction(SettingAction.OpenSpecialCodeSuccess)
            },
            onDismiss = {
                openSpecialCodeDialog = false
            }
        )
    }
}

