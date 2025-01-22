package com.capstone.bookshelf.presentation.bookcontent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class BookContentRootViewModel : ViewModel() {
    private val _state = MutableStateFlow(BookContentRootState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: BookContentRootAction) {
        when(action) {
            is BookContentRootAction.SelectedBook -> {
                _state.update { it.copy(
                    book = action.book
                ) }
            }
        }
    }
}