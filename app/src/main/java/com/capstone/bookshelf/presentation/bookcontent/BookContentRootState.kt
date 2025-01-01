package com.capstone.bookshelf.presentation.bookcontent

import com.capstone.bookshelf.domain.wrapper.Book

data class BookContentRootState (
    val book: Book? = null,
    val enableScaffoldBar : Boolean = true,
    val enablePagerScroll : Boolean = true,

//    val currentBookIndex : Int = 0,
//    val currentParagraphIndex: Int = 0,
//    val currentChapterContent : List<String>? = null,
//    val isSelectedParagraph: Boolean = false,
//    val commentButtonClicked: Boolean = false,
)