package com.capstone.bookshelf.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.capstone.bookshelf.util.darken

@Composable
fun BookChip(
    modifier: Modifier = Modifier,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    chipContent: @Composable RowScope.() -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected)
            color
        else
            MaterialTheme.colorScheme.surfaceContainer,
        border = if (!selected) BorderStroke(width = 2.dp, color = color.darken(0.2f)) else null,
        contentColor = if (!selected)
            MaterialTheme.colorScheme.onSurface
        else
            MaterialTheme.colorScheme.inverseOnSurface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            chipContent()
        }
    }
}