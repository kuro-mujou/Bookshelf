package com.capstone.bookshelf.presentation.bookdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class BookDetailViewModel: ViewModel() {

    private val _state = MutableStateFlow(BookDetailState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: BookDetailAction) {
        when(action) {
            is BookDetailAction.OnSelectedBookChange -> {
                _state.update { it.copy(
                    book = action.book
                ) }
            }
        }
    }
}