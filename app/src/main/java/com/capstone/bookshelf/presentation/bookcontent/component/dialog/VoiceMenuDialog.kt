package com.capstone.bookshelf.presentation.bookcontent.component.dialog

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarState
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentAction
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.util.DataStoreManager
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@UnstableApi
fun VoiceMenuDialog(
    viewModel: ContentViewModel,
    contentState: ContentState,
    bottomBarState: BottomBarState,
    colorPaletteState: ColorPalette,
    tts: TextToSpeech,
    dataStoreManager: DataStoreManager,
    onDismiss: () -> Unit,
    testVoiceButtonClicked: () -> Unit
){
    var speedSliderValue by remember { mutableFloatStateOf(contentState.currentSpeed?:1f) }
    var pitchSliderValue by remember { mutableFloatStateOf(contentState.currentPitch?:1f) }
    val locales = tts.availableLanguages?.toList()?.sortedBy { it.displayName }
    val voices = tts.voices
        ?.filter{ !it.isNetworkConnectionRequired }
        ?.sortedBy { it.name }
    var languageMenuExpanded by remember { mutableStateOf(false) }
    var voiceMenuExpanded by remember { mutableStateOf(false) }
    val filteredVoices = voices?.filter { it.locale == contentState.currentLanguage }
    Dialog(
        onDismissRequest = {
            if(contentState.currentVoice == null){
                viewModel.fixNullVoice(dataStoreManager,tts)
            }
            onDismiss()
        }
    ) {
        LaunchedEffect (contentState.currentSpeed){
            speedSliderValue = contentState.currentSpeed?:1f
        }
        LaunchedEffect (contentState.currentPitch){
            pitchSliderValue = contentState.currentPitch?:1f
        }
        Surface(
            color = colorPaletteState.backgroundColor,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    modifier = Modifier
                        .padding(bottom = 4.dp),
                    text = "Voice Setting",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorPaletteState.textColor,
                        fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                    )
                )
                HorizontalDivider(thickness = 2.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    Box(
                        modifier = Modifier
                            .wrapContentHeight()
                            .width(100.dp),
                        contentAlignment = Alignment.CenterStart,
                        content = {
                            Text(
                                text = "Language",
                                style = TextStyle(
                                    color = colorPaletteState.textColor,
                                    fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                                )
                            )
                        }
                    )
                    ExposedDropdownMenuBox(
                        expanded = languageMenuExpanded,
                        onExpandedChange = { languageMenuExpanded = !languageMenuExpanded }
                    ) {
                        OutlinedTextField(
                            shape = RoundedCornerShape(8.dp),
                            value = contentState.currentLanguage?.displayName.toString(),
                            textStyle = TextStyle(
                                color = colorPaletteState.textColor,
                                fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                            ),
                            onValueChange = {

                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageMenuExpanded)
                            },
                            readOnly = true,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable, true),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colorPaletteState.textColor,
                                unfocusedTextColor = colorPaletteState.textColor,
                                focusedTrailingIconColor = colorPaletteState.textColor,
                                unfocusedTrailingIconColor = colorPaletteState.textColor,
                                focusedBorderColor = colorPaletteState.textColor,
                                unfocusedBorderColor = colorPaletteState.textColor,
                            )
                        )
                        ExposedDropdownMenu(
                            modifier = Modifier.height(IntrinsicSize.Min),
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false },
                            containerColor = colorPaletteState.backgroundColor,
                        ) {
                            locales?.forEach { locale ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = locale.displayName,
                                            style = TextStyle(
                                                color = colorPaletteState.textColor,
                                                fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                                            )
                                        )
                                    },
                                    onClick = {
                                        viewModel.onContentAction(dataStoreManager, ContentAction.UpdateTTSLanguage(locale))
                                        languageMenuExpanded = false
                                        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateTTSVoice(null))
                                    },
                                    colors = MenuItemColors(
                                        textColor = colorPaletteState.textColor,
                                        leadingIconColor = colorPaletteState.textColor,
                                        trailingIconColor = colorPaletteState.textColor,
                                        disabledTextColor = colorPaletteState.textColor.copy(alpha = 0.5f),
                                        disabledLeadingIconColor = colorPaletteState.textColor.copy(alpha = 0.5f),
                                        disabledTrailingIconColor = colorPaletteState.textColor.copy(alpha = 0.5f),
                                    )
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    Box(
                        modifier = Modifier
                            .wrapContentHeight()
                            .width(100.dp),
                        contentAlignment = Alignment.CenterStart,
                        content = {
                            Text(
                                text = "Voice",
                                style = TextStyle(
                                    color = colorPaletteState.textColor,
                                    fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                                )
                            )
                        }
                    )
                    ExposedDropdownMenuBox(
                        expanded = voiceMenuExpanded,
                        onExpandedChange = { voiceMenuExpanded = !voiceMenuExpanded }
                    ) {
                        OutlinedTextField(
                            shape = RoundedCornerShape(8.dp),
                            value = contentState.currentVoice?.name.toString(),
                            textStyle = TextStyle(
                                color = colorPaletteState.textColor,
                                fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                            ),
                            onValueChange = {

                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = voiceMenuExpanded)
                            },
                            readOnly = true,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable, true),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colorPaletteState.textColor,
                                unfocusedTextColor = colorPaletteState.textColor,
                                focusedTrailingIconColor = colorPaletteState.textColor,
                                unfocusedTrailingIconColor = colorPaletteState.textColor,
                                focusedBorderColor = colorPaletteState.textColor,
                                unfocusedBorderColor = colorPaletteState.textColor,
                            )
                        )
                        ExposedDropdownMenu(
                            modifier = Modifier.height(IntrinsicSize.Min),
                            expanded = voiceMenuExpanded,
                            onDismissRequest = { voiceMenuExpanded = false },
                            containerColor = colorPaletteState.backgroundColor,
                        ) {
                            filteredVoices?.forEach{voice ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "Quality: " + voice.quality.toString(),
                                                style = TextStyle(
                                                    color = colorPaletteState.textColor,
                                                    fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                                                )
                                            )
                                            Text(
                                                text = voice.name,
                                                style = TextStyle(
                                                    color = colorPaletteState.textColor,
                                                    fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                                                )
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateTTSVoice(voice))
                                        voiceMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(
                        text = "Speed",
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )
                    Text(
                        text = "%.2fx".format(speedSliderValue),
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )

                }
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = speedSliderValue,
                    onValueChange = { value ->
                        speedSliderValue = (value * 100).roundToInt() / 100f
                    },
                    onValueChangeFinished = {
                        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateTTSSpeed(speedSliderValue))
                    },
                    colors = SliderDefaults.colors(
                        activeTrackColor = colorPaletteState.textColor,
                        inactiveTrackColor = colorPaletteState.textColor.copy(alpha = 0.5f)
                    ),
                    valueRange = 0.5f..2.5f,
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = colorPaletteState.textColor,
                                    shape = CircleShape
                                )
                        )
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(
                        text = "Pitch",
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )
                    Text(
                        text = "%.2fx".format(pitchSliderValue),
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )
                }
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = pitchSliderValue,
                    onValueChange = { value ->
                        pitchSliderValue = (value * 100).roundToInt() / 100f
                    },
                    onValueChangeFinished = {
                        viewModel.onContentAction(dataStoreManager,ContentAction.UpdateTTSPitch(pitchSliderValue))
                    },
                    colors = SliderDefaults.colors(
                        activeTrackColor = colorPaletteState.textColor,
                        inactiveTrackColor = colorPaletteState.textColor.copy(alpha = 0.5f)
                    ),
                    valueRange = 0.5f..1.5f,
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = colorPaletteState.textColor,
                                    shape = CircleShape
                                )
                        )
                    }
                )
                if(bottomBarState.openSetting) {
                    OutlinedButton(
                        onClick = {
                            testVoiceButtonClicked()
                        },
                        colors = ButtonColors(
                            containerColor = colorPaletteState.backgroundColor,
                            contentColor = colorPaletteState.textColor,
                            disabledContainerColor = colorPaletteState.backgroundColor,
                            disabledContentColor = colorPaletteState.textColor.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = "Test Voice",
                            style = TextStyle(
                                color = colorPaletteState.textColor,
                                fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                            )
                        )
                    }
                }
            }
        }
    }
}
