package com.capstone.bookshelf.presentation.bookcontent.content.content_component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.component.dialog.NoteDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderText(
    colorPaletteState: ColorPalette,
    content: HeaderContent,
    level: Int,
    style: TextStyle,
    isHighlighted: Boolean,
    isSpeaking: Boolean
) {
    val color = if(isHighlighted && isSpeaking)
        colorPaletteState.textBackgroundColor
    else
        Color.Transparent
    val size = when(level){
        1 -> style.fontSize*2
        2 -> style.fontSize*1.5
        3 -> style.fontSize*1.17
        4 -> style.fontSize*1
        5 -> style.fontSize*0.83
        6 -> style.fontSize*0.67
        else -> style.fontSize
    }
    var isOpenDialog by remember { mutableStateOf(false) }
    val tooltipState = rememberTooltipState()
    if(isOpenDialog){
        NoteDialog(
            note = content.content,
            colorPaletteState = colorPaletteState,
            onDismiss = {
                isOpenDialog = false
            }
        )
    }
    TooltipBox(
        positionProvider = customPopupPositionProvider(),
        tooltip = {
            IconButton(
                modifier = Modifier
                    .background(
                        color = style.background,
                        shape = CircleShape
                    ),
                onClick = {
                    isOpenDialog = true
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_twotone_bookmark_add),
                    contentDescription = null
                )
            }
        },
        state = tooltipState,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            text = content.content,
            style = TextStyle(
                fontSize = size,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = colorPaletteState.textColor,
                background = color
            )
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParagraphText(
    colorPaletteState: ColorPalette,
    content: ParagraphContent,
    style: TextStyle,
    isHighlighted: Boolean,
    isSpeaking: Boolean,
) {
    val color = if(isHighlighted && isSpeaking)
        colorPaletteState.textBackgroundColor
    else
        Color.Transparent
    var isOpenDialog by remember { mutableStateOf(false) }
    val tooltipState = rememberTooltipState()
    if(isOpenDialog){
        NoteDialog(
            note = content.text.value,
            colorPaletteState = colorPaletteState,
            onDismiss = {
                isOpenDialog = false
            }
        )
    }
    TooltipBox(
        positionProvider = customPopupPositionProvider(),
        tooltip = {
            IconButton(
                modifier = Modifier
                    .background(
                        color = style.background,
                        shape = CircleShape
                    ),
                onClick = {
                    isOpenDialog = true
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_twotone_bookmark_add),
                    contentDescription = null
                )
            }
        },
        state = tooltipState,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            text = content.text.value,
            style = TextStyle(
                textIndent = style.textIndent,
                textAlign = style.textAlign,
                fontSize = style.fontSize,
                color = colorPaletteState.textColor,
                background = color,
                lineBreak = style.lineBreak,
            ),
        )
    }
}
@Composable
fun customPopupPositionProvider(): PopupPositionProvider {
    val tooltipAnchorSpacing = 0
    return remember(tooltipAnchorSpacing) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val x = anchorBounds.topRight.x
                val y =
                    if(anchorBounds.height>popupContentSize.height)
                        anchorBounds.topRight.y + popupContentSize.height/2
                    else
                        anchorBounds.topRight.y
                return IntOffset(x, y)
            }
        }
    }
}