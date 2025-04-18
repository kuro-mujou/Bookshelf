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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupPositionProvider
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@UnstableApi
fun HeaderText(
    colorPaletteState: ColorPalette,
    contentState: ContentState,
    content: HeaderContent,
    isHighlighted: Boolean,
    isSpeaking: Boolean,
    openNoteDialog: () -> Unit,
) {
    val color = if (isHighlighted && isSpeaking)
        colorPaletteState.textBackgroundColor
    else
        Color.Transparent
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = customPopupPositionProvider(),
        tooltip = {
            IconButton(
                modifier = Modifier
                    .background(
                        color = colorPaletteState.textBackgroundColor,
                        shape = CircleShape
                    ),
                onClick = {
                    openNoteDialog()
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_comment),
                    contentDescription = null
                )
            }
        },
        state = tooltipState,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            text = content.content.trim(),
            style = TextStyle(
                fontSize = content.fontSize.floatValue.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                textAlign = TextAlign.Center,
                color = colorPaletteState.textColor,
                background = color,
                lineHeight = (content.fontSize.floatValue + content.contentState.lineSpacing).sp
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@UnstableApi
fun ParagraphText(
    drawerContainerViewModel: DrawerContainerViewModel,
    colorPaletteState: ColorPalette,
    contentState: ContentState,
    content: ParagraphContent,
    isHighlighted: Boolean,
    isSpeaking: Boolean,
    openNoteDialog: () -> Unit,
) {
    val color = if (isHighlighted && isSpeaking)
        colorPaletteState.textBackgroundColor
    else
        Color.Transparent
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = customPopupPositionProvider(),
        tooltip = {
            IconButton(
                modifier = Modifier
                    .background(
                        color = colorPaletteState.textBackgroundColor,
                        shape = CircleShape
                    ),
                onClick = {
                    openNoteDialog()
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_comment),
                    contentDescription = null
                )
            }
        },
        state = tooltipState,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            text = content.text.value,
            style = TextStyle(
                textIndent = if (content.contentState.textIndent)
                    TextIndent(firstLine = (content.contentState.fontSize * 2).sp)
                else
                    TextIndent.None,
                textAlign = if (content.contentState.textAlign) TextAlign.Justify else TextAlign.Left,
                fontSize = content.contentState.fontSize.sp,
                fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                color = colorPaletteState.textColor,
                background = color,
                lineBreak = LineBreak.Paragraph,
                lineHeight = (content.contentState.fontSize + content.contentState.lineSpacing).sp
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
                    if (anchorBounds.height > popupContentSize.height)
                        anchorBounds.topRight.y + popupContentSize.height / 2
                    else
                        anchorBounds.topRight.y
                return IntOffset(x, y)
            }
        }
    }
}