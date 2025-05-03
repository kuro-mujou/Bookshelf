package com.capstone.bookshelf.presentation.home_screen.main_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.domain.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MainState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            _state.value
        )

    init {
        viewModelScope.launch {
            bookRepository.getBookListForMainScreen()
                .collectLatest {
                    _state.value = state.value.copy(
                        recentBooks = it
                    )
                }
        }
    }
}