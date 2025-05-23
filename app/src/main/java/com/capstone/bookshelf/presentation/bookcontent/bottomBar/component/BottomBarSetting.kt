package com.capstone.bookshelf.presentation.bookcontent.bottomBar.component

import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarAction
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarState
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollState
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.component.dialog.AutoScrollMenuDialog
import com.capstone.bookshelf.presentation.bookcontent.component.dialog.VoiceMenuDialog
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

@Composable
@UnstableApi
fun BottomBarSetting(
    viewModel: ContentViewModel,
    bottomBarViewModel: BottomBarViewModel,
    autoScrollViewModel: AutoScrollViewModel,
    contentState: ContentState,
    bottomBarState: BottomBarState,
    colorPaletteState: ColorPalette,
    hazeState: HazeState,
    autoScrollState: AutoScrollState,
    style: HazeStyle,
    dataStoreManager: DataStoreManager,
    tts: TextToSpeech,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onEnableSpecialArtChange: (Boolean) -> Unit,
    onBackgroundMusicSetting: () -> Unit,
    onBookmarkThemeSetting: () -> Unit,
) {
    if (bottomBarState.openTTSVoiceMenu) {
        VoiceMenuDialog(
            viewModel = viewModel,
            bottomBarState = bottomBarState,
            contentState = contentState,
            colorPaletteState = colorPaletteState,
            tts = tts,
            onDismiss = {
                bottomBarViewModel.onAction(BottomBarAction.OpenSetting(false))
                bottomBarViewModel.onAction(BottomBarAction.OpenVoiceMenuSetting(false))
            },
            testVoiceButtonClicked = {
                tts.language = contentState.currentLanguage
                tts.voice = contentState.currentVoice
                contentState.currentPitch?.let { tts.setPitch(it) }
                contentState.currentSpeed?.let { tts.setSpeechRate(it) }
                tts.speak("xin chào", TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
            }
        )
    }
    if (bottomBarState.openAutoScrollMenu) {
        AutoScrollMenuDialog(
            contentState = contentState,
            autoScrollState = autoScrollState,
            autoScrollViewModel = autoScrollViewModel,
            colorPaletteState = colorPaletteState,
            dataStoreManager = dataStoreManager,
            onDismissRequest = {
                bottomBarViewModel.onAction(BottomBarAction.OpenAutoScrollMenu(false))
            }
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.hazeEffect(
                        state = hazeState,
                        style = style
                    )
                } else {
                    Modifier.background(colorPaletteState.containerColor)
                }
            ),
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .wrapContentHeight(),
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
                    color = colorPaletteState.textColor,
                    fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                )
            )
            HorizontalDivider(thickness = 2.dp, color = colorPaletteState.textColor.copy(0.8f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable {
                        onKeepScreenOnChange(!contentState.keepScreenOn)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(24.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_keep_screen_on),
                    tint = colorPaletteState.textColor,
                    contentDescription = "Keep Screen On"
                )
                Text(
                    text = "Keep Screen On",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = colorPaletteState.textColor,
                        fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = contentState.keepScreenOn,
                    onCheckedChange = {
                        onKeepScreenOnChange(it)
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
            HorizontalDivider(thickness = 1.dp, color = colorPaletteState.textColor.copy(0.8f))
            if(contentState.unlockSpecialCodeStatus) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            onEnableSpecialArtChange(!contentState.enableSpecialArt)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp)
                            .size(24.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_favourite_music),
                        tint = colorPaletteState.textColor,
                        contentDescription = "enable special art"
                    )
                    Text(
                        text = "Enable special Art",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = contentState.enableSpecialArt,
                        onCheckedChange = {
                            onEnableSpecialArtChange(it)
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
                HorizontalDivider(thickness = 1.dp, color = colorPaletteState.textColor.copy(0.8f))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable {
                        onBackgroundMusicSetting()
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(24.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_music_background),
                    tint = colorPaletteState.textColor,
                    contentDescription = "background music"
                )
                Text(
                    text = "Background Music",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = colorPaletteState.textColor,
                        fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(30.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_right),
                    tint = colorPaletteState.textColor,
                    contentDescription = "background music"
                )
            }
            HorizontalDivider(thickness = 1.dp, color = colorPaletteState.textColor.copy(0.8f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable {
                        onBookmarkThemeSetting()
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(24.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_tag),
                    tint = colorPaletteState.textColor,
                    contentDescription = "Bookmark theme"
                )
                Text(
                    text = "Bookmark theme",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = colorPaletteState.textColor,
                        fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(30.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_right),
                    tint = colorPaletteState.textColor,
                    contentDescription = "Bookmark theme"
                )
            }
            if (contentState.book?.fileType != "cbz" && contentState.book?.fileType != "pdf/images") {
                HorizontalDivider(thickness = 1.dp, color = colorPaletteState.textColor.copy(0.8f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable {
                            bottomBarViewModel.onAction(BottomBarAction.OpenSetting(true))
                            viewModel.loadTTSSetting(tts)
                            bottomBarViewModel.onAction(BottomBarAction.OpenVoiceMenuSetting(true))
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp)
                            .size(24.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_headphones),
                        tint = colorPaletteState.textColor,
                        contentDescription = "text to speech"
                    )
                    Text(
                        text = "Text to Speech",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp)
                            .size(30.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_right),
                        tint = colorPaletteState.textColor,
                        contentDescription = "text to speech"
                    )
                }
            }
            HorizontalDivider(thickness = 1.dp, color = colorPaletteState.textColor.copy(0.8f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable {
                        bottomBarViewModel.onAction(BottomBarAction.OpenAutoScrollMenu(true))
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(24.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_scroll),
                    tint = colorPaletteState.textColor,
                    contentDescription = "auto scroll up"
                )
                Text(
                    text = "Auto Scroll Up",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = colorPaletteState.textColor,
                        fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(30.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_right),
                    tint = colorPaletteState.textColor,
                    contentDescription = "auto scroll up"
                )
            }
        }
    }
}