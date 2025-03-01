package com.capstone.bookshelf.presentation.bookcontent.component.autoscroll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class AutoScrollViewModel : ViewModel() {
    private val _state = MutableStateFlow(AutoScrollState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: AutoScrollAction){
        when(action) {
            is AutoScrollAction.UpdateIsStart -> {
                _state.value = _state.value.copy(
                    isStart = action.isAutoScroll
                )
            }
            is AutoScrollAction.UpdateAutoScrollSpeed -> {
                _state.value = _state.value.copy(
                    currentSpeed = action.autoScrollSpeed
                )
            }
            is AutoScrollAction.UpdateIsPaused -> {
                _state.value = _state.value.copy(
                    isPaused = action.isPaused
                )
            }
            is AutoScrollAction.UpdateDelayAtEnd -> {
                _state.value = _state.value.copy(
                    delayAtEnd = action.delayAtEnd
                )
            }
            is AutoScrollAction.UpdateDelayAtStart -> {
                _state.value = _state.value.copy(
                    delayAtStart = action.delayAtStart
                )
            }
            is AutoScrollAction.UpdateAutoResumeScrollMode -> {
                _state.value = _state.value.copy(
                    isAutoResumeScrollMode = action.autoResumeScrollMode
                )
            }
            is AutoScrollAction.UpdateDelayResume -> {
                _state.value = _state.value.copy(
                    delayResumeMode = action.delayResume
                )
            }
        }
    }
}