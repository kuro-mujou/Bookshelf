package com.capstone.bookshelf.presentation.home_screen.setting_screen.component

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.capstone.bookshelf.presentation.home_screen.setting_screen.SettingAction
import com.capstone.bookshelf.presentation.home_screen.setting_screen.SettingState
import com.capstone.bookshelf.util.DataStoreManager
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSetting(
    tts: TextToSpeech?,
    settingState: SettingState,
    dataStoreManager: DataStoreManager,
    onDismiss: () -> Unit,
    testVoiceButtonClicked: () -> Unit,
    onAction: (SettingAction) -> Unit,
) {
    var speedSliderValue by remember { mutableFloatStateOf(settingState.currentSpeed) }
    var pitchSliderValue by remember { mutableFloatStateOf(settingState.currentPitch) }
    val locales = tts?.availableLanguages?.toList()?.sortedBy { it.displayName }
    val voices = tts?.voices
        ?.filter { !it.isNetworkConnectionRequired }
        ?.sortedBy { it.name }
    var languageMenuExpanded by remember { mutableStateOf(false) }
    var voiceMenuExpanded by remember { mutableStateOf(false) }
    val filteredVoices = voices?.filter { it.locale == settingState.currentLanguage }
    Dialog(
        onDismissRequest = {
            onDismiss()
        }
    ) {
        val focusManager = LocalFocusManager.current
        Surface(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        focusManager.clearFocus()
                    }
                ),
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
                    )
                )
                HorizontalDivider(thickness = 2.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentHeight()
                            .width(100.dp),
                        contentAlignment = Alignment.CenterStart,
                        content = {
                            Text(text = "Language")
                        }
                    )
                    ExposedDropdownMenuBox(
                        expanded = languageMenuExpanded,
                        onExpandedChange = { languageMenuExpanded = !languageMenuExpanded }
                    ) {
                        OutlinedTextField(
                            shape = RoundedCornerShape(8.dp),
                            value = settingState.currentLanguage?.displayName.toString(),
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
                        )
                        ExposedDropdownMenu(
                            modifier = Modifier.height(IntrinsicSize.Min),
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false },
                        ) {
                            locales?.forEach { locale ->
                                DropdownMenuItem(
                                    text = {
                                        Text(text = locale.displayName)
                                    },
                                    onClick = {
                                        languageMenuExpanded = false
                                        focusManager.clearFocus()
                                        onAction(SettingAction.UpdateLanguage(locale))
                                        onAction(SettingAction.UpdateVoice(null))
                                    },
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
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentHeight()
                            .width(100.dp),
                        contentAlignment = Alignment.CenterStart,
                        content = {
                            Text(text = "Voice")
                        }
                    )
                    ExposedDropdownMenuBox(
                        expanded = voiceMenuExpanded,
                        onExpandedChange = { voiceMenuExpanded = !voiceMenuExpanded }
                    ) {
                        OutlinedTextField(
                            shape = RoundedCornerShape(8.dp),
                            value = settingState.currentVoice?.name.toString(),
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
                        )
                        ExposedDropdownMenu(
                            modifier = Modifier.height(IntrinsicSize.Min),
                            expanded = voiceMenuExpanded,
                            onDismissRequest = { voiceMenuExpanded = false },
                        ) {
                            filteredVoices?.forEach { voice ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(text = "Quality: " + voice.quality.toString())
                                            Text(text = voice.name)
                                        }
                                    },
                                    onClick = {
                                        voiceMenuExpanded = false
                                        focusManager.clearFocus()
                                        onAction(SettingAction.UpdateVoice(voice))
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
                ) {
                    Text(text = "Speed")
                    Text(text = "%.2fx".format(speedSliderValue))
                }
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = speedSliderValue,
                    onValueChange = { value ->
                        speedSliderValue = (value * 100).roundToInt() / 100f
                    },
                    onValueChangeFinished = {
                        onAction(SettingAction.UpdateSpeed(speedSliderValue))
                    },
                    valueRange = 0.5f..2.5f,
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Pitch")
                    Text(text = "%.2fx".format(pitchSliderValue))
                }
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = pitchSliderValue,
                    onValueChange = { value ->
                        pitchSliderValue = (value * 100).roundToInt() / 100f
                    },
                    onValueChangeFinished = {
                        onAction(SettingAction.UpdatePitch(pitchSliderValue))
                    },
                    valueRange = 0.5f..1.5f,
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                    }
                )
                OutlinedButton(
                    onClick = {
                        testVoiceButtonClicked()
                    },
                ) {
                    Text(text = "Test Voice")
                }
            }
        }
    }
}
