package com.capstone.bookshelf.presentation.bookcontent.drawer.component.bookmark

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import com.capstone.bookshelf.util.DataStoreManager

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun BookmarkList(
    drawerContainerState: DrawerContainerState,
    contentState: ContentState,
    viewModel: ContentViewModel,
    dataStoreManager: DataStoreManager,
    colorPaletteState: ColorPalette,
    onCardClicked: (Int) -> Unit,
    onDeleted: (Int) -> Unit,
    onUndo: () -> Unit
) {
    var openBookmarkThemeMenu by remember { mutableStateOf(false) }
    val bookmarkMenuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    openBookmarkThemeMenu = true
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_setting),
                    contentDescription = null,
                    tint = colorPaletteState.textColor
                )
            }
            AnimatedVisibility(
                visible = drawerContainerState.enableUndoDeleteBookmark
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
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
        ) {
            items(
                items = drawerContainerState.tableOfContents.filter { it.isFavorite == true },
                key = { it.index }
            ) { bookMark ->
                BookmarkCard(
                    bookmarkContent = bookMark.title,
                    bookmarkIndex = bookMark.index,
                    contentState = contentState,
                    colorPaletteState = colorPaletteState,
                    bookmarkStyle = contentState.selectedBookmarkStyle,
                    onCardClicked = {
                        onCardClicked(it)
                    },
                    onDeleted = {
                        onDeleted(it)
                    }
                )
            }
        }
    }
    if (openBookmarkThemeMenu) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
            dragHandle = {
                Surface(
                    modifier = Modifier
                        .padding(vertical = 22.dp),
                    color = colorPaletteState.textColor,
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Box(Modifier.size(width = 32.dp, height = 4.dp))
                }
            },
            sheetState = bookmarkMenuSheetState,
            onDismissRequest = { openBookmarkThemeMenu = false },
            containerColor = colorPaletteState.backgroundColor
        ) {
            BookmarkMenu(
                contentViewModel = viewModel,
                dataStoreManager = dataStoreManager,
                colorPalette = colorPaletteState,
                contentState = contentState
            )
        }
    }
}