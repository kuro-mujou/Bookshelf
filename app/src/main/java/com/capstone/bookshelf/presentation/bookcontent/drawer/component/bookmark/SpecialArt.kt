package com.capstone.bookshelf.presentation.bookcontent.drawer.component.bookmark

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@Composable
fun HexagonGridLayoutCanvas(
    modifier: Modifier = Modifier,
    hexRadius: Dp,
    spacing: Dp = 0.dp,
    onDrawCell: DrawScope.(cellCenter: Offset, cellRadius: Float, drawIndex: Int) -> Unit
) {
    if (hexRadius <= 0.dp) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val hexRadiusPx = hexRadius.toPx()
        val spacingPx = spacing.toPx()
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (hexRadiusPx <= 0f) return@Canvas

        val hexHeightPx = 2f * hexRadiusPx
        val hexWidthPx = sqrt(3f) * hexRadiusPx

        val colStepX = hexWidthPx + spacingPx
        val rowStepY = hexHeightPx * 0.75f + spacingPx
        val staggerOffsetX = colStepX / 2f

        val startDrawingRegionX = -hexWidthPx
        val endDrawingRegionX = canvasWidth + hexWidthPx
        val startDrawingRegionY = -hexHeightPx
        val endDrawingRegionY = canvasHeight + hexHeightPx

        val calculationPadding = 2
        val numColsRoughly =
            ceil((endDrawingRegionX - startDrawingRegionX) / colStepX).toInt() + calculationPadding
        val numRowsRoughly =
            ceil((endDrawingRegionY - startDrawingRegionY) / rowStepY).toInt() + calculationPadding

        val originX = 0f
        val originY = 0f

        val visibleHexData = mutableListOf<Triple<Offset, Int, Int>>()

        for (r in 0 until numRowsRoughly) {
            for (c in 0 until numColsRoughly) {
                var currentX = originX + c * colStepX
                val currentY = originY + r * rowStepY
                if (r % 2 != 0) {
                    currentX += staggerOffsetX
                }
                val hexCenter = Offset(currentX, currentY)
                if (hexCenter.x + hexRadiusPx > startDrawingRegionX && hexCenter.x - hexRadiusPx < endDrawingRegionX &&
                    hexCenter.y + hexRadiusPx > startDrawingRegionY && hexCenter.y - hexRadiusPx < endDrawingRegionY
                ) {
                    visibleHexData.add(Triple(hexCenter, r, c))
                }
            }
        }

        if (visibleHexData.isEmpty()) return@Canvas

        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        visibleHexData.forEach { (center, _, _) ->
            minX = min(minX, center.x - hexWidthPx / 2f)
            minY = min(minY, center.y - hexHeightPx / 2f)
            maxX = max(maxX, center.x + hexWidthPx / 2f)
            maxY = max(maxY, center.y + hexHeightPx / 2f)
        }

        val gridActualWidth = maxX - minX
        val gridActualHeight = maxY - minY

        val centeringOffsetX = (canvasWidth - gridActualWidth) / 2f - minX
        val centeringOffsetY = (canvasHeight - gridActualHeight) / 2f - minY

        var drawCallIndex = 0
        visibleHexData.forEach { (unCenteredHexCenter, _, _) ->
            val centeredHexCellCenter =
                unCenteredHexCenter + Offset(centeringOffsetX, centeringOffsetY)
            if (centeredHexCellCenter.x > -hexRadiusPx && centeredHexCellCenter.x < canvasWidth + hexRadiusPx &&
                centeredHexCellCenter.y > -hexRadiusPx && centeredHexCellCenter.y < canvasHeight + hexRadiusPx
            ) {
                this.onDrawCell(centeredHexCellCenter, hexRadiusPx, drawCallIndex)
                drawCallIndex++
            }
        }
    }
}

@Composable
fun SpecialArt(
    baseColor: Color,
    backgroundColor: Color,
) {
    val rotationAngles = remember { listOf(36f, 72f, 108f, 144f, 180f, 216f, 252f, 288f, 324f, 360f) }
    val myShape = remember {
        CustomShape(
            relativeCircleData = listOf(
                Offset(0f, 0f) to 25f,
                Offset(40f, 0f) to 25f
            ),
            relativeRect = CustomRect(
                topLeft = Offset(0f, 10f),
                size = Size(40f, 120f)
            ),
            relativeArc = ArcSpec(
                topLeft = Offset(5f, 115f),
                size = Size(45f, 45f),
                startAngle = 180f,
                sweepAngle = -180f
            ),
        )
    }

    HexagonGridLayoutCanvas(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        hexRadius = 36.dp,
        spacing = 6.dp,
        onDrawCell = { cellCenter, cellRadiusPx, drawIndex ->
            val angleIndex = drawIndex % rotationAngles.size
            val currentRotationAngle = rotationAngles[angleIndex]

            this.rotate(degrees = currentRotationAngle, pivot = cellCenter) {
                myShape.draw(
                    drawScope = this,
                    baseColor = baseColor,
                    targetCanvasCenter = cellCenter,
                )
            }
        }
    )
}

data class CustomRect(val topLeft: Offset, val size: Size)

data class ArcSpec(
    val topLeft: Offset,
    val size: Size,
    val startAngle: Float,
    val sweepAngle: Float
)


data class CustomShape(
    val relativeCircleData: List<Pair<Offset, Float>>,
    val relativeRect: CustomRect,
    val relativeArc: ArcSpec,
) {
    private val bounds: Rect by lazy { calculateBounds() }
    private val localCenter: Offset by lazy { bounds.center }

    private fun calculateBounds(): Rect {
        var overallBounds: Rect? = null
        fun Rect.union(other: Rect): Rect {
            return Rect(
                min(this.left, other.left),
                min(this.top, other.top),
                max(this.right, other.right),
                max(this.bottom, other.bottom)
            )
        }
        fun updateOverallBounds(componentBounds: Rect) {
            overallBounds = overallBounds?.union(componentBounds) ?: componentBounds
        }
        relativeCircleData.forEach { (relCenter, radius) ->
            updateOverallBounds(
                Rect(
                    left = relCenter.x - radius,
                    top = relCenter.y - radius,
                    right = relCenter.x + radius,
                    bottom = relCenter.y + radius
                )
            )
        }
        updateOverallBounds(
            Rect(
                left = relativeRect.topLeft.x,
                top = relativeRect.topLeft.y,
                right = relativeRect.topLeft.x + relativeRect.size.width,
                bottom = relativeRect.topLeft.y + relativeRect.size.height
            )
        )
        val arcEffectiveTopLeft = Offset(
            x = -relativeArc.topLeft.x / 2f,
            y = relativeArc.topLeft.y
        )
        updateOverallBounds(
            Rect(
                left = arcEffectiveTopLeft.x,
                top = arcEffectiveTopLeft.y,
                right = arcEffectiveTopLeft.x + relativeArc.size.width,
                bottom = arcEffectiveTopLeft.y + relativeArc.size.height
            )
        )
        return overallBounds ?: Rect.Zero
    }

    fun draw(
        drawScope: DrawScope,
        baseColor: Color,
        targetCanvasCenter: Offset,
    ) {
        with(drawScope) {
            val drawingOriginOffset = targetCanvasCenter - localCenter
            relativeCircleData.forEach { (relCenter, radius) ->
                drawCircle(
                    color = baseColor,
                    center = relCenter + drawingOriginOffset,
                    radius = radius
                )
            }
            val rectTopLeftOnCanvas = relativeRect.topLeft + drawingOriginOffset
            val rectOnCanvas = Rect(rectTopLeftOnCanvas, relativeRect.size)
            drawRect(
                color = baseColor,
                topLeft = rectOnCanvas.topLeft,
                size = rectOnCanvas.size
            )
            val arcDrawTopLeft = Offset(
                x = drawingOriginOffset.x - relativeArc.topLeft.x / 2f,
                y = drawingOriginOffset.y + relativeArc.topLeft.y
            )
            drawArc(
                color = baseColor,
                topLeft = arcDrawTopLeft,
                size = relativeArc.size,
                startAngle = relativeArc.startAngle,
                sweepAngle = relativeArc.sweepAngle,
                useCenter = true
            )
        }
    }
}
