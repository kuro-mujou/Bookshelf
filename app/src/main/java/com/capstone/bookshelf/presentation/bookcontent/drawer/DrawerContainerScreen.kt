package com.capstone.bookshelf.presentation.bookcontent.drawer

import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.capstone.bookshelf.R
import com.capstone.bookshelf.domain.wrapper.Note
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.component.bookmark.BookmarkList
import com.capstone.bookshelf.presentation.bookcontent.drawer.component.note.NoteList
import com.capstone.bookshelf.presentation.bookcontent.drawer.component.toc.TableOfContents
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@UnstableApi
@Composable
fun DrawerScreen(
    dataStoreManager: DataStoreManager,
    contentViewModel: ContentViewModel,
    drawerContainerState: DrawerContainerState,
    contentState: ContentState,
    drawerState: DrawerState,
    drawerLazyColumnState: LazyListState,
    colorPaletteState: ColorPalette,
    hazeState: HazeState,
    onDrawerItemClick: (Int) -> Unit,
    onAddingChapter: (String, String) -> Unit,
    onDeleteBookmark: (Int) -> Unit,
    onUndoDeleteBookmark: () -> Unit,
    onNoteClicked: (Int, Int) -> Unit,
    onNoteSelected: (Int) -> Unit,
    onEditNote: (Note, String) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onUndoDeleteNote: () -> Unit,
    onTabItemClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val style = HazeMaterials.thin(colorPaletteState.containerColor)
    val tabItems = listOf(
        TabItem(
            title = "Table of Contents",
        ),
        TabItem(
            title = "Note",
        ),
        TabItem(
            title = "Book Mark",
        ),
    )
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(drawerState.isClosed) {
        selectedTabIndex = 0
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .then(
                        if (contentState.book?.isEditable == true) {
                            Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                Modifier.hazeEffect(
                                    state = hazeState,
                                    style = style
                                )
                            } else {
                                Modifier.background(colorPaletteState.containerColor)
                            }
                        }
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(8.dp),
                ) {
                    AsyncImage(
                        model =
                            if (contentState.book?.coverImagePath == "error")
                                R.mipmap.book_cover_not_available
                            else
                                contentState.book?.coverImagePath,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .width(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(8.dp)
                    ) {
                        contentState.book?.let {
                            Text(
                                text = it.title,
                                style = TextStyle(
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                    color = if (it.isEditable) {
                                        MaterialTheme.colorScheme.onBackground
                                    } else {
                                        colorPaletteState.textColor
                                    },
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = if (it.isEditable) {
                                        MaterialTheme.typography.bodyMedium.fontFamily
                                    } else {
                                        contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                                    },
                                )
                            )
                        }
                        contentState.book?.let {
                            Text(
                                text = it.authors.joinToString(","),
                                style = TextStyle(
                                    color = if (it.isEditable) {
                                        MaterialTheme.colorScheme.onBackground
                                    } else {
                                        colorPaletteState.textColor
                                    },
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = if (it.isEditable) {
                                        MaterialTheme.typography.bodyMedium.fontFamily
                                    } else {
                                        contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                                    },
                                ),
                            )
                        }
                    }
                }
                if (contentState.book?.isEditable == true) {
                    TableOfContents(
                        drawerContainerState = drawerContainerState,
                        contentState = contentState,
                        drawerLazyColumnState = drawerLazyColumnState,
                        colorPaletteState = colorPaletteState,
                        onDrawerItemClick = { contentPageIndex ->
                            onDrawerItemClick(contentPageIndex)
                        },
                        onAddingChapter = { chapterTitle, headerSize ->
                            onAddingChapter(chapterTitle, headerSize)
                        }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        PrimaryTabRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            selectedTabIndex = selectedTabIndex,
                            indicator = {
                                TabRowDefaults.PrimaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(
                                        selectedTabIndex,
                                        matchContentSize = true
                                    ),
                                    width = Dp.Unspecified,
                                    color = colorPaletteState.textColor
                                )
                            },
                            containerColor = Color.Transparent,
                            contentColor = colorPaletteState.textColor,
                            divider = {
                                HorizontalDivider(
                                    color = colorPaletteState.backgroundColor
                                )
                            }
                        ) {
                            tabItems.forEachIndexed { index, item ->
                                Tab(
                                    selected = index == selectedTabIndex,
                                    onClick = {
                                        selectedTabIndex = index
                                        onTabItemClick()
                                    },
                                    text = {
                                        Text(
                                            text = item.title,
                                            style = TextStyle(
                                                textAlign = TextAlign.Center,
                                                fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                                            )
                                        )
                                    },
                                )
                            }
                        }
                        Crossfade(targetState = selectedTabIndex) { option ->
                            when (option) {
                                0 -> {
                                    TableOfContents(
                                        drawerContainerState = drawerContainerState,
                                        contentState = contentState,
                                        drawerLazyColumnState = drawerLazyColumnState,
                                        colorPaletteState = colorPaletteState,
                                        onDrawerItemClick = { contentPageIndex ->
                                            onDrawerItemClick(contentPageIndex)
                                        },
                                        onAddingChapter = { chapterTitle, headerSize ->
                                            onAddingChapter(chapterTitle, headerSize)
                                        }
                                    )
                                }

                                1 -> {
                                    NoteList(
                                        drawerContainerState = drawerContainerState,
                                        contentState = contentState,
                                        colorPaletteState = colorPaletteState,
                                        onUndo = {
                                            onUndoDeleteNote()
                                        },
                                        onCardClicked = { tocId, contentId ->
                                            onNoteClicked(tocId, contentId)
                                        },
                                        onCardSelected = {
                                            onNoteSelected(it)
                                        },
                                        onCardDeleted = {
                                            onDeleteNote(it)
                                        },
                                        onEditNote = { note, newInput ->
                                            onEditNote(note, newInput)
                                        }
                                    )
                                }

                                2 -> {
                                    BookmarkList(
                                        drawerContainerState = drawerContainerState,
                                        contentState = contentState,
                                        viewModel = contentViewModel,
                                        dataStoreManager = dataStoreManager,
                                        colorPaletteState = colorPaletteState,
                                        onCardClicked = {
                                            onDrawerItemClick(it)
                                        },
                                        onDeleted = {
                                            onDeleteBookmark(it)
                                        },
                                        onUndo = {
                                            onUndoDeleteBookmark()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        gesturesEnabled = contentState.enablePagerScroll || drawerState.isOpen,
    ) {
        content()
    }
}

data class TabItem(
    val title: String,
)