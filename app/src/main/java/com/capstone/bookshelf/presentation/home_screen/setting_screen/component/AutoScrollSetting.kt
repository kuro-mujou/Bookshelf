package com.capstone.bookshelf.presentation.home_screen.setting_screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.capstone.bookshelf.presentation.home_screen.setting_screen.SettingState
import com.capstone.bookshelf.util.DataStoreManager
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoScrollSetting(
    settingState: SettingState,
    dataStoreManager: DataStoreManager,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        var speedSliderValue by remember { mutableIntStateOf(0) }
        var delayAtStart by remember { mutableIntStateOf(0) }
        var delayAtEnd by remember { mutableIntStateOf(0) }
        var delayResumeMode by remember { mutableIntStateOf(0) }
        val scope = rememberCoroutineScope()
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
                        scope.launch {
                            dataStoreManager.setAutoScrollSpeed(speedSliderValue)
                        }
                    },
                    valueRange = 0.1f..2f,
                    steps = 18,
//                    thumb = {
//                        Box(
//                            modifier = Modifier
//                                .size(24.dp)
//                        )
//                    },
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
                        scope.launch {
                            dataStoreManager.setDelayTimeAtStart(delayAtStart)
                        }
                    },
                    valueRange = 1f..10f,
                    steps = 8,
//                    thumb = {
//                        Box(
//                            modifier = Modifier
//                                .size(24.dp)
//                                .background(
//                                    color = colorPaletteState.textColor,
//                                    shape = CircleShape
//                                )
//                        )
//                    },
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
                        scope.launch {
                            dataStoreManager.setDelayTimeAtEnd(delayAtEnd)
                        }
                    },
                    valueRange = 1f..10f,
                    steps = 8,
//                    thumb = {
//                        Box(
//                            modifier = Modifier
//                                .size(24.dp)
//                                .background(
//                                    color = colorPaletteState.textColor,
//                                    shape = CircleShape
//                                )
//                        )
//                    }
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
                            scope.launch {
                                dataStoreManager.setAutoScrollResumeMode(it)
                            }
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
                            scope.launch {
                                dataStoreManager.setAutoScrollResumeDelayTime(delayResumeMode)
                            }
                        },
                        valueRange = 1f..5f,
                        steps = 3,
//                        thumb = {
//                            Box(
//                                modifier = Modifier
//                                    .size(24.dp)
//                                    .background(
//                                        color = colorPaletteState.textColor,
//                                        shape = CircleShape
//                                    )
//                            )
//                        },
                    )
                }
            }
        }
    }
}