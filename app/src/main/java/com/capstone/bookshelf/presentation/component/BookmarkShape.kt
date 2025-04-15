package com.capstone.bookshelf.presentation.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class BookmarkShape(
    private val cutSize: Dp = 12.dp,
    private val cornerRadius: Dp = 6.dp,
    private val holeRadius: Dp = 4.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cut = with(density) { cutSize.toPx() }
        val radius = with(density) { cornerRadius.toPx() }
        val hole = with(density) { holeRadius.toPx() }

        val path = Path().apply {
            // Bookmark outer shape
            moveTo(radius, 0f)
            lineTo(size.width - 1.25f * cut, 0f)
            lineTo(size.width, cut)
            lineTo(size.width, size.height - cut)
            lineTo(size.width - 1.25f * cut, size.height)
            lineTo(radius, size.height)
            // Bottom left arc
            arcTo(
                rect = Rect(0f, size.height - radius * 2, radius * 2, size.height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // Top left arc
            arcTo(
                rect = Rect(0f, 0f, radius * 2, radius * 2),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
            // Subtract circular hole
            val holeCenter = Offset(size.width - 3 * hole, size.height / 2)
            addOval(
                Rect(
                    left = holeCenter.x - hole,
                    top = holeCenter.y - hole,
                    right = holeCenter.x + hole,
                    bottom = holeCenter.y + hole
                )
            )
        }

        return Outline.Generic(path)
    }
}