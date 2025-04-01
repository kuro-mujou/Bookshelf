package com.capstone.bookshelf.presentation.bookwriter

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import com.capstone.bookshelf.presentation.bookwriter.component.ComponentContainer
import com.capstone.bookshelf.presentation.bookwriter.component.EditorControls
import com.capstone.bookshelf.presentation.bookwriter.component.Paragraph
import com.capstone.bookshelf.presentation.bookwriter.component.ParagraphType
import com.capstone.bookshelf.util.move
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@SuppressLint("SdCardPath")
@UnstableApi
@Composable
fun BookWriterEdit(
    bookWriterViewModel: BookWriterViewModel,
    contentViewModel: ContentViewModel,
    drawerContainerState: DrawerContainerState,
    contentState: ContentState
){
    val bookWriterState by bookWriterViewModel.bookWriterState.collectAsStateWithLifecycle()
    val chapterContent by contentViewModel.chapterContent
    val isImeVisible = WindowInsets.isImeVisible
    val contentList = remember { mutableStateListOf<Paragraph>() }
    val focusRequesters = remember { mutableStateListOf(FocusRequester()) }
    val lazyListState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    LaunchedEffect(contentState.currentChapterIndex) {
        contentList.clear()
        contentList.addAll(
            chapterContent?.content?.map {
                if(bookWriterState.linkPattern.containsMatchIn(it)){
                    Paragraph(
                        text = it,
                        type = ParagraphType.IMAGE
                    )
                } else if(bookWriterState.headerPattern.containsMatchIn(it)) {
                    if (bookWriterState.htmlTagPattern.replace(it, replacement = "").isNotEmpty()) {
                        Paragraph(
                            text = it,
                            type = ParagraphType.TITLE,
                            headerLevel = bookWriterState.headerLevel.find(it)?.groupValues[1]?.toInt()
                        )
                    } else{
                        Paragraph(
                            text = it,
                            type = ParagraphType.SUBTITLE,
                            headerLevel = 4
                        )
                    }
                } else {
                    Paragraph(
                        text = it,
                        type = ParagraphType.PARAGRAPH
                    )
                }
            }?:emptyList()
        )
        focusRequesters.clear()
        focusRequesters.addAll(List(contentList.size){FocusRequester()})
    }
    LaunchedEffect(isImeVisible) {
        if(!isImeVisible){
            bookWriterViewModel.onAction(BookWriterAction.UpdateSelectedItem(-1))
            focusManager.clearFocus()
            bookWriterViewModel.onAction(BookWriterAction.UpdateBoldState(false))
            bookWriterViewModel.onAction(BookWriterAction.UpdateItalicState(false))
            bookWriterViewModel.onAction(BookWriterAction.UpdateUnderlineState(false))
            bookWriterViewModel.onAction(BookWriterAction.UpdateStrikethroughState(false))
            bookWriterViewModel.onAction(BookWriterAction.UpdateAlignState(1))
        }
    }
    LaunchedEffect(bookWriterState.selectedItem){
        if(bookWriterState.selectedItem >= 0 && !bookWriterState.addingState){
            lazyListState.animateScrollToItem(bookWriterState.selectedItem)
        }
    }
    LaunchedEffect(bookWriterState.addingState){
        if(bookWriterState.addingState) {
            try {
                if (bookWriterState.addIndex < bookWriterState.selectedItem) {
                    if (bookWriterState.addIndex < 1) {
                        focusRequesters.add(1, FocusRequester())
                        contentList.add(
                            1,
                            when (bookWriterState.addType) {
                                ParagraphType.IMAGE -> {
                                    Paragraph(text = "", type = ParagraphType.IMAGE)
                                }

                                ParagraphType.SUBTITLE -> {
                                    Paragraph(
                                        text = "",
                                        type = ParagraphType.SUBTITLE,
                                        headerLevel = 4
                                    )
                                }
                                else -> {
                                    Paragraph(text = "test text", type = ParagraphType.PARAGRAPH)
                                }
                            }
                        )
                        lazyListState.animateScrollToItem(1)
                        delay(300)
                        if (bookWriterState.addType != ParagraphType.IMAGE) {
                            focusRequesters[1].requestFocus()
                        } else {
                            focusManager.clearFocus()
                        }
                    } else {
                        focusRequesters.add(bookWriterState.selectedItem, FocusRequester())
                        contentList.add(
                            bookWriterState.selectedItem,
                            when (bookWriterState.addType) {
                                ParagraphType.IMAGE -> {
                                    Paragraph(text = "", type = ParagraphType.IMAGE)
                                }

                                ParagraphType.SUBTITLE -> {
                                    Paragraph(
                                        text = "",
                                        type = ParagraphType.SUBTITLE,
                                        headerLevel = 4
                                    )
                                }

                                else -> {
                                    Paragraph(text = "", type = ParagraphType.PARAGRAPH)
                                }
                            }
                        )
                        lazyListState.animateScrollToItem(bookWriterState.selectedItem)
                        delay(300)
                        if (bookWriterState.addType != ParagraphType.IMAGE) {
                            focusRequesters[bookWriterState.selectedItem].requestFocus()
                        } else {
                            focusManager.clearFocus()
                        }
                    }
                } else if (bookWriterState.addIndex > bookWriterState.selectedItem) {
                    focusRequesters.add(bookWriterState.addIndex, FocusRequester())
                    contentList.add(
                        bookWriterState.addIndex,
                        when (bookWriterState.addType) {
                            ParagraphType.IMAGE -> {
                                Paragraph(text = "", type = ParagraphType.IMAGE)
                            }

                            ParagraphType.SUBTITLE -> {
                                Paragraph(text = "", type = ParagraphType.SUBTITLE, headerLevel = 4)
                            }

                            else -> {
                                Paragraph(text = "", type = ParagraphType.PARAGRAPH)
                            }
                        }
                    )
                    lazyListState.animateScrollToItem(bookWriterState.addIndex)
                    delay(300)
                    if (bookWriterState.addType != ParagraphType.IMAGE) {
                        focusRequesters[bookWriterState.addIndex].requestFocus()
                    } else {
                        focusManager.clearFocus()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                bookWriterViewModel.onAction(BookWriterAction.UpdateAddIndex(-1))
                bookWriterViewModel.onAction(BookWriterAction.UpdateAddType(ParagraphType.NONE))
                bookWriterViewModel.onAction(BookWriterAction.UpdateAddingState(false))
            }
        }
    }
    LaunchedEffect(bookWriterState.triggerScroll){
        if(bookWriterState.triggerScroll){
            lazyListState.animateScrollBy(with(density){24.sp.toPx()})
            bookWriterViewModel.onAction(BookWriterAction.UpdateTriggerScroll(false))
        }
    }
    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .imePadding()
            .fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(color = Color(91, 72, 0, 255))
            ) {
                EditorControls(
                    bookWriterState = bookWriterState,
                    onBoldClick = {
                        bookWriterViewModel.onAction(BookWriterAction.ToggleBold)
                    },
                    onItalicClick = {
                        bookWriterViewModel.onAction(BookWriterAction.ToggleItalic)
                    },
                    onUnderlineClick = {
                        bookWriterViewModel.onAction(BookWriterAction.ToggleUnderline)
                    },
                    onStrikethroughClick = {
                        bookWriterViewModel.onAction(BookWriterAction.ToggleStrikethrough)
                    },
                    onAlignClick = {
                        bookWriterViewModel.onAction(BookWriterAction.ToggleAlign)
                    }
                )
                Spacer(modifier = Modifier.navigationBarsPadding())
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
                        contentList.forEachIndexed { index, paragraph ->
                            contentList[index] = paragraph.copy(isControllerVisible = false)
                        }
                    }
                ),
        ) {
            itemsIndexed(
                items = contentList,
                key = { _, item -> item.id }
            ) { index, paragraph ->
                ComponentContainer(
                    bookWriterState = bookWriterState,
                    paragraph = paragraph,
                    index = index,
                    focusRequester = focusRequesters.getOrNull(index) ?: FocusRequester(),
                    onSizeChange = { index ->
                        if(isImeVisible && !bookWriterState.addingState && index == bookWriterState.selectedItem) {
                            bookWriterViewModel.onAction(BookWriterAction.UpdateTriggerScroll(true))
                        }
                    },
                    onAdd = { selectedItemIndex, newIndex, type ->
                        bookWriterViewModel.onAction(BookWriterAction.UpdateSelectedItem(selectedItemIndex))
                        bookWriterViewModel.onAction(BookWriterAction.UpdateAddType(type))
                        bookWriterViewModel.onAction(BookWriterAction.UpdateAddIndex(newIndex))
                        bookWriterViewModel.onAction(BookWriterAction.UpdateAddingState(true))
                    },
                    onDelete = {
                        focusManager.clearFocus()
                        contentList.removeAt(it)
                        focusRequesters.removeAt(it)
                    },
                    onEditing = { newText ->
                        contentList[index] = paragraph.copy(text = newText)
                    },
                    onMoveUp = {
                        if (it - 1 >= 1) {
                            contentList.move(it, it - 1)
                            focusRequesters.move(it, it - 1)
                        }
                    },
                    onMoveDown = {
                        if (it + 1 <= contentList.size) {
                            contentList.move(it, it + 1)
                            focusRequesters.move(it, it + 1)
                        }
                    },
                    onVisibilityChange = { idx, isVisible ->
                        contentList.forEachIndexed { i, paragraph ->
                            contentList[i] =
                                paragraph.copy(isControllerVisible = (i == idx && isVisible))
                        }
                    },
                    focusedItem = {
                        bookWriterViewModel.onAction(BookWriterAction.UpdateSelectedItem(it))
                    }
                )
            }
        }
    }
}
