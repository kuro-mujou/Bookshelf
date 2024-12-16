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
            is AutoScrollAction.UpdateIsAutoScroll -> {
                _state.value = _state.value.copy(
                    isAutoScroll = action.isAutoScroll
                )
            }
        }
    }
}