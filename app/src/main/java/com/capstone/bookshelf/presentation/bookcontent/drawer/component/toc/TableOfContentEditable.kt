package com.capstone.bookshelf.presentation.bookcontent.drawer.component.toc

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.R
import com.capstone.bookshelf.domain.wrapper.TableOfContent
import com.capstone.bookshelf.presentation.bookcontent.component.dialog.AddTOCDialog
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerViewModel
import com.capstone.bookshelf.util.CustomAlertDialog
import com.capstone.bookshelf.util.DraggableItem
import com.capstone.bookshelf.util.dragContainer
import com.capstone.bookshelf.util.rememberDragAndDropListState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@UnstableApi
fun TableOfContentsEditable(
    drawerContainerViewModel: DrawerContainerViewModel,
    drawerContainerState: DrawerContainerState,
    contentState: ContentState,
    drawerLazyColumnState: LazyListState,
    onTocItemClick: (Int) -> Unit,
    onAddTocItem: (String, String) -> Unit,
    onDeleteTocItem: (TableOfContent) -> Unit,
    onMoveItem: (Int, Int) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var enableDeleteMode by remember { mutableStateOf(false) }
    var searchInput by remember { mutableStateOf("") }
    var targetSearchIndex by remember { mutableIntStateOf(-1) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteChapterDialog by remember { mutableStateOf(false) }
    var onDeleteItem by remember { mutableStateOf<TableOfContent?>(null) }
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
            enableDeleteMode = false
        }
    }
    if (showAddDialog) {
        AddTOCDialog(
            onDismissRequest = {
                showAddDialog = false
            },
            onConfirm = { chapterTitle, headerSize ->
                showAddDialog = false
                onAddTocItem(chapterTitle, headerSize)
            }
        )
    }
    if (showDeleteChapterDialog) {
        CustomAlertDialog(
            title = "Delete Chapter",
            text = "Are you sure you want to delete this chapter?\n\nThis action cannot be undone.",
            confirmButtonText = "Confirm",
            dismissButtonText = "Cancel",
            onConfirm = {
                showDeleteChapterDialog = false
                onDeleteTocItem(onDeleteItem!!)
            },
            onDismiss = {
                showDeleteChapterDialog = false
                onDeleteItem = null
            }
        )
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
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
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                ),
                label = {
                    Text(
                        text = "Enter a chapter number",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
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
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.onBackground,
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.End
            ) {
                if (enableDeleteMode) {
                    IconButton(
                        onClick = {
                            enableDeleteMode = false
                            targetSearchIndex = -1
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_confirm),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            enableDeleteMode = true
                            targetSearchIndex = -1
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            showAddDialog = true
                            targetSearchIndex = -1
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_add_music),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            TocEditLazyColumn<TableOfContent>(
                lazyListState = drawerLazyColumnState,
                drawerContainerViewModel = drawerContainerViewModel,
                moveItem = {from, to->
                    onMoveItem(from, to)
                },
                modifier = Modifier.fillMaxSize(),
                itemContent = { itemData, itemModifier ->
                    CustomNavigationDrawerItem(
                        label = {
                            Text(
                                text = itemData.title,
                                style =
                                    if (drawerContainerState.tableOfContents.indexOf(itemData) == targetSearchIndex) {
                                        TextStyle(
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                        )
                                    } else if (drawerContainerState.tableOfContents.indexOf(itemData) == contentState.currentChapterIndex) {
                                        TextStyle(
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                        )
                                    } else {
                                        TextStyle(
                                            fontSize = 14.sp,
                                            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                        )
                                    },
                            )
                        },
                        selected = drawerContainerState.tableOfContents.indexOf(itemData) == contentState.currentChapterIndex,
                        modifier = itemModifier
                            .padding(4.dp, 2.dp, 4.dp, 2.dp)
                            .wrapContentHeight()
                            .clickable(
                                onClick = {
                                    onTocItemClick(
                                        drawerContainerState.tableOfContents.indexOf(
                                            itemData
                                        )
                                    )
                                    Log.d(
                                        "TableOfContentsEditable",
                                        "id: ${drawerContainerState.tableOfContents.indexOf(itemData)}"
                                    )
                                }
                            )
                            .then(
                                if (drawerContainerState.tableOfContents.indexOf(itemData) == targetSearchIndex)
                                    Modifier.border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        shape = RoundedCornerShape(25.dp)
                                    )
                                else if (drawerContainerState.tableOfContents.indexOf(itemData) == contentState.currentChapterIndex) {
                                    Modifier.border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        shape = RoundedCornerShape(25.dp)
                                    )
                                } else {
                                    Modifier
                                }
                            ),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor =
                                if (drawerContainerState.tableOfContents.indexOf(itemData) == targetSearchIndex) {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                },
                            unselectedContainerColor =
                                if (drawerContainerState.tableOfContents.indexOf(itemData) == targetSearchIndex) {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                } else {
                                    Color.Transparent
                                },
                            selectedTextColor = MaterialTheme.colorScheme.onBackground,
                            unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                        ),
                        badge = {
                            Row {
                                if (enableDeleteMode) {
                                    IconButton(
                                        onClick = {
                                            targetSearchIndex = -1
                                            showDeleteChapterDialog = true
                                            onDeleteItem = itemData
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                                            contentDescription = null,
                                            tint = if (isSystemInDarkTheme())
                                                Color(250, 160, 160)
                                            else
                                                Color(194, 59, 34)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> TocEditLazyColumn(
    lazyListState: LazyListState,
    drawerContainerViewModel: DrawerContainerViewModel,
    moveItem: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable LazyItemScope.(itemData: TableOfContent, modifier: Modifier) -> Unit
) {
    val drawerContainerState by drawerContainerViewModel.state.collectAsStateWithLifecycle()
    val rememberedOnMove = remember<(Int, Int) -> Unit>(drawerContainerViewModel) {
        { from, to ->
            drawerContainerViewModel.moveItem(from, to)
            moveItem(from, to)
        }
    }
    val dragDropState = rememberDragAndDropListState<TableOfContent>(
        lazyListState = lazyListState,
        onMove = rememberedOnMove,
        getCurrentList = { drawerContainerState.tableOfContents }
    )

    val getLazyListItemInfo: (Offset) -> LazyListItemInfo? = remember(lazyListState) {
        { offset ->
            lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
                offset.y.toInt() in item.offset..(item.offset + item.size)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .dragContainer(dragDropState, getLazyListItemInfo)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = lazyListState
        ) {
            itemsIndexed(
                items = drawerContainerState.tableOfContents,
                key = { index, item -> item.tocId!! }
            ) { index, item ->
                DraggableItem(
                    dragAndDropListState = dragDropState,
                    index = index
                ) { _, itemModifier ->
                    itemContent(
                        item,
                        itemModifier
                    )
                }
            }
        }

        dragDropState.draggedItemData?.let { draggedItem: TableOfContent ->
            dragDropState.currentDragPosition?.let { currentPosition ->
                val itemHeight = dragDropState.draggedItemHeight?.toFloat() ?: 0f
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = 0f
                            translationY = currentPosition.y - (itemHeight / 2f)
                            scaleX = 1.05f
                            scaleY = 1.05f
                            shadowElevation = 8.dp.toPx()
                            alpha = 0.9f
                        }
                ) {
                    CustomNavigationDrawerItem(
                        label = {
                            Text(
                                text = draggedItem.title,
                                style =
                                    TextStyle(
                                        fontSize = 14.sp,
                                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                    )
                            )
                        },
                        selected = false,
                        modifier = Modifier
                            .padding(4.dp, 2.dp, 4.dp, 2.dp)
                            .wrapContentHeight()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = RoundedCornerShape(25.dp)
                            ),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color.Transparent,
                            unselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.onBackground,
                            unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                        )
                    )
                }
            }
        }
    }
}