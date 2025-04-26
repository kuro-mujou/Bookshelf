package com.capstone.bookshelf.presentation.bookwriter

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookwriter.component.ComponentContainer
import com.capstone.bookshelf.presentation.bookwriter.component.EditorControls
import com.capstone.bookshelf.presentation.bookwriter.component.Paragraph
import com.capstone.bookshelf.presentation.bookwriter.component.TopBar
import com.capstone.bookshelf.util.DraggableItem
import com.capstone.bookshelf.util.dragContainer
import com.capstone.bookshelf.util.rememberDragAndDropListState
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@UnstableApi
@Composable
fun BookWriterEdit(
    contentState: ContentState,
    onNavigateBack: () -> Unit,
    onDrawerClick: () -> Unit,
) {
    val bookWriterViewModel = koinViewModel<BookWriterViewModel>()
    val bookWriterState by bookWriterViewModel.bookWriterState.collectAsStateWithLifecycle()
    val isImeVisible = WindowInsets.isImeVisible
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    LaunchedEffect(bookWriterState.triggerLoadChapter) {
        if (bookWriterState.triggerLoadChapter) {
            bookWriterViewModel.loadChapterContent(contentState.currentChapterIndex)
            bookWriterViewModel.onAction(BookWriterAction.UpdateTriggerLoadChapter(false))
        }
    }
    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) {
            focusManager.clearFocus()
            bookWriterViewModel.onAction(BookWriterAction.UpdateSelectedItem(""))
            bookWriterViewModel.onAction(
                BookWriterAction.UpdateItemMenuVisible(
                    "",
                    false
                )
            )
        }
    }
    Scaffold(
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    focusManager.clearFocus()
                    bookWriterViewModel.onAction(BookWriterAction.UpdateSelectedItem(""))
                    bookWriterViewModel.onAction(
                        BookWriterAction.UpdateItemMenuVisible(
                            "",
                            false
                        )
                    )
                }
            ),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                EditorControls(
                    bookWriterState = bookWriterState,
                    selectedState = bookWriterState.contentList.firstOrNull { it.id == bookWriterState.selectedItem }?.richTextState,
                    onAction = { action ->
                        bookWriterViewModel.editTextStyle(action)
                    },
                )
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        },
        topBar = {
            TopBar(
                bookWriterState,
                onNavigateBack = {
                    bookWriterViewModel.onAction(BookWriterAction.SaveChapter(contentState.currentChapterIndex))
                    onNavigateBack()
                },
                onDrawerClick = {
                    onDrawerClick()
                },
                onSaveClick = {
                    bookWriterViewModel.onAction(BookWriterAction.SaveChapter(contentState.currentChapterIndex))
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        BookEditLazyColumn<Paragraph>(
            bookWriterViewModel = bookWriterViewModel,
            modifier = Modifier
                .padding(innerPadding),
            itemContent = { itemData, itemModifier ->
                ComponentContainer(
                    modifier = itemModifier,
                    state = itemData.richTextState,
                    bookWriterState = bookWriterState,
                    paragraph = itemData,
                    onAddAbove = { type ->
                        bookWriterViewModel.onAction(
                            BookWriterAction.AddParagraphAbove(
                                itemData.id,
                                Paragraph(
                                    type = type,
                                    richTextState = RichTextState(),
                                )
                            )
                        )
                    },
                    onAddBelow = { type ->
                        bookWriterViewModel.onAction(
                            BookWriterAction.AddParagraphBelow(
                                itemData.id,
                                Paragraph(
                                    type = type,
                                    richTextState = RichTextState(),
                                )
                            )
                        )
                    },
                    onDelete = {
                        bookWriterViewModel.onAction(BookWriterAction.DeleteParagraph(itemData.id))
                    },
                    onVisibilityChange = { isVisible ->
                        bookWriterViewModel.onAction(
                            BookWriterAction.UpdateItemMenuVisible(
                                itemData.id,
                                isVisible
                            )
                        )
                    },
                    focusedItem = {
                        bookWriterViewModel.onAction(
                            BookWriterAction.UpdateSelectedItem(
                                itemData.id
                            )
                        )
                    },
                    onFocusRequestedAndCleared = {
                        bookWriterViewModel.onAction(BookWriterAction.SetFocusTarget(""))
                    },
                    onImageSelected = { uri ->
                        bookWriterViewModel.onAction(
                            BookWriterAction.AddImage(
                                context = context,
                                chapterIndex = contentState.currentChapterIndex,
                                paragraphId = itemData.id,
                                imageUri = uri
                            )
                        )
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> BookEditLazyColumn(
    bookWriterViewModel: BookWriterViewModel,
    modifier: Modifier = Modifier,
    itemContent: @Composable LazyItemScope.(itemData: Paragraph, modifier: Modifier) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val bookWriterState by bookWriterViewModel.bookWriterState.collectAsStateWithLifecycle()
    val rememberedOnMove = remember<(Int, Int) -> Unit>(bookWriterViewModel) {
        { from, to -> bookWriterViewModel.moveItem(from, to) }
    }
    val dragDropState = rememberDragAndDropListState<Paragraph>(
        lazyListState = lazyListState,
        onMove = rememberedOnMove,
        getCurrentList = { bookWriterState.contentList }
    )

    val getLazyListItemInfo: (Offset) -> LazyListItemInfo? = remember(lazyListState) {
        { offset ->
            lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
                offset.y.toInt() in item.offset..(item.offset + item.size)
            }
        }
    }
    LaunchedEffect(bookWriterState.triggerScroll) {
        if (bookWriterState.triggerScroll) {
            val selectedId = bookWriterState.selectedItem
            val index = if (selectedId.isNotEmpty()) {
                bookWriterState.contentList.indexOfFirst { it.id == selectedId }
            } else {
                -1
            }
            if (index != -1) {
                try {
                    lazyListState.animateScrollToItem(index)
                } catch (_: Exception) {
                } finally {
                    bookWriterViewModel.onAction(BookWriterAction.UpdateTriggerScroll(false))
                }
            } else {
                bookWriterViewModel.onAction(BookWriterAction.UpdateTriggerScroll(false))
            }
        }
    }

    LaunchedEffect(bookWriterState.itemToFocusId) {
        val focusTargetId = bookWriterState.itemToFocusId
        if (focusTargetId.isNotEmpty()) {
            val index = bookWriterState.contentList.indexOfFirst { it.id == focusTargetId }
            if (index != -1) {
                delay(100)
                lazyListState.scrollToItem(index)
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
                items = bookWriterState.contentList,
                key = { index, item -> item.id }
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

        dragDropState.draggedItemData?.let { draggedItem: Paragraph ->
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
                    ComponentContainer(
                        modifier = Modifier,
                        state = draggedItem.richTextState,
                        bookWriterState = bookWriterState,
                        paragraph = draggedItem,
                    )
                }
            }
        }
    }
}