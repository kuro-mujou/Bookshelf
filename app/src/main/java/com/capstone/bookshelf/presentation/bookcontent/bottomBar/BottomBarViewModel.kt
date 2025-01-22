package com.capstone.bookshelf.presentation.bookcontent.bottomBar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class BottomBarViewModel : ViewModel(){
    private val _state = MutableStateFlow(BottomBarState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )
    fun onAction(action: BottomBarAction) {
        when(action) {
            is BottomBarAction.UpdateVisibility -> {
                _state.update { it.copy(
                    visibility = action.visibility
                ) }
            }
            is BottomBarAction.UpdateBottomBarDefaultState -> {
                _state.update { it.copy(
                    bottomBarDefaultState = action.default
                ) }
            }
            is BottomBarAction.UpdateBottomBarAutoScrollState -> {
                _state.update { it.copy(
                    bottomBarAutoScrollState = action.autoScroll
                ) }
            }
            is BottomBarAction.UpdateBottomBarSettingState -> {
                _state.update { it.copy(
                    bottomBarSettingState = action.setting
                ) }
            }
            is BottomBarAction.UpdateBottomBarTTSState -> {
                _state.update { it.copy(
                    bottomBarTTSState = action.tts
                ) }
            }
            is BottomBarAction.UpdateBottomBarThemeState -> {
                _state.update { it.copy(
                    bottomBarThemeState = action.theme
                ) }
            }

            is BottomBarAction.UpdateKeepScreenOn -> {
                _state.update { it.copy(
                    screenShallBeKeptOn = action.keepScreenOn
                ) }
            }

            is BottomBarAction.OpenVoiceMenuSetting -> {
                _state.update { it.copy(
                    openTTSVoiceMenu = action.open
                ) }
            }

            is BottomBarAction.OpenSetting -> {
                _state.update { it.copy(
                    openSetting = action.open
                ) }
            }

            is BottomBarAction.OpenAutoScrollMenu -> {
                _state.update { it.copy(
                    openAutoScrollMenu = action.open
                ) }
            }
        }
    }
}