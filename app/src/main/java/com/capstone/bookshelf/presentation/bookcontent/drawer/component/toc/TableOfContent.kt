package com.capstone.bookshelf.presentation.bookcontent.drawer.component.toc

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
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
import com.capstone.bookshelf.presentation.bookcontent.component.dialog.AddTOCDialog
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
    onDrawerItemClick: (Int) -> Unit,
    onAddingChapter: (String, String) -> Unit,
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
    var showAddDialog by remember { mutableStateOf(false) }
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
    if (showAddDialog) {
        AddTOCDialog(
            onDismissRequest = {
                showAddDialog = false
            },
            onConfirm = { chapterTitle, headerSize ->
                onAddingChapter(chapterTitle, headerSize)
                showAddDialog = false
            }
        )
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (contentState.book?.isEditable == true) {
                IconButton(
                    onClick = {
                        scope.launch {
                            showAddDialog = true
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.background
                    )
                }
            }
            if (showButton) {
                IconButton(
                    onClick = {
                        scope.launch {
                            drawerLazyColumnState.animateScrollToItem(contentState.currentChapterIndex)
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (contentState.book?.isEditable == true) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            colorPaletteState.textColor
                        },
                    )
                ) {
                    if (contentState.currentChapterIndex < firstItemIndex)
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_up),
                            modifier = Modifier.size(16.dp),
                            contentDescription = null,
                            tint = if (contentState.book?.isEditable == true) {
                                MaterialTheme.colorScheme.background
                            } else {
                                colorPaletteState.backgroundColor
                            },
                        )
                    else
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_down),
                            modifier = Modifier.size(16.dp),
                            contentDescription = null,
                            tint = if (contentState.book?.isEditable == true) {
                                MaterialTheme.colorScheme.background
                            } else {
                                colorPaletteState.backgroundColor
                            },
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
                    color = if (contentState.book?.isEditable == true) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        colorPaletteState.textColor
                    },
                    fontFamily = if (contentState.book?.isEditable == true) {
                        MaterialTheme.typography.bodyMedium.fontFamily
                    } else {
                        contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                    },
                ),
                label = {
                    Text(
                        text = "Enter a chapter number",
                        style = TextStyle(
                            color = if (contentState.book?.isEditable == true) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                colorPaletteState.textColor
                            },
                            fontFamily = if (contentState.book?.isEditable == true) {
                                MaterialTheme.typography.bodyMedium.fontFamily
                            } else {
                                contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                            },
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
                    unfocusedBorderColor = if (contentState.book?.isEditable == true) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        colorPaletteState.textColor
                    },
                    unfocusedLabelColor = if (contentState.book?.isEditable == true) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        colorPaletteState.textColor
                    },
                    focusedBorderColor = if (contentState.book?.isEditable == true) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        colorPaletteState.textColor
                    },
                    focusedLabelColor = if (contentState.book?.isEditable == true) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        colorPaletteState.textColor
                    },
                    cursorColor = if (contentState.book?.isEditable == true) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        colorPaletteState.textColor
                    },
                )
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = drawerLazyColumnState,
            ) {
                items(
                    items = drawerContainerState.tableOfContents
                ) { tocItem ->
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = tocItem.title,
                                style =
                                    if (drawerContainerState.tableOfContents.indexOf(tocItem) == targetSearchIndex) {
                                        TextStyle(
                                            color = if (contentState.book?.isEditable == true) {
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            } else {
                                                colorPaletteState.containerColor
                                            },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = if (contentState.book?.isEditable == true) {
                                                MaterialTheme.typography.bodyMedium.fontFamily
                                            } else {
                                                contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                                            },
                                        )
                                    } else if (drawerContainerState.tableOfContents.indexOf(tocItem) == contentState.currentChapterIndex) {
                                        TextStyle(
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = if (contentState.book?.isEditable == true) {
                                                MaterialTheme.typography.bodyMedium.fontFamily
                                            } else {
                                                contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                                            },
                                        )
                                    } else {
                                        TextStyle(
                                            fontSize = 14.sp,
                                            fontFamily = if (contentState.book?.isEditable == true) {
                                                MaterialTheme.typography.bodyMedium.fontFamily
                                            } else {
                                                contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                                            },
                                        )
                                    },
                            )
                        },
                        selected = drawerContainerState.tableOfContents.indexOf(tocItem) == contentState.currentChapterIndex,
                        onClick = {
                            onDrawerItemClick(drawerContainerState.tableOfContents.indexOf(tocItem))
                        },
                        modifier = Modifier
                            .padding(4.dp, 2.dp, 4.dp, 2.dp)
                            .wrapContentHeight(),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = if (drawerContainerState.tableOfContents.indexOf(
                                    tocItem
                                ) == targetSearchIndex
                            ) {
                                if (contentState.book?.isEditable == true) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    colorPaletteState.textColor
                                }
                            } else {
                                Color.Transparent
                            },
                            unselectedContainerColor = if (drawerContainerState.tableOfContents.indexOf(
                                    tocItem
                                ) == targetSearchIndex
                            ) {
                                if (contentState.book?.isEditable == true) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    colorPaletteState.textColor
                                }
                            } else {
                                Color.Transparent
                            },
                            selectedTextColor = if (contentState.book?.isEditable == true) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                colorPaletteState.tocTextColor
                            },
                            unselectedTextColor = if (contentState.book?.isEditable == true) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                colorPaletteState.tocTextColor.copy(alpha = 0.75f)
                            },
                        ),
                        badge = {
                            if (contentState.book?.isEditable == true) {
                                IconButton(
                                    onClick = {
                                        scope.launch {

                                        }
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = Color.Transparent
                                    )
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                                        contentDescription = null,
                                        tint = if (isSystemInDarkTheme()) Color(
                                            250,
                                            160,
                                            160
                                        ) else Color(194, 59, 34)
                                    )
                                }
                            }
                        }
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