package com.capstone.bookshelf.presentation.bookcontent.bottomBar


sealed interface BottomBarAction {
    data class UpdateVisibility(val visibility: Boolean) : BottomBarAction
    data class UpdateBottomBarDefaultState(val default : Boolean) : BottomBarAction
    data class UpdateBottomBarAutoScrollState(val autoScroll : Boolean) : BottomBarAction
    data class UpdateBottomBarSettingState(val setting : Boolean) : BottomBarAction
    data class UpdateBottomBarTTSState(val tts : Boolean) : BottomBarAction
    data class UpdateBottomBarThemeState(val theme : Boolean) : BottomBarAction
    data class UpdateKeepScreenOn(val keepScreenOn : Boolean) : BottomBarAction
    data class OpenVoiceMenuSetting(val open : Boolean) : BottomBarAction
    data class OpenAutoScrollMenu(val open : Boolean) : BottomBarAction
    data class OpenSetting(val open : Boolean) : BottomBarAction
}