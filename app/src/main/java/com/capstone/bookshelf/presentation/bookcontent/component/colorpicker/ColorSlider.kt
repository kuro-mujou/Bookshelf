package com.capstone.bookshelf.presentation.bookcontent.component.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSlider(
    modifier: Modifier,
    title: String,
    titleColor: Color,
    gradientColorsReversed: List<Color>,
    valueRange:  ClosedFloatingPointRange<Float> = 0f..255f,
    rgb: Float,
    onColorChanged: (Float) -> Unit
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title.substring(0,1), color = titleColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Slider(
            modifier = Modifier.weight(1f),
            value = rgb,
            onValueChange = { onColorChanged(it) },
            valueRange = valueRange,
            onValueChangeFinished = {},
            track = {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .border(2.dp, Color.Gray, RoundedCornerShape(15.dp))
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    drawLine(
                        Brush.linearGradient(gradientColorsReversed),
                        start = Offset(0f + canvasHeight/2, center.y),
                        end = Offset(canvasWidth - canvasHeight/2, center.y),
                        strokeWidth = canvasHeight,
                        cap = StrokeCap.Round
                    )
                }
            },
            thumb = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        )
                )
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = rgb.toInt().toString(),
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.width(30.dp)
        )
    }
}