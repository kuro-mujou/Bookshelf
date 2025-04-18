package com.capstone.bookshelf.presentation.bookcontent.drawer.component.bookmark

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.capstone.bookshelf.util.darken
import com.capstone.bookshelf.util.lighten
import com.capstone.bookshelf.util.toHsv
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Helper function to calculate a linear interpolation between two values.
 * **/
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

@Composable
fun CardBackgroundCloudWithBirds(
    modifier: Modifier = Modifier,
    baseColor: Color,
    cardWidth: Int,
    cardHeight: Int
) {
    val cloudData = remember(cardWidth, cardHeight) {
        mutableListOf<Pair<Offset, Float>>().apply {
            fun createDenseClouds(center: Offset, scale: Float) {
                val baseRadius = 25f * scale
                val parts = listOf(
                    Offset(0f, 0f),
                    Offset(-20f, -10f),
                    Offset(20f, -5f),
                    Offset(-10f, -20f),
                    Offset(10f, -18f)
                )
                parts.forEach { partOffset ->
                    val circleCenter = center + partOffset * scale
                    val circleRadius = baseRadius * (0.8f + (0.4f * Random.nextFloat()))
                    add(Pair(circleCenter, circleRadius))
                }
            }

            var x = -40f
            while (x < cardWidth + 40f) {
                val scale = 0.7f + Random.nextFloat() * 0.5f
                val cloudCenter = Offset(x, cardHeight.toFloat())
                createDenseClouds(cloudCenter, scale)
                x += 20f + Random.nextFloat() * 50f
            }
        }
    }

    val birdPaths = remember(cardWidth, cardHeight) {
        mutableListOf<Pair<Path, Stroke>>().apply {
            fun createBirdPaths(): List<Pair<Path, Stroke>> {
                val birdPaths = mutableListOf<Pair<Path, Stroke>>()
                val birdStroke = Stroke(width = 2f, cap = StrokeCap.Round)
                val centerX = Random.nextFloat() * cardWidth
                val centerY = cardHeight * (0.2f + Random.nextFloat() * 0.3f)
                val wingSpan = 25f + Random.nextFloat() * 30f

                val leftWing = Path().apply {
                    moveTo(centerX, centerY)
                    quadraticTo(
                        centerX - wingSpan / 2, centerY - wingSpan / 2,
                        centerX - wingSpan, centerY
                    )
                }
                val rightWing = Path().apply {
                    moveTo(centerX, centerY)
                    quadraticTo(
                        centerX + wingSpan / 2, centerY - wingSpan / 2,
                        centerX + wingSpan, centerY
                    )
                }
                birdPaths.add(Pair(leftWing, birdStroke))
                birdPaths.add(Pair(rightWing, birdStroke))
                return birdPaths
            }

            val birdCount = 2 + Random.nextInt(2)
            repeat(birdCount) {
                createBirdPaths().forEach { (path, stroke) ->
                    add(Pair(path, stroke))
                }
            }
        }
    }
    Box(
        modifier = modifier
            .drawWithCache {
                onDrawBehind {
                    cloudData.forEach { (center, radius) ->
                        drawCircle(
                            color = baseColor,
                            center = center,
                            radius = radius
                        )
                    }
                    birdPaths.forEach { (path, stroke) ->
                        drawPath(
                            path = path,
                            color = baseColor.copy(alpha = 0.8f),
                            style = stroke
                        )
                    }
                }
            }
    )
}

@Composable
fun CardBackgroundWaveWithBirds(
    modifier: Modifier = Modifier,
    baseColor: Color,
    cardWidth: Int,
    cardHeight: Int
) {
    val wavePaths = remember(cardWidth, cardHeight) {
        fun randomInRange(range: ClosedFloatingPointRange<Float>): Float {
            return range.start + Random.nextFloat() * (range.endInclusive - range.start)
        }

        fun createWavePath(
            width: Float,
            height: Float,
            offsetY: Float,
            amplitudeRange: ClosedFloatingPointRange<Float>,
            segmentWidthRange: ClosedFloatingPointRange<Float>
        ): Path {
            val path = Path()
            val baseY = height - offsetY
            path.moveTo(0f, height)
            path.lineTo(0f, baseY)

            var currentX = 0f
            while (currentX < width + 100f) {
                val segWidth = randomInRange(segmentWidthRange)
                val controlY = baseY + randomInRange(amplitudeRange)
                val endY = baseY + randomInRange(amplitudeRange)
                val nextX = currentX + segWidth
                val controlX = currentX + segWidth / 2f
                path.quadraticTo(
                    controlX,
                    controlY,
                    nextX,
                    endY
                )
                currentX = nextX
            }

            path.lineTo(width, baseY)
            path.lineTo(width, height)
            path.close()
            return path
        }

        listOf(
            createWavePath(cardWidth.toFloat(), cardHeight.toFloat(), 30f, -10f..10f, 30f..60f),
            createWavePath(cardWidth.toFloat(), cardHeight.toFloat(), 50f, -12f..12f, 40f..70f),
            createWavePath(cardWidth.toFloat(), cardHeight.toFloat(), 70f, -15f..15f, 50f..80f)
        )
    }

    val birdPaths = remember(cardWidth, cardHeight) {
        fun createBirdPaths(): List<Pair<Path, Stroke>> {
            val birdPaths = mutableListOf<Pair<Path, Stroke>>()
            val birdStroke = Stroke(width = 2f, cap = StrokeCap.Round)
            val birdCount = 2 + Random.nextInt(2)
            repeat(birdCount) {
                val centerX = Random.nextFloat() * cardWidth
                val centerY = cardHeight * (0.2f + Random.nextFloat() * 0.3f)
                val wingSpan = 25f + Random.nextFloat() * 30f

                val leftWing = Path().apply {
                    moveTo(centerX, centerY)
                    quadraticTo(
                        centerX - wingSpan / 2, centerY - wingSpan / 2,
                        centerX - wingSpan, centerY
                    )
                }
                val rightWing = Path().apply {
                    moveTo(centerX, centerY)
                    quadraticTo(
                        centerX + wingSpan / 2, centerY - wingSpan / 2,
                        centerX + wingSpan, centerY
                    )
                }
                birdPaths.add(Pair(leftWing, birdStroke))
                birdPaths.add(Pair(rightWing, birdStroke))
            }
            return birdPaths
        }

        createBirdPaths()
    }

    Box(
        modifier = modifier
            .drawWithCache {
                onDrawBehind {
                    drawPath(wavePaths[0], color = baseColor)
                    drawPath(wavePaths[1], color = baseColor.copy(alpha = 0.6f))
                    drawPath(wavePaths[2], color = baseColor.copy(alpha = 0.3f))

                    birdPaths.forEach { (path, stroke) ->
                        drawPath(
                            path = path,
                            color = baseColor.copy(alpha = 0.8f),
                            style = stroke
                        )
                    }
                }
            }
    )
}

@Composable
fun CardBackgroundStarryNight(
    modifier: Modifier = Modifier,
    baseColor: Color,
    cardWidth: Int,
    cardHeight: Int
) {
    data class Star(val position: Offset, val radius: Float, val alpha: Float)
    data class ConcaveStar(val position: Offset, val size: Float, val alpha: Float)

    val stars = remember(cardWidth, cardHeight) {
        List(100) {
            Star(
                position = Offset(
                    Random.nextFloat() * cardWidth,
                    Random.nextFloat() * cardHeight
                ),
                radius = 1.5f + Random.nextFloat() * 2f,
                alpha = 0.4f + Random.nextFloat() * 0.6f
            )
        }
    }

    val bigStars = remember(cardWidth, cardHeight) {
        val bigStarCount = 4
        List(bigStarCount) {
            ConcaveStar(
                position = Offset(
                    Random.nextFloat() * cardWidth,
                    Random.nextFloat() * cardHeight * 0.7f
                ),
                size = 5f + Random.nextFloat() * 25f,
                alpha = 0.3f + Random.nextFloat() * 0.5f
            )
        }
    }

    Box(
        modifier = modifier
            .drawWithCache {
                onDrawBehind {
                    stars.forEach { (pos, radius, alpha) ->
                        drawCircle(
                            color = baseColor.copy(alpha = alpha),
                            radius = radius,
                            center = pos
                        )
                    }

                    bigStars.forEach { (center, size, alpha) ->
                        val path = Path().apply {
                            moveTo(center.x, center.y - size)
                            quadraticTo(
                                center.x + size * 0.1f,
                                center.y - size * 0.1f,
                                center.x + size,
                                center.y
                            )
                            quadraticTo(
                                center.x + size * 0.1f,
                                center.y + size * 0.1f,
                                center.x,
                                center.y + size
                            )
                            quadraticTo(
                                center.x - size * 0.1f,
                                center.y + size * 0.1f,
                                center.x - size,
                                center.y
                            )
                            quadraticTo(
                                center.x - size * 0.1f,
                                center.y - size * 0.1f,
                                center.x,
                                center.y - size
                            )
                            close()
                        }
                        drawPath(
                            path = path,
                            color = baseColor.copy(alpha = alpha),
                            style = Fill
                        )
                    }
                }
            }
    )
}

@Composable
fun CardBackgroundGeometricTriangle(
    modifier: Modifier = Modifier,
    baseColor: Color,
    cardWidth: Int,
    cardHeight: Int
) {
    data class Triangle(val path: Path, val color: Color)

    val triangles = remember(cardWidth, cardHeight) {
        List(150) {
            val width = cardWidth.toFloat()
            val height = cardHeight.toFloat()
            val triangleSize = lerp(width * 0.05f, width * 0.18f, Random.nextFloat())
            val center = Offset(
                Random.nextFloat() * width * 1.2f - width * 0.1f,
                Random.nextFloat() * height * 1.2f - height * 0.1f
            )
            val angleOffset = Random.nextFloat() * 360f

            val p1 = Offset(
                center.x + cos(Math.toRadians(angleOffset.toDouble())).toFloat() * triangleSize * 0.7f,
                center.y + sin(Math.toRadians(angleOffset.toDouble())).toFloat() * triangleSize * 0.7f
            )
            val p2 = Offset(
                center.x + cos(Math.toRadians(angleOffset + 50.0 + Random.nextFloat() * 20f)).toFloat() * triangleSize,
                center.y + sin(Math.toRadians(angleOffset + 110.0 + Random.nextFloat() * 20f)).toFloat() * triangleSize
            )
            val p3 = Offset(
                center.x + cos(Math.toRadians(angleOffset + 230.0 + Random.nextFloat() * 20f)).toFloat() * triangleSize * 0.8f,
                center.y + sin(Math.toRadians(angleOffset + 230.0 + Random.nextFloat() * 20f)).toFloat() * triangleSize * 0.8f
            )

            val path = Path().apply {
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                close()
            }

            val alphaMod = (Random.nextFloat() - 0.5f) * 2f * 0.4f
            val lightnessMod = (Random.nextFloat() - 0.5f) * 2f * 0.15f
            val color = baseColor
                .copy(alpha = (baseColor.alpha * (1f - abs(alphaMod))).coerceIn(0.1f, 1f))
                .let { if (lightnessMod > 0) it.lighten(lightnessMod) else it.darken(-lightnessMod) }

            Triangle(path, color)
        }
    }

    Box(
        modifier = modifier
            .drawWithCache {
                onDrawBehind {
                    triangles.forEach { triangle ->
                        drawPath(
                            path = triangle.path,
                            color = triangle.color,
                            style = Fill
                        )
                    }
                }
            }
    )
}

@Composable
fun CardBackgroundScatteredHexagons(
    modifier: Modifier = Modifier,
    baseColor: Color,
    cardWidth: Int,
    cardHeight: Int,
    hexagonCount: Int = 25,
    minRadius: Dp = 8.dp,
    maxRadius: Dp = 16.dp,
) {
    data class ScatteredHexagonInfo(
        val center: Offset,
        val radiusPx: Float,
        val rotation: Float,
        val color: Color
    )

    val density = LocalDensity.current
    fun generateUnitHexagonPath(radiusPx: Float): Path {
        val path = Path()
        for (i in 0..5) {
            val angleDeg = 60f * i - 30f
            val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
            val x = radiusPx * cos(angleRad)
            val y = radiusPx * sin(angleRad)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }

    val hexagons = remember(cardWidth, cardHeight) {
        val minRadiusPx = with(density) { minRadius.toPx() }
        val maxRadiusPx = with(density) { maxRadius.toPx() }
        List(hexagonCount) {
            val radius = lerp(minRadiusPx, maxRadiusPx, Random.nextFloat())
            val center = Offset(
                x = Random.nextFloat() * cardWidth,
                y = Random.nextFloat() * cardHeight
            )
            val rotation = Random.nextFloat() * 360f
            val (baseHue, baseSat, baseVal) = baseColor.toHsv()
            val lightnessAdjust = (Random.nextFloat() - 0.5f) * 2f * 0.15f
            val saturationAdjust = (Random.nextFloat() - 0.5f) * 2f * 0.10f
            val alphaAdjust = (Random.nextFloat() - 0.5f) * 2f * 1f
            val randomSat = (baseSat + saturationAdjust).coerceIn(0f, 1f)
            val randomVal = (baseVal + lightnessAdjust).coerceIn(0f, 1f)
            val randomAlpha = (baseColor.alpha * (1f - abs(alphaAdjust))).coerceIn(0.5f, 1f)
            val randomOutlineColor = Color.hsv(baseHue, randomSat, randomVal, randomAlpha)
            ScatteredHexagonInfo(center, radius, rotation, randomOutlineColor)
        }
    }

    Box(
        modifier = modifier
            .drawWithCache {
                val strokeStyle = Stroke(
                    width = 1.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
                onDrawBehind {
                    hexagons.forEach { hex ->
                        translate(left = hex.center.x, top = hex.center.y) {
                            rotate(degrees = hex.rotation, pivot = Offset.Zero) {
                                drawPath(
                                    path = generateUnitHexagonPath(hex.radiusPx),
                                    color = hex.color,
                                    style = strokeStyle
                                )
                            }
                        }
                    }
                }
            }
    )
}

@Composable
fun CardBackgroundPolygonalHexagon(
    modifier: Modifier = Modifier,
    baseColor: Color,
    cardWidth: Int,
    cardHeight: Int
) {
    data class PolygonHexagonInfo(val path: Path, val color: Color)

    val density = LocalDensity.current
    val hexagonsToDraw = remember(cardWidth, cardHeight) {
        val hexagons = mutableListOf<PolygonHexagonInfo>()
        val radiusPx = with(density) { 20.dp.toPx() }
        val hexWidth = sqrt(3.0f) * radiusPx
        val (baseHue, baseSat, baseVal) = baseColor.toHsv()
        val vertDist = 2 * radiusPx * 3 / 4
        val patternRepeatHeight = vertDist * 2f
        val randomOffsetX = Random.nextFloat() * hexWidth
        val randomOffsetY = Random.nextFloat() * patternRepeatHeight
        val startY = -patternRepeatHeight - radiusPx
        val startX = -hexWidth - (hexWidth / 2f)
        val endY = cardHeight + patternRepeatHeight + radiusPx
        val endX = cardWidth + hexWidth + (hexWidth / 2f)

        var y = startY
        while (y < endY) {
            val effectiveRow = floor((y + patternRepeatHeight + radiusPx) / vertDist).toInt()
            val xOffset = if (effectiveRow % 2 != 0) hexWidth / 2f else 0f
            var x = startX
            while (x < endX) {
                val center = Offset(x + xOffset, y)
                val path = Path()
                for (i in 0..5) {
                    val angleDeg = 60f * i - 30f
                    val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
                    val vertexX = center.x + radiusPx * cos(angleRad)
                    val vertexY = center.y + radiusPx * sin(angleRad)
                    if (i == 0) path.moveTo(vertexX, vertexY) else path.lineTo(vertexX, vertexY)
                }
                path.close()

                val lightnessAdjust = (Random.nextFloat() - 0.5f) * 2f * 0.15f
                val saturationAdjust = (Random.nextFloat() - 0.5f) * 2f * 0.10f
                val alphaAdjust = (Random.nextFloat() - 0.5f) * 2f * 1f
                val randomSat = (baseSat + saturationAdjust).coerceIn(0f, 1f)
                val randomVal = (baseVal + lightnessAdjust).coerceIn(0f, 1f)
                val randomAlpha = (baseColor.alpha * (1f - abs(alphaAdjust))).coerceIn(0.1f, 1f)
                val randomOutlineColor = Color.hsv(baseHue, randomSat, randomVal, randomAlpha)

                hexagons.add(PolygonHexagonInfo(path, randomOutlineColor))
                x += hexWidth
            }
            y += vertDist
        }

        Pair(hexagons, Offset(randomOffsetX, randomOffsetY))
    }

    Box(
        modifier = modifier
            .drawWithCache {
                val (hexagons, randomOffset) = hexagonsToDraw
                val strokeStyle =
                    Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                onDrawBehind {
                    translate(left = -randomOffset.x, top = -randomOffset.y) {
                        hexagons.forEach { hex ->
                            drawPath(
                                path = hex.path,
                                color = hex.color,
                                style = strokeStyle
                            )
                        }
                    }
                }
            }
    )
}

@Composable
fun CardBackgroundCherryBlossomRain(
    modifier: Modifier = Modifier,
    baseColor: Color,
    cardWidth: Int,
    cardHeight: Int
) {
    data class PetalInfo(
        val center: Offset,
        val size: Float,
        val rotation: Float,
        val color: Color
    )

    val petalsInfo = remember(cardWidth, cardHeight) {
        val petalColor = baseColor.copy(alpha = 0.8f)
        val baseAlpha = petalColor.alpha

        val unitPetalPath = Path().apply {
            val nominalHeight = 1.0f
            val nominalWidth = 1.0f
            val baseY = nominalHeight / 2f
            val tipY = -nominalHeight / 2f
            val notchDepth = nominalHeight * 0.15f
            val notchCenterY = tipY + notchDepth
            val tipWidth = nominalWidth * 0.35f
            val maxWidth = nominalWidth
            val leftTipX = -tipWidth / 2f
            val rightTipX = tipWidth / 2f
            val controlPointHorizontalOffset = maxWidth * 0.55f
            val controlPointVerticalOffsetNearTip = nominalHeight * 0.3f
            val controlPointVerticalOffsetNearBase = nominalHeight * 0.4f
            moveTo(0f, baseY)
            cubicTo(
                -controlPointHorizontalOffset,
                baseY - controlPointVerticalOffsetNearBase,
                leftTipX - controlPointHorizontalOffset * 0.1f,
                tipY + controlPointVerticalOffsetNearTip,
                leftTipX,
                tipY
            )
            lineTo(0f, notchCenterY)
            lineTo(rightTipX, tipY)
            cubicTo(
                rightTipX + controlPointHorizontalOffset * 0.1f,
                tipY + controlPointVerticalOffsetNearTip,
                controlPointHorizontalOffset,
                baseY - controlPointVerticalOffsetNearBase,
                0f,
                baseY
            )
            close()
        }

        val petals = List(50) {
            val petalSize = lerp(
                cardWidth * 0.025f,
                cardWidth * 0.055f,
                Random.nextFloat()
            )
            val center = Offset(
                x = Random.nextFloat() * cardWidth,
                y = Random.nextFloat() * cardHeight
            )
            val rotation = Random.nextFloat() * 360f
            val currentAlpha = (baseAlpha * (1.0f - Random.nextFloat() * 0.3f))
                .coerceIn(0.1f, 1f)
            PetalInfo(
                center = center,
                size = petalSize,
                rotation = rotation,
                color = petalColor.copy(alpha = currentAlpha)
            )
        }

        Pair(unitPetalPath, petals)
    }

    Box(
        modifier = modifier
            .drawWithCache {
                val (unitPetalPath, petals) = petalsInfo
                onDrawBehind {
                    petals.forEach { petal ->
                        translate(left = petal.center.x, top = petal.center.y) {
                            rotate(degrees = petal.rotation, pivot = Offset.Zero) {
                                scale(scale = petal.size, pivot = Offset.Zero) {
                                    drawPath(
                                        path = unitPetalPath,
                                        color = petal.color,
                                        style = Fill
                                    )
                                }
                            }
                        }
                    }
                }
            }
    )
}