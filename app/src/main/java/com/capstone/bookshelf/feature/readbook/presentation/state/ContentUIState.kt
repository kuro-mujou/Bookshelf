package com.capstone.bookshelf.feature.readbook.presentation.state

data class ContentUIState(
    val screenShallBeKeptOn : Boolean = false,
    val currentBookIndex : Int = 0,
    val currentChapterIndex: Int = 0,
    val currentParagraphIndex: Int = 0,
    val currentChapterHeader : String? = null,
    val currentChapterContent : List<String>? = null,
    val currentReadingPosition : Int = 0,
    val totalChapter : Int = 0,
    val topBarState : Boolean = false,
    val bottomBarState: Boolean = false,
    val bottomBarIndex : Int = 0,
    val drawerState : Boolean = false,
    val enableScaffoldBar : Boolean = true,
    val enablePagerScroll : Boolean = true,
    val screenHeight : Int = 0,
    val screenWidth : Int = 0,
    val openTTSVoiceMenu : Boolean = false,
    val openAutoScrollMenu : Boolean = false,
    val openSetting : Boolean = false,
)
