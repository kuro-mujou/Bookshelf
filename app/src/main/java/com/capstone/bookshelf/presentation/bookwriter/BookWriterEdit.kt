package com.capstone.bookshelf.presentation.bookwriter

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import com.capstone.bookshelf.presentation.bookwriter.component.BottomBar
import com.capstone.bookshelf.presentation.bookwriter.component.ComponentContainer
import com.capstone.bookshelf.presentation.bookwriter.component.Paragraph
import com.capstone.bookshelf.presentation.bookwriter.component.ParagraphType
import com.capstone.bookshelf.util.move
import com.capstone.bookshelf.util.rememberKeyboardAsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val chapterContent by contentViewModel.chapterContent
    val isKeyboardVisible by rememberKeyboardAsState()
    val contentList = remember { mutableStateListOf<Paragraph>() }
    val focusRequesters = remember { mutableStateListOf(FocusRequester()) }
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val context = LocalContext.current
    val linkPattern = Regex("""/data/user/0/com\.capstone\.bookshelf/files/[^ ]*""")
    val headerPattern = Regex("""<h([1-6])[^>]*>(.*?)</h([1-6])>""")
    val headerLevel = Regex("""<h([1-6])>.*?</h\1>""")
    val htmlTagPattern = Regex(pattern = """<[^>]+>""")
    var onAdding by remember { mutableStateOf(false) }

    LaunchedEffect(contentState.currentChapterIndex) {
        contentList.clear()
        contentList.addAll(
            chapterContent?.content?.map {
                if(linkPattern.containsMatchIn(it)){
                    Paragraph(
                        text = it,
                        type = ParagraphType.IMAGE
                    )
                } else if(headerPattern.containsMatchIn(it)) {
                    if (htmlTagPattern.replace(it, replacement = "").isNotEmpty()) {
                        Paragraph(
                            text = it,
                            type = ParagraphType.TITLE,
                            headerLevel = headerLevel.find(it)?.groupValues[1]?.toInt()
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
    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible) {
            focusManager.clearFocus()
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
                BottomBar()
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
                        keyboardController?.hide()
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
                    paragraph = paragraph,
                    index = index,
                    focusRequester = focusRequesters.getOrNull(index) ?: FocusRequester(),
                    onSizeChange = {
                        if(isKeyboardVisible && !onAdding) {
                            scope.launch {
                                lazyListState.animateScrollBy(with(density){24.sp.toPx()})
                            }
                        }
                    },
                    onAdd = { selectedItemIndex, newIndex ->
                        if (newIndex < selectedItemIndex) {
                            if (newIndex < 0) {
                                scope.launch {
                                    onAdding = true
                                    focusRequesters.add(0, FocusRequester())
                                    contentList.add(
                                        0,
                                        Paragraph(text = "", type = ParagraphType.PARAGRAPH)
                                    )
                                    lazyListState.animateScrollToItem(0)
                                    delay(300)
                                    focusRequesters[0].requestFocus()
                                    onAdding = false
                                }
                            } else {
                                scope.launch {
                                    onAdding = true
                                    focusRequesters.add(selectedItemIndex, FocusRequester())
                                    contentList.add(
                                        selectedItemIndex,
                                        Paragraph(text = "", type = ParagraphType.PARAGRAPH)
                                    )
                                    lazyListState.animateScrollToItem(selectedItemIndex)
                                    delay(300)
                                    focusRequesters[selectedItemIndex].requestFocus()
                                    onAdding = false
                                }
                            }
                        } else if (newIndex > selectedItemIndex) {
                            scope.launch {
                                onAdding = true
                                focusRequesters.add(newIndex, FocusRequester())
                                contentList.add(
                                    newIndex,
                                    Paragraph(text = "", type = ParagraphType.PARAGRAPH)
                                )
                                lazyListState.animateScrollToItem(newIndex)
                                delay(300)
                                focusRequesters[newIndex].requestFocus()
                                onAdding = false
                            }
                        }
                    },
                    onDelete = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
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
                        if(!onAdding) {
                            scope.launch {
                                lazyListState.animateScrollToItem(it)
                            }
                        }
                    }
                )
            }
        }
    }
}
