package com.capstone.bookshelf.presentation.bookcontent.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.capstone.bookshelf.R
import com.capstone.bookshelf.domain.wrapper.Book
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.drawer.component.toc.TableOfContents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerScreen(
    drawerContainerState : DrawerContainerState,
    contentState: ContentState,
    drawerState: DrawerState,
    drawerLazyColumnState: LazyListState,
    colorPaletteState: ColorPalette,
    book: Book?,
    onDrawerItemClick: (Int) -> Unit,
    content: @Composable () -> Unit
){
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
    var selectedTabIndex by remember {
        mutableIntStateOf(0)
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column (
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxHeight()
                    .width(300.dp)
                    .background(colorPaletteState.backgroundColor),
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(8.dp),
                ) {
                    AsyncImage(
                        model =
                        if(book?.coverImagePath=="error")
                            R.mipmap.book_cover_not_available
                        else
                            book?.coverImagePath,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .width(70.dp)
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(8.dp)
                    ) {
                        book?.title?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        book?.authors?.joinToString(",")?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    PrimaryTabRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        selectedTabIndex = selectedTabIndex,
                        containerColor = colorPaletteState.backgroundColor,
                        contentColor = colorPaletteState.textColor,
                    ) {
                        tabItems.forEachIndexed { index, item ->
                            Tab(
                                selected = index == selectedTabIndex,
                                onClick = {
                                    selectedTabIndex = index
                                },
                                text = {
                                    Text(text = item.title)
                                },
                            )
                        }
                    }
                    when(selectedTabIndex) {
                        0 -> {
                            TableOfContents(
                                drawerContainerState = drawerContainerState,
                                contentState = contentState,
                                drawerLazyColumnState = drawerLazyColumnState,
                                colorPaletteState = colorPaletteState,
                                onDrawerItemClick = {contentPageIndex->
                                    onDrawerItemClick(contentPageIndex)
                                },
                            )
                        }
                        1 -> {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ){
                                Text(text = "Note")
                            }
                        }
                        2 -> {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ){
                                Text(text = "BookMark")
                            }
                        }
                    }

                }
            }
        },
        gesturesEnabled = true
    ){
        content()
    }
}
data class TabItem(
    val title: String,
)