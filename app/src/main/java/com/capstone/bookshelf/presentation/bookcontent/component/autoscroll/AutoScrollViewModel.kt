package com.capstone.bookshelf.presentation.bookcontent.component.autoscroll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

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
                _state.update {  it.copy(
                    isStart = action.isAutoScroll
                ) }
//                _state.value = _state.value.copy(
//                    isStart = action.isAutoScroll
//                )
            }
            is AutoScrollAction.UpdateAutoScrollSpeed -> {
                _state.update { it.copy(
                    currentSpeed = action.autoScrollSpeed
                ) }
//                _state.value = _state.value.copy(
//                    currentSpeed = action.autoScrollSpeed
//                )
            }
            is AutoScrollAction.UpdateIsPaused -> {
                _state.update { it.copy(
                    isPaused = action.isPaused
                ) }
//                _state.value = _state.value.copy(
//                    isPaused = action.isPaused
//                )
            }
            is AutoScrollAction.UpdateDelayAtEnd -> {
                _state.update { it.copy(
                    delayAtEnd = action.delayAtEnd
                ) }
//                _state.value = _state.value.copy(
//                    delayAtEnd = action.delayAtEnd
//                )
            }
            is AutoScrollAction.UpdateDelayAtStart -> {
                _state.update { it.copy(
                    delayAtStart = action.delayAtStart
                ) }
//                _state.value = _state.value.copy(
//                    delayAtStart = action.delayAtStart
//                )
            }
            is AutoScrollAction.UpdateAutoResumeScrollMode -> {
                _state.update { it.copy(
                    isAutoResumeScrollMode = action.autoResumeScrollMode
                ) }
//                _state.value = _state.value.copy(
//                    isAutoResumeScrollMode = action.autoResumeScrollMode
//                )
            }
            is AutoScrollAction.UpdateDelayResume -> {
                _state.update { it.copy(
                    delayResumeMode = action.delayResume
                ) }
//                _state.value = _state.value.copy(
//                    delayResumeMode = action.delayResume
//                )
            }
            is AutoScrollAction.UpdateStopAutoScroll -> {
                _state.update { it.copy(
                    stopAutoScroll = action.stopScroll
                ) }
//                _state.value = _state.value.copy(
//                    stopAutoScroll = action.stopScroll
//                )
            }
        }
    }
}