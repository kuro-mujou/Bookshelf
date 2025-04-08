package com.capstone.bookshelf.presentation.bookcontent.topbar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class TopBarViewModel : ViewModel() {

    private val _state = MutableStateFlow(TopBarState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: TopBarAction) {
        when(action){
            is TopBarAction.UpdateVisibility -> {
                _state.update { it.copy(
                    visibility = action.visibility
                ) }
            }
        }
    }
}