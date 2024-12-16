package com.capstone.bookshelf.presentation.bookcontent.content

import com.capstone.bookshelf.domain.wrapper.Chapter

data class ContentState(
    val currentChapterIndex: Int = 0,
    val totalChapter : Int = 0,
    val flagTriggerScrolling : Boolean = false,
    val flagStartScrolling : Boolean = false,
    val flagScrollAdjusted : Boolean = false,
    val flagTriggerAdjustScroll : Boolean = false,
    val flagStartAdjustScroll : Boolean = false,
    val firstVisibleItemIndex : Int = 0,
    val lastVisibleItemIndex : Int = 0,

    val screenHeight : Int = 0,
    val screenWidth : Int = 0,

    val chapter: Chapter? = null,
)