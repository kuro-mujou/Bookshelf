package com.capstone.bookshelf.presentation.bookcontent.drawer.component.toc

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@UnstableApi
fun TableOfContents(
    drawerContainerState: DrawerContainerState,
    contentState: ContentState,
    drawerLazyColumnState: LazyListState,
    colorPaletteState: ColorPalette,
    onTocItemClick: (Int) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    var searchInput by remember { mutableStateOf("") }
    var targetSearchIndex by remember { mutableIntStateOf(-1) }
    var firstItemIndex by remember { mutableIntStateOf(0) }
    var lastItemIndex by remember { mutableIntStateOf(0) }
    var showButton by remember { mutableStateOf(false) }
    var flag by remember { mutableStateOf(false) }
    LaunchedEffect(flag) {
        if (flag) {
            drawerLazyColumnState.scrollToItem(targetSearchIndex)
            flag = false
        }
    }
    LaunchedEffect(drawerContainerState.drawerState) {
        if (!drawerContainerState.drawerState) {
            searchInput = ""
            targetSearchIndex = -1
            flag = false
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (showButton) {
                IconButton(
                    onClick = {
                        scope.launch {
                            drawerLazyColumnState.animateScrollToItem(contentState.currentChapterIndex)
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = colorPaletteState.textColor,
                    )
                ) {
                    if (contentState.currentChapterIndex < firstItemIndex)
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_up),
                            modifier = Modifier.size(16.dp),
                            contentDescription = null,
                            tint = colorPaletteState.backgroundColor,
                        )
                    else
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_down),
                            modifier = Modifier.size(16.dp),
                            contentDescription = null,
                            tint = colorPaletteState.backgroundColor,
                        )
                }
            }
        }
    ) {
        Column {
            OutlinedTextField(
                value = searchInput,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        searchInput = newValue
                    }
                },
                textStyle = TextStyle(
                    color = colorPaletteState.textColor,
                    fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                ),
                label = {
                    Text(
                        text = "Enter a chapter number",
                        style = TextStyle(
                            color =  colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                        )
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val chapterIndex = searchInput.toIntOrNull()
                        if (chapterIndex != null) {
                            targetSearchIndex =
                                if (chapterIndex < drawerContainerState.tableOfContents.size)
                                    chapterIndex
                                else
                                    drawerContainerState.tableOfContents.size - 1
                            flag = true
                            focusManager.clearFocus()
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = colorPaletteState.textColor,
                    unfocusedLabelColor = colorPaletteState.textColor,
                    focusedBorderColor = colorPaletteState.textColor,
                    focusedLabelColor = colorPaletteState.textColor,
                    cursorColor = colorPaletteState.textColor,
                )
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = drawerLazyColumnState,
                contentPadding = PaddingValues(
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
            ) {
                items(
                    items = drawerContainerState.tableOfContents
                ) { tocItem ->
                    CustomNavigationDrawerItem(
                        label = {
                            Text(
                                text = tocItem.title,
                                style =
                                    if (drawerContainerState.tableOfContents.indexOf(tocItem) == targetSearchIndex) {
                                        TextStyle(
                                            color = colorPaletteState.textColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                                        )
                                    } else if (drawerContainerState.tableOfContents.indexOf(tocItem) == contentState.currentChapterIndex) {
                                        TextStyle(
                                            color = colorPaletteState.containerColor,
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                                        )
                                    } else {
                                        TextStyle(
                                            fontSize = 14.sp,
                                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                                        )
                                    },
                            )
                        },
                        selected = drawerContainerState.tableOfContents.indexOf(tocItem) == contentState.currentChapterIndex,
                        modifier = Modifier
                            .padding(4.dp, 2.dp, 4.dp, 2.dp)
                            .wrapContentHeight()
                            .clickable(
                                onClick = {
                                    onTocItemClick(drawerContainerState.tableOfContents.indexOf(tocItem))
                                },
                            )
                            .then(
                                if(drawerContainerState.tableOfContents.indexOf(tocItem) == targetSearchIndex)
                                    Modifier.border(
                                        width = 1.dp,
                                        color = colorPaletteState.textColor,
                                        shape = RoundedCornerShape(25.dp)
                                    )
                                else {
                                    Modifier
                                }
                            ),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor =
                                if (drawerContainerState.tableOfContents.indexOf(tocItem) == targetSearchIndex) {
                                    colorPaletteState.textBackgroundColor
                                } else {
                                    colorPaletteState.textColor
                                },
                            unselectedContainerColor =
                                if (drawerContainerState.tableOfContents.indexOf(tocItem) == targetSearchIndex) {
                                    colorPaletteState.textBackgroundColor
                                } else {
                                    Color.Transparent
                                },
                            selectedTextColor = colorPaletteState.tocTextColor,
                            unselectedTextColor = colorPaletteState.tocTextColor.copy(alpha = 0.75f),
                        )
                    )
                }
            }
        }
    }
    LaunchedEffect(drawerLazyColumnState) {
        snapshotFlow { drawerLazyColumnState.layoutInfo.visibleItemsInfo.firstOrNull()?.index }
            .collect { index ->
                firstItemIndex = index ?: 0
            }
    }
    LaunchedEffect(drawerLazyColumnState) {
        snapshotFlow { drawerLazyColumnState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { index ->
                lastItemIndex = index ?: 0
            }
    }
    LaunchedEffect(firstItemIndex, lastItemIndex) {
        showButton =
            contentState.currentChapterIndex < firstItemIndex || contentState.currentChapterIndex > lastItemIndex
    }
}