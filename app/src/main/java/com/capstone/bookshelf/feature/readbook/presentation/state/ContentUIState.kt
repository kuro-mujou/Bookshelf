package com.capstone.bookshelf.feature.readbook.presentation.state

data class ContentUIState(
    val currentBookIndex : Int = 0,
    val currentChapterIndex: Int = 0,
    val currentParagraphIndex: Int = 0,
    val topBarState : Boolean = false,
    val bottomBarState: Boolean = false,
    val drawerState : Boolean = false,
    val enableScaffoldBar : Boolean = true,
    val enablePagerScroll : Boolean = true,
    val screenHeight : Int = 0,
    val screenWidth : Int = 0
)
