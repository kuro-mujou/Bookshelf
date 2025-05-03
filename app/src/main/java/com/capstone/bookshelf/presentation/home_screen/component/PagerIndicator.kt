package com.capstone.bookshelf.presentation.home_screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import kotlin.math.absoluteValue

@Composable
fun PagerIndicator(
    pagerState: PagerState,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = activeColor.copy(alpha = 0.3f),
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val pageCount = pagerState.pageCount
        val currentPage = pagerState.currentPage
        val targetPage = pagerState.targetPage
        val offsetFraction = pagerState.currentPageOffsetFraction.absoluteValue

        repeat(pageCount) { index ->
            val color = when (index) {
                currentPage -> lerp(activeColor, inactiveColor, offsetFraction)
                targetPage -> lerp(inactiveColor, activeColor, offsetFraction)
                else -> inactiveColor
            }
            val size = lerp(
                start = 8.dp,
                stop = 8.dp * 1.2f,
                fraction = if (index == currentPage || index == targetPage)
                    (1f - offsetFraction.coerceIn(0f, 1f))
                else 0f
            )
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}