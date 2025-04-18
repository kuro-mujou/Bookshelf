package com.capstone.bookshelf.presentation.bookcontent.component.fab

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette

@Composable
fun CustomFab(
    colorPaletteState: ColorPalette,
    onFabClick: () -> Unit,
) {
    FilledIconButton(
        modifier = Modifier
            .padding(bottom = 12.dp)
            .size(48.dp)
            .border(
                width = 2.dp,
                color = colorPaletteState.textColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            ),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = colorPaletteState.containerColor
        ),
        shape = RoundedCornerShape(10.dp),
        onClick = {
            onFabClick()
        }
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_undo),
            contentDescription = null,
            tint = colorPaletteState.textColor
        )
    }
}