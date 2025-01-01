package com.capstone.bookshelf.presentation.bookcontent.component.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun ColorPickerWheel(
    modifier: Modifier = Modifier,
    selectionRadius: Dp = (-1).dp,
    hue : Int,
    onChange: (Int) -> Unit,
) {
    val gradientColors = listOf(
        Color.Red,
        Color.Magenta,
        Color.Blue,
        Color.Cyan,
        Color.Green,
        Color.Yellow,
        Color.Red
    )
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current.density
        var isTouched by remember { mutableStateOf(false) }
        var angle by remember { mutableIntStateOf(hue) }
        var center by remember { mutableStateOf(Offset.Zero) }
        var radiusOuter by remember { mutableFloatStateOf(0f) }
        var radiusInner by remember { mutableFloatStateOf(0f) }
        val selectorRadius =
            if (selectionRadius > 0.dp) selectionRadius.value * density else radiusInner * 2 * .04f
        val colorPickerModifier = modifier
            .clipToBounds()
            .pointerMotionEvents(
                onDown = {
                    val position = it.position
                    val distance = calculateDistanceFromCenter(center, position)
                    isTouched = (distance in radiusInner..radiusOuter)
                    if (isTouched) {
                        angle = calculateAngleFomLocalCoordinates(center, position)
                        onChange(angle)
                    }
                },
                onMove = {
                    if (isTouched) {
                        val position = it.position
                        angle = calculateAngleFomLocalCoordinates(center, position)
                        onChange(angle)
                    }
                },
                onUp = {
                    isTouched = false
                },
                delayAfterDownInMillis = 20
            )
        Canvas(modifier = colorPickerModifier) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val cX = canvasWidth / 2
            val cY = canvasHeight / 2
            val canvasRadius = canvasWidth.coerceAtMost(canvasHeight) / 2f
            center = Offset(cX, cY)
            radiusOuter = canvasRadius * .9f
            radiusInner = canvasRadius * .65f
            val strokeWidth = (radiusOuter - radiusInner)
            drawCircle(
                brush = Brush.sweepGradient(colors = gradientColors, center = center),
                radius = radiusInner + strokeWidth / 2,

                style = Stroke(
                    width = strokeWidth
                )
            )
            drawCircle(Color.Black, radiusInner - 7f, style = Stroke(width = 14f))
            drawCircle(Color.Black, radiusOuter + 7f, style = Stroke(width = 14f))
            withTransform(
                {
                    rotate(degrees = -hue.toFloat())
                }
            ) {
                drawCircle(
                    Color.White,
                    radius = selectorRadius,
                    center = Offset(center.x + radiusInner + strokeWidth / 2f, center.y),
                    style = Stroke(width = selectorRadius / 2)
                )
            }
        }
    }
}
private fun calculateDistanceFromCenter(center: Offset, position: Offset): Float {
    val dy = center.y - position.y
    val dx = position.x - center.x
    return sqrt(dx * dx + dy * dy)
}
private fun calculateAngleFomLocalCoordinates(center: Offset, position: Offset): Int {
    if (center == Offset.Unspecified || position == Offset.Unspecified) return -1
    val dy = center.y - position.y
    val dx = position.x - center.x
    return ((360 + ((atan2(dy, dx) * 180 / PI))) % 360).roundToInt()
}
private fun calculateAngleFromRootCoordinates(center: Offset, position: Offset): Int {
    if (center == Offset.Unspecified || position == Offset.Unspecified) return -1
    val dy = (position.y - center.y)
    val dx = (center.x - position.x)
    return ((360 + ((atan2(dy, dx) * 180 / PI))) % 360).roundToInt()
}