package com.capstone.bookshelf.presentation.bookcontent.drawer.component.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.R
import com.capstone.bookshelf.domain.wrapper.Note
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.component.dialog.NoteDialog
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState

@UnstableApi
@Composable
fun NoteCard(
    drawerContainerState: DrawerContainerState,
    contentState: ContentState,
    colorPaletteState: ColorPalette,
    index: Int,
    note: Note,
    onCardClicked: (Int, Int) -> Unit,
    onCardSelected: (Int) -> Unit,
    onCardDeleted: (Note) -> Unit,
    onEditNote: (Note, String) -> Unit
) {
    var isOpenDialog by remember { mutableStateOf(false) }
    if (isOpenDialog) {
        NoteDialog(
            contentState = contentState,
            note = note.noteBody,
            noteInput = note.noteInput,
            colorPaletteState = colorPaletteState,
            onDismiss = {
                isOpenDialog = false
            },
            onNoteChanged = { noteInput ->
                onEditNote(
                    note, noteInput
                )
            }
        )
    }
    ElevatedCard(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    onCardClicked(note.tocId, note.contentId)
                },
                onLongClick = {
                    onCardSelected(index)
                }
            )
            .then(
                if (drawerContainerState.currentSelectedNote == index) {
                    Modifier.border(
                        width = 2.dp,
                        color = colorPaletteState.textColor,
                        shape = CardDefaults.elevatedShape
                    )
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colorPaletteState.backgroundColor,
            contentColor = colorPaletteState.textColor,
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp,
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .fillMaxWidth()
            ) {
                VerticalDivider(
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                    thickness = 2.dp,
                    color = colorPaletteState.textColor
                )
                Text(
                    text = note.noteBody,
                    modifier = Modifier.padding(all = 8.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontStyle = FontStyle.Italic,
                        color = colorPaletteState.textColor,
                        textAlign = TextAlign.Justify,
                        fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                    )
                )
            }
            Text(
                text = note.noteInput,
                style = TextStyle(
                    color = colorPaletteState.textColor,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Justify,
                    fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                )
            )
            Text(
                text = note.timestamp,
                modifier = Modifier.align(Alignment.End),
                style = TextStyle(
                    fontStyle = FontStyle.Italic,
                    color = colorPaletteState.textColor,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Justify,
                    fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                )
            )
            AnimatedVisibility(
                visible = drawerContainerState.currentSelectedNote == index
            ) {
                Row {
                    IconButton(
                        onClick = {
                            onCardDeleted(note)
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                            contentDescription = "Delete",
                            tint = colorPaletteState.textColor
                        )
                    }
                    IconButton(
                        onClick = {
                            isOpenDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_edit),
                            contentDescription = "Edit",
                            tint = colorPaletteState.textColor
                        )
                    }
                }
            }
        }
    }
}