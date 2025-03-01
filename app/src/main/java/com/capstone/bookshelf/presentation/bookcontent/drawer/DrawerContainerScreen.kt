package com.capstone.bookshelf.presentation.bookcontent.drawer

import android.os.Build
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
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.drawer.component.toc.TableOfContents
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@UnstableApi
@Composable
fun DrawerScreen(
    drawerContainerState : DrawerContainerState,
    contentState: ContentState,
    drawerState: DrawerState,
    drawerLazyColumnState: LazyListState,
    colorPaletteState: ColorPalette,
    hazeState: HazeState,
    onDrawerItemClick: (Int) -> Unit,
    content: @Composable () -> Unit
){
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
    var selectedTabIndex by remember {
        mutableIntStateOf(0)
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column (
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .then(
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                            Modifier.hazeEffect(
                                state = hazeState,
                                style = style
                            )
                        }else{
                            Modifier.background(colorPaletteState.containerColor)
                        }
                    ),
            ){
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(8.dp),
                ) {
                    AsyncImage(
                        model =
                        if(contentState.book?.coverImagePath=="error")
                            R.mipmap.book_cover_not_available
                        else
                            contentState.book?.coverImagePath,
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
                        contentState.book?.title?.let {
                            Text(
                                text = it,
                                style = TextStyle(
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                    color = colorPaletteState.textColor,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                                )
                            )
                        }
                        contentState.book?.authors?.joinToString(",")?.let {
                            Text(
                                text = it,
                                style = TextStyle(
                                    color = colorPaletteState.textColor,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                                ),
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
                        indicator = {TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = true),
                            width = Dp.Unspecified,
                            color = colorPaletteState.textColor
                        )},
                        containerColor = Color.Transparent,
                        contentColor = colorPaletteState.textColor,
                    ) {
                        tabItems.forEachIndexed { index, item ->
                            Tab(
                                selected = index == selectedTabIndex,
                                onClick = {
                                    selectedTabIndex = index
                                },
                                text = {
                                    Text(
                                        text = item.title,
                                        style = TextStyle(
                                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                                        )
                                    )
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