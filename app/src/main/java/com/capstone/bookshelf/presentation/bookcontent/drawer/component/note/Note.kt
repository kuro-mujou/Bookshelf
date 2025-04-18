package com.capstone.bookshelf.presentation.bookcontent.drawer.component.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.R
import com.capstone.bookshelf.domain.wrapper.Note
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState

@UnstableApi
@Composable
fun NoteList(
    drawerContainerState: DrawerContainerState,
    contentState: ContentState,
    colorPaletteState: ColorPalette,
    onUndo: () -> Unit,
    onCardClicked: (Int, Int) -> Unit,
    onCardSelected: (Int) -> Unit,
    onCardDeleted: (Note) -> Unit,
    onEditNote: (Note, String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = drawerContainerState.enableUndoDeleteNote
        ) {
            IconButton(
                onClick = {
                    onUndo()
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_undo),
                    contentDescription = null,
                    tint = colorPaletteState.textColor
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 4.dp)
                .fillMaxSize()
        ) {
            itemsIndexed(
                items = drawerContainerState.notes
            ) { index, note ->
                NoteCard(
                    drawerContainerState = drawerContainerState,
                    contentState = contentState,
                    colorPaletteState = colorPaletteState,
                    index = index,
                    note = note,
                    onCardClicked = { tocId, contentId ->
                        onCardClicked(tocId, contentId)
                    },
                    onCardSelected = {
                        onCardSelected(it)
                    },
                    onCardDeleted = {
                        onCardDeleted(it)
                    },
                    onEditNote = { note, newInput ->
                        onEditNote(note, newInput)
                    }
                )
            }
        }
    }
}