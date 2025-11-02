package com.capstone.bookshelf.presentation.home_screen.setting_screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.bookshelf.presentation.bookcontent.drawer.component.bookmark.BookmarkCard
import com.capstone.bookshelf.presentation.bookcontent.drawer.component.bookmark.BookmarkMenuItem
import com.capstone.bookshelf.presentation.bookcontent.drawer.component.bookmark.BookmarkStyle
import com.capstone.bookshelf.presentation.home_screen.setting_screen.SettingAction
import com.capstone.bookshelf.presentation.home_screen.setting_screen.SettingState

@Composable
fun BookmarkMenu(
    settingState: SettingState,
    onAction: (SettingAction) -> Unit,
) {
    val listState = rememberLazyListState()
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
    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    WindowInsets.safeContent
                        .only(WindowInsetsSides.Horizontal)
                        .asPaddingValues()
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Center),
                    text = "BOOKMARK STYLE",
                    style = TextStyle(
                        fontSize = 20.sp,
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
                    ) { listItem ->
                        BookmarkItemView(
                            listItem = listItem,
                            settingState = settingState,
                            onSelected = {
                                onAction(
                                    SettingAction.UpdateSelectedBookmarkStyle(
                                        it
                                    )
                                )
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun BookmarkItemView(
    settingState: SettingState,
    listItem: BookmarkMenuItem,
    onSelected: (BookmarkStyle) -> Unit
) {
    val checked = settingState.selectedBookmarkStyle == listItem.bookmarkStyle
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
        )
        BookmarkCard(
            bookmarkContent = listItem.title,
            bookmarkIndex = listItem.id,
            bookmarkStyle = listItem.bookmarkStyle,
            onCardClicked = {
                onSelected(listItem.bookmarkStyle)
            },
        )
    }
}