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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollState
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoScrollMenuDialog(
    autoScrollState: AutoScrollState,
    colorPaletteState: ColorPalette,
    onDismissRequest: () -> Unit,
){
    Dialog(
        onDismissRequest = {
//            viewModel.changeMenuTriggerAutoScroll(false)
            onDismissRequest()
        }
    ) {
        var speedSliderValue by remember { mutableFloatStateOf(autoScrollState.currentSpeed) }
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
                        color = colorPaletteState.textColor
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
                            color = colorPaletteState.textColor
                        )
                    )
                    Text(
                        text = "%.2fx".format(speedSliderValue),
                        style = TextStyle(
                            color = colorPaletteState.textColor
                        )
                    )
                }
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = speedSliderValue,
                    onValueChange = { value ->
                        speedSliderValue = (value * 100).roundToInt() / 100f
                    },
                    onValueChangeFinished ={
//                        viewModel.updateCurrentSpeed(speedSliderValue)
                    },
                    valueRange = 0.5f..2.5f,
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
                    }
                )
            }
        }
    }
}