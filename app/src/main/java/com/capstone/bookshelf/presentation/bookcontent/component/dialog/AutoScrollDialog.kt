package com.capstone.bookshelf.presentation.bookcontent.component.dialog

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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollAction
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollState
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.util.DataStoreManager
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@UnstableApi
fun AutoScrollMenuDialog(
    contentState: ContentState,
    autoScrollState: AutoScrollState,
    autoScrollViewModel: AutoScrollViewModel,
    colorPaletteState: ColorPalette,
    dataStoreManager: DataStoreManager,
    onDismissRequest: () -> Unit,
){
    Dialog(
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        var speedSliderValue by remember { mutableIntStateOf(autoScrollState.currentSpeed) }
        var delayAtStart by remember { mutableIntStateOf(autoScrollState.delayAtStart) }
        var delayAtEnd by remember { mutableIntStateOf(autoScrollState.delayAtEnd) }
        var delayResumeMode by remember { mutableIntStateOf(autoScrollState.delayResumeMode) }
        val scope = rememberCoroutineScope()
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = colorPaletteState.containerColor,
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
                        color = colorPaletteState.textColor,
                        fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                    )
                )
                HorizontalDivider(thickness = 2.dp)
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
                        text = "%.2fx".format(speedSliderValue/10000f),
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )
                }
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = speedSliderValue/10000f,
                    onValueChange = { value ->
                        speedSliderValue = (value * 10000).roundToInt()
                    },
                    onValueChangeFinished = {
                        autoScrollViewModel.onAction(AutoScrollAction.UpdateAutoScrollSpeed(speedSliderValue))
                        scope.launch {
                            dataStoreManager.setAutoScrollSpeed(speedSliderValue)
                        }
                    },
                    valueRange = 0.5f..1.5f,
                    steps = 9,
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = colorPaletteState.textColor,
                                    shape = CircleShape
                                )
                        )
                    },
                    colors = SliderDefaults.colors(
                        activeTrackColor = colorPaletteState.textColor,
                        activeTickColor = colorPaletteState.containerColor,
                        inactiveTrackColor = colorPaletteState.textColor.copy(alpha = 0.5f),
                        inactiveTickColor = colorPaletteState.containerColor.copy(alpha = 0.5f),
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(
                        text = "Delay at start",
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )
                    Text(
                        text = "%.2fs".format(delayAtStart/1000f),
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )
                }
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = delayAtStart/1000f,
                    onValueChange = { value ->
                        delayAtStart = (value * 1000).roundToInt()
                    },
                    onValueChangeFinished = {
                        autoScrollViewModel.onAction(AutoScrollAction.UpdateDelayAtStart(delayAtStart))
                        scope.launch {
                            dataStoreManager.setDelayTimeAtStart(delayAtStart)
                        }
                    },
                    valueRange = 1f..10f,
                    steps = 8,
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = colorPaletteState.textColor,
                                    shape = CircleShape
                                )
                        )
                    },
                    colors = SliderDefaults.colors(
                        activeTrackColor = colorPaletteState.textColor,
                        activeTickColor = colorPaletteState.containerColor,
                        inactiveTrackColor = colorPaletteState.textColor.copy(alpha = 0.5f),
                        inactiveTickColor = colorPaletteState.containerColor.copy(alpha = 0.5f),
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(
                        text = "Delay at end",
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )
                    Text(
                        text = "%.2fs".format(delayAtEnd/1000f),
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )
                }
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = delayAtEnd/1000f,
                    onValueChange = { value ->
                        delayAtEnd = (value * 1000).roundToInt()
                    },
                    onValueChangeFinished = {
                        autoScrollViewModel.onAction(AutoScrollAction.UpdateDelayAtEnd(delayAtEnd))
                        scope.launch {
                            dataStoreManager.setDelayTimeAtEnd(delayAtEnd)
                        }
                    },
                    valueRange = 1f..10f,
                    steps = 8,
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = colorPaletteState.textColor,
                                    shape = CircleShape
                                )
                        )
                    },
                    colors = SliderDefaults.colors(
                        activeTrackColor = colorPaletteState.textColor,
                        activeTickColor = colorPaletteState.containerColor,
                        inactiveTrackColor = colorPaletteState.textColor.copy(alpha = 0.5f),
                        inactiveTickColor = colorPaletteState.containerColor.copy(alpha = 0.5f),
                    ),
                )
                HorizontalDivider(thickness = 1.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Auto Scroll after pause",
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )
                    Switch(
                        checked = autoScrollState.isAutoResumeScrollMode,
                        onCheckedChange = {
                            autoScrollViewModel.onAction(AutoScrollAction.UpdateAutoResumeScrollMode(it))
                            scope.launch {
                                dataStoreManager.setAutoScrollResumeMode(it)
                            }
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
                if(autoScrollState.isAutoResumeScrollMode){
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Text(
                            text = "Delay time",
                            style = TextStyle(
                                color = colorPaletteState.textColor,
                                fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                            )
                        )
                        Text(
                            text = "%.2fs".format(delayResumeMode/1000f),
                            style = TextStyle(
                                color = colorPaletteState.textColor,
                                fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                            )
                        )
                    }
                    Slider(
                        modifier = Modifier.fillMaxWidth(),
                        value = delayResumeMode/1000f,
                        onValueChange = { value ->
                            delayResumeMode = (value * 1000).roundToInt()
                        },
                        onValueChangeFinished = {
                            autoScrollViewModel.onAction(AutoScrollAction.UpdateDelayResume(delayResumeMode))
                            scope.launch {
                                dataStoreManager.setAutoScrollResumeDelayTime(delayResumeMode)
                            }
                        },
                        valueRange = 1f..5f,
                        steps = 3,
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = colorPaletteState.textColor,
                                        shape = CircleShape
                                    )
                            )
                        },
                        colors = SliderDefaults.colors(
                            activeTrackColor = colorPaletteState.textColor,
                            activeTickColor = colorPaletteState.containerColor,
                            inactiveTrackColor = colorPaletteState.textColor.copy(alpha = 0.5f),
                            inactiveTickColor = colorPaletteState.containerColor.copy(alpha = 0.5f),
                        ),
                    )
                }
            }
        }
    }
}