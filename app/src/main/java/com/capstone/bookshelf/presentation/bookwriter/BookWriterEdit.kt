package com.capstone.bookshelf.presentation.bookwriter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import com.capstone.bookshelf.presentation.bookwriter.component.AddChapterTitle
import com.capstone.bookshelf.presentation.bookwriter.component.AddParagraph
import java.util.UUID

@UnstableApi
@Composable
fun BookWriterEdit(
    contentViewModel: ContentViewModel,
    drawerContainerState: DrawerContainerState,
    contentState: ContentState
){
    val chapterContent by contentViewModel.chapterContent
    val lazyListState = rememberLazyListState()
    val emptyList = remember { mutableStateListOf<Paragraph>() }
    LaunchedEffect(contentState.currentChapterIndex) {
        emptyList.clear()
        emptyList.addAll(
            chapterContent?.content?.map {
                Paragraph(text = it)
            }?:emptyList()
        )
    }
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.systemBarsPadding()
        ){
            AddChapterTitle(
                drawerContainerState.currentTOC,
                onAddParagraph = {
                    emptyList.add(0, Paragraph(text = ""))
                }
            )
            LazyColumn(
                state = lazyListState
            ) {
                itemsIndexed(
                    items = emptyList,
                    key = { _, item -> item.id }
                ) { index, paragraph  ->
                    AddParagraph(
                        paragraph = paragraph.text,
                        index = index
                    )
                }
            }
        }
    }
}

data class Paragraph(val id: String = UUID.randomUUID().toString(), var text: String)
