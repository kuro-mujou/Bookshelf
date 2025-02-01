package com.capstone.bookshelf.presentation.bookcontent.component.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun SaturationRhombus(
    modifier: Modifier = Modifier,
    hue: Float,
    saturation: Float = 0.5f,
    lightness: Float = 0.5f,
    selectionRadius: Dp = (-1).dp,
    onChange: (Float, Float) -> Unit
) {
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current.density
        val length = maxWidth.value * density
        val colorPoints: MutableList<ColorPoint> = remember {
            getPointsInRhombus(length)
        }
        val selectorRadius =
            if (selectionRadius > 0.dp) selectionRadius.value * density else length * .04f
        var currentPosition by remember(saturation, lightness) {
            mutableStateOf(
                setSelectorPositionFromColorParams(saturation, lightness, length)
            )
        }
        var isTouched by remember { mutableStateOf(false) }
        val canvasModifier = Modifier
            .size(maxWidth)
            .pointerMotionEvents(
                onDown = {
                    val position = it.position
                    val posX = position.x
                    val posY = position.y
                    val range = getBoundsInLength(length, posY)
                    val start = range.start - selectorRadius
                    val end = range.endInclusive + selectorRadius
                    isTouched = posX in start..end
                    if (isTouched) {
                        val posXInPercent = (posX / length).coerceIn(0f, 1f)
                        val posYInPercent = (posY / length).coerceIn(0f, 1f)
                        onChange(posXInPercent, 1 - posYInPercent)
                        currentPosition = Offset(posX, posY)
                    }
                    it.consume()
                },
                onMove = {
                    if (isTouched) {
                        val position = it.position
                        val posX = position.x.coerceIn(0f, length)
                        val posY = position.y.coerceIn(0f, length)
                        val range = getBoundsInLength(length, posY)
                        val posXInPercent = (posX / length).coerceIn(0f, 1f)
                        val posYInPercent = (posY / length).coerceIn(0f, 1f)
                        onChange(posXInPercent, 1 - posYInPercent)
                        currentPosition = Offset(posX.coerceIn(range), posY)
                    }
                    it.consume()
                },
                onUp = {
                    isTouched = false
                    it.consume()
                }
            )
        Canvas(modifier = canvasModifier) {
            colorPoints.forEach { colorPoint: ColorPoint ->
                drawCircle(
                    Color.hsl(hue, colorPoint.saturation, colorPoint.lightness),
                    center = colorPoint.point,
                    radius = 5f
                )
            }
            drawCircle(
                Color.White,
                radius = selectorRadius,
                center = currentPosition,
                style = Stroke(width = selectorRadius / 2)
            )
        }
    }
}
private fun setSelectorPositionFromColorParams(
    saturation: Float,
    lightness: Float,
    length: Float
): Offset {
    val range = getBoundsInLength(length, lightness * length)
    val verticalPositionOnRhombus = (1 - lightness) * length
    val horizontalPositionOnRhombus = (saturation * length).coerceIn(range)
    return Offset(horizontalPositionOnRhombus, verticalPositionOnRhombus)
}
fun getBoundsInLength(
    length: Float,
    position: Float
): ClosedFloatingPointRange<Float> {
    val center = length / 2
    return if (position <= center) {
        (center - position)..(center + position)
    } else {
        val heightAfterCenter = length - position
        (center - heightAfterCenter)..(center + heightAfterCenter)
    }
}
fun getIntRangeInLength(
    length: Float,
    position: Float
): IntRange {
    val center = length / 2
    return if (position <= center) {
        (center - position).roundToInt()..(center + position).roundToInt()
    } else {
        val heightAfterCenter = length - position
        (center - heightAfterCenter).roundToInt()..(center + heightAfterCenter).roundToInt()
    }
}
fun getPointsInRhombus(length: Float): MutableList<ColorPoint> {
    val step = length.toInt() / 100
    val colorPints = mutableListOf<ColorPoint>()
    for (yPos in 0..length.toInt() step step) {
        val range = getIntRangeInLength(length = length, yPos.toFloat())
        for (xPos in range step step) {
            val saturation = xPos / length
            val lightness = 1 - (yPos / length)
            val colorPoint =
                ColorPoint(Offset(xPos.toFloat(), yPos.toFloat()), saturation, lightness)
            colorPints.add(colorPoint)
        }
    }
    return colorPints
}
data class ColorPoint(val point: Offset, val saturation: Float, val lightness: Float)