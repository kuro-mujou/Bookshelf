package com.capstone.bookshelf.presentation.bookcontent.drawer.component.bookmark

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState

@UnstableApi
@Composable
fun BookmarkList(
    drawerContainerState : DrawerContainerState,
    contentState : ContentState,
    colorPaletteState: ColorPalette,
    onCardClicked: (Int) -> Unit,
){
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
    ) {
        items(
            items = drawerContainerState.tableOfContents.filter { it.isFavorite == true },
            key =  { it.index }
        ) {bookMark ->
            BookmarkCard(
                bookmarkContent = bookMark.title,
                bookmarkIndex = bookMark.index,
                contentState = contentState,
                colorPaletteState = colorPaletteState,
                bookmarkStyle = contentState.selectedBookmarkStyle,
                onCardClicked = {
                    onCardClicked(it)
                }
            )
        }
    }
}