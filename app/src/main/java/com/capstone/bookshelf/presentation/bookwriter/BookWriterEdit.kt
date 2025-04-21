package com.capstone.bookshelf.presentation.bookwriter

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookwriter.component.ComponentContainer
import com.capstone.bookshelf.presentation.bookwriter.component.EditorControls
import com.capstone.bookshelf.presentation.bookwriter.component.Paragraph
import com.capstone.bookshelf.presentation.bookwriter.component.ParagraphType
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@UnstableApi
@Composable
fun BookWriterEdit(
    bookWriterViewModel: BookWriterViewModel,
    bookWriterState: BookWriterState,
    contentState: ContentState,
    onNavigateBack: () -> Unit,
    onDrawerClick: () -> Unit,
) {
    val isImeVisible = WindowInsets.isImeVisible
    val lazyListState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    LaunchedEffect(contentState.currentChapterIndex) {
        bookWriterViewModel.loadChapterContent(contentState.currentChapterIndex)
    }
    LaunchedEffect(bookWriterState.triggerLoadChapter) {
        if (bookWriterState.triggerLoadChapter) {
            bookWriterViewModel.loadChapterContent(contentState.currentChapterIndex)
            bookWriterViewModel.onAction(BookWriterAction.UpdateTriggerLoadChapter(false))
        }
    }
    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) {
            bookWriterViewModel.onAction(BookWriterAction.UpdateSelectedItem(""))
            focusManager.clearFocus()
            bookWriterViewModel.onAction(
                BookWriterAction.UpdateItemMenuVisible(
                    "",
                    false
                )
            )
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
    Scaffold(
        modifier = Modifier
            .imePadding()
            .fillMaxSize(),
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                IconButton(
                    modifier = Modifier.statusBarsPadding(),
                    onClick = {
                        bookWriterViewModel.onAction(BookWriterAction.SaveChapter(contentState.currentChapterIndex))
                        onNavigateBack()
                    }
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    modifier = Modifier.statusBarsPadding(),
                    onClick = {
                        onDrawerClick()
                    }
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_menu),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if(bookWriterState.contentList.isNotEmpty()) {
                    IconButton(
                        modifier = Modifier.statusBarsPadding(),
                        onClick = {
                            bookWriterViewModel.onAction(BookWriterAction.SaveChapter(contentState.currentChapterIndex))
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_save),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        focusManager.clearFocus()
                        bookWriterViewModel.onAction(
                            BookWriterAction.UpdateItemMenuVisible(
                                "",
                                false
                            )
                        )
                    }
                ),
        ) {
            itemsIndexed(
                items = bookWriterState.contentList,
                key = { _, item -> item.id }
            ) { index, paragraph ->
                ComponentContainer(
                    state = paragraph.richTextState,
                    bookWriterState = bookWriterState,
                    paragraph = paragraph,
                    onAddAbove = { type ->
                        bookWriterViewModel.onAction(
                            BookWriterAction.AddParagraphAbove(
                                paragraph.id,
                                if (type == ParagraphType.PARAGRAPH) {
                                    Paragraph(
                                        type = type,
                                        richTextState = RichTextState().apply {
                                            toggleParagraphStyle(ParagraphStyle(textIndent = TextIndent(20.sp)))
                                        },
                                    )
                                } else {
                                    Paragraph(
                                        type = type,
                                        richTextState = RichTextState(),
                                    )
                                }
                            )
                        )
                    },
                    onAddBelow = { type ->
                        bookWriterViewModel.onAction(
                            BookWriterAction.AddParagraphBelow(
                                paragraph.id,
                                if (type == ParagraphType.PARAGRAPH) {
                                    Paragraph(
                                        type = type,
                                        richTextState = RichTextState().apply {
                                            toggleParagraphStyle(ParagraphStyle(textIndent = TextIndent(20.sp)))
                                        },
                                    )
                                } else {
                                    Paragraph(
                                        type = type,
                                        richTextState = RichTextState(),
                                    )
                                }
                            )
                        )
                    },
                    onDelete = {
                        bookWriterViewModel.onAction(BookWriterAction.DeleteParagraph(paragraph.id))
                    },
                    onMoveUp = {
                        bookWriterViewModel.onAction(BookWriterAction.MoveParagraphUp(paragraph.id))
                        bookWriterViewModel.onAction(BookWriterAction.SetFocusTarget(""))
                    },
                    onMoveDown = {
                        bookWriterViewModel.onAction(BookWriterAction.MoveParagraphDown(paragraph.id))
                        bookWriterViewModel.onAction(BookWriterAction.SetFocusTarget(""))
                    },
                    onVisibilityChange = { isVisible ->
                        bookWriterViewModel.onAction(
                            BookWriterAction.UpdateItemMenuVisible(
                                paragraph.id,
                                isVisible
                            )
                        )
                    },
                    focusedItem = {
                        bookWriterViewModel.onAction(BookWriterAction.UpdateSelectedItem(paragraph.id))
                    },
                    onFocusRequestedAndCleared = {
                        bookWriterViewModel.onAction(BookWriterAction.SetFocusTarget(""))
                    },
                    onImageSelected = { uri ->
                        bookWriterViewModel.onAction(
                            BookWriterAction.AddImage(
                                context = context,
                                chapterIndex = contentState.currentChapterIndex,
                                paragraphIndex = index,
                                paragraphId = paragraph.id,
                                imageUri = uri
                            )
                        )
                    }
                )
            }
        }
    }
}
