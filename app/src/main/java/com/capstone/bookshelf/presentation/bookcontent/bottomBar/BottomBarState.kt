package com.capstone.bookshelf.presentation.bookcontent.bottomBar

data class BottomBarState(
    val visibility: Boolean = false,
    val bottomBarDefaultState: Boolean = true,
    val bottomBarAutoScrollState: Boolean = false,
    val bottomBarSettingState: Boolean = false,
    val bottomBarTTSState: Boolean = false,
    val bottomBarThemeState: Boolean = false,
    val openTTSVoiceMenu: Boolean = false,
    val openAutoScrollMenu: Boolean = false,
    val openSetting: Boolean = false,
    val currentChapterHeader: String? = "test",
)