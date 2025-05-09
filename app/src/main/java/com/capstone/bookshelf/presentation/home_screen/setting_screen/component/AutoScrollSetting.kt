package com.capstone.bookshelf.presentation.home_screen.setting_screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.capstone.bookshelf.presentation.home_screen.setting_screen.SettingAction
import com.capstone.bookshelf.presentation.home_screen.setting_screen.SettingState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoScrollSetting(
    settingState: SettingState,
    onDismissRequest: () -> Unit,
    onAction: (SettingAction) -> Unit,
) {
    Dialog(
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        var speedSliderValue by remember { mutableIntStateOf(settingState.currentScrollSpeed) }
        var delayAtStart by remember { mutableIntStateOf(settingState.delayAtStart) }
        var delayAtEnd by remember { mutableIntStateOf(settingState.delayAtEnd) }
        var delayResumeMode by remember { mutableIntStateOf(settingState.delayResumeMode) }
        Surface(
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
                    text = "Auto Scroll Setting",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                )
                HorizontalDivider(thickness = 2.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Faster")
                    Text(text = "Speed")
                    Text(text = "Slower")
                }
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = speedSliderValue / 10000f,
                    onValueChange = { value ->
                        speedSliderValue = (value * 10000).roundToInt()
                    },
                    onValueChangeFinished = {
                        onAction(SettingAction.UpdateScrollSpeed(speedSliderValue))
                    },
                    valueRange = 0.1f..2f,
                    steps = 18,
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Delay at start")
                    Text(text = "%.2fs".format(delayAtStart / 1000f))
                }
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = delayAtStart / 1000f,
                    onValueChange = { value ->
                        delayAtStart = (value * 1000).roundToInt()
                    },
                    onValueChangeFinished = {
                        onAction(SettingAction.UpdateDelayAtStart(delayAtStart))
                    },
                    valueRange = 1f..10f,
                    steps = 8,
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Delay at end")
                    Text(text = "%.2fs".format(delayAtEnd / 1000f))
                }
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = delayAtEnd / 1000f,
                    onValueChange = { value ->
                        delayAtEnd = (value * 1000).roundToInt()
                    },
                    onValueChangeFinished = {
                        onAction(SettingAction.UpdateDelayAtEnd(delayAtEnd))
                    },
                    valueRange = 1f..10f,
                    steps = 8,
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
                HorizontalDivider(thickness = 1.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Auto Scroll after pause")
                    Switch(
                        checked = settingState.isAutoResumeScrollMode,
                        onCheckedChange = {
                            onAction(SettingAction.UpdateAutoResumeScrollMode(it))
                        },
                    )
                }
                if (settingState.isAutoResumeScrollMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text( text = "Delay time")
                        Text(text = "%.2fs".format(delayResumeMode / 1000f))
                    }
                    Slider(
                        modifier = Modifier.fillMaxWidth(),
                        value = delayResumeMode / 1000f,
                        onValueChange = { value ->
                            delayResumeMode = (value * 1000).roundToInt()
                        },
                        onValueChangeFinished = {
                            onAction(SettingAction.UpdateDelayResumeMode(delayResumeMode))
                        },
                        valueRange = 1f..5f,
                        steps = 3,
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                            )
                        },
                    )
                }
            }
        }
    }
}