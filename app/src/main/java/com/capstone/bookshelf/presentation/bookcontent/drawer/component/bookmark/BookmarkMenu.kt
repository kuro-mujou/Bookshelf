package com.capstone.bookshelf.presentation.bookcontent.drawer.component.bookmark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentAction
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.util.DataStoreManager
import kotlinx.coroutines.flow.first

data class BookmarkMenuItem(
    val id: Int,
    val title: String,
    val bookmarkStyle: BookmarkStyle
)

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun BookmarkMenu(
    contentViewModel: ContentViewModel,
    dataStoreManager: DataStoreManager,
    colorPalette: ColorPalette,
    contentState: ContentState
){
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        contentViewModel.onContentAction(ContentAction.UpdateSelectedBookmarkStyle(dataStoreManager.bookmarkStyle.first()))
    }
    val items = remember {
        listOf(
            BookmarkMenuItem(1, "Wavy style", BookmarkStyle.WAVE_WITH_BIRDS),
            BookmarkMenuItem(2, "Cloudy style", BookmarkStyle.CLOUD_WITH_BIRDS),
            BookmarkMenuItem(3, "Starry night", BookmarkStyle.STARRY_NIGHT),
            BookmarkMenuItem(4, "Geometric triangle", BookmarkStyle.GEOMETRIC_TRIANGLE),
            BookmarkMenuItem(5, "Polygonal hexagon", BookmarkStyle.POLYGONAL_HEXAGON),
            BookmarkMenuItem(6, "Scattered hexagon", BookmarkStyle.SCATTERED_HEXAGON),
            BookmarkMenuItem(7, "Cherry Blossom rain", BookmarkStyle.CHERRY_BLOSSOM_RAIN),
        )
    }
    Surface(
        color = colorPalette.backgroundColor
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "BOOKMARK STYLE",
                    style = TextStyle(
                        fontSize = 20.sp,
                        color = colorPalette.textColor,
                        fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                    )
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 8.dp),
                state = listState,
                content = {
                    items(
                        items = items,
                        key = { it.id }
                    ) { listItem->
                        BookmarkItemView(
                            listItem = listItem,
                            contentState = contentState,
                            colorPaletteState = colorPalette,
                            onSelected = {
                                contentViewModel.onContentAction(ContentAction.UpdateSelectedBookmarkStyle(it))
                            }
                        )
                    }
                }
            )
        }
    }
}

@UnstableApi
@Composable
fun BookmarkItemView(
    listItem: BookmarkMenuItem,
    contentState: ContentState,
    colorPaletteState: ColorPalette,
    onSelected: (BookmarkStyle) -> Unit
){
    val checked = contentState.selectedBookmarkStyle == listItem.bookmarkStyle
    Row(
        modifier = Modifier
            .padding(all = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = {
                onSelected(listItem.bookmarkStyle)
            },
            colors = CheckboxDefaults.colors(
                checkedColor = colorPaletteState.textColor,
                uncheckedColor = colorPaletteState.textColor,
                checkmarkColor = colorPaletteState.backgroundColor
            )
        )
        BookmarkCard(
            bookmarkContent = listItem.title,
            bookmarkIndex = listItem.id,
            contentState = contentState,
            colorPaletteState = colorPaletteState,
            bookmarkStyle = listItem.bookmarkStyle,
            onCardClicked = {
                onSelected(listItem.bookmarkStyle)
            },
        )
    }
}