package com.capstone.bookshelf.presentation.bookcontent.component.autoscroll

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState

suspend fun slowScrollToBottom(
    pagerState: PagerState,
    listState: LazyListState,
    contentState: ContentState,
    speed: Int,
) {
    val totalItems = listState.layoutInfo.totalItemsCount
    while(true){
        listState.animateScrollBy(
            value = contentState.screenHeight.toFloat(),
            animationSpec = tween(
                durationMillis = speed,
                delayMillis = 0,
                easing = LinearEasing
            )
        )
        if(contentState.lastVisibleItemIndex == totalItems - 1){
            pagerState.animateScrollToPage(contentState.currentChapterIndex+1)
        }
    }
}