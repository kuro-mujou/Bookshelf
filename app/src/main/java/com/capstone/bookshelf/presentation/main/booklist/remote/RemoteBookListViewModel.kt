package com.capstone.bookshelf.presentation.main.booklist.remote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.domain.book.ImagePathRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RemoteBookListViewModel(
    private val imagePathRepository: ImagePathRepository
) : ViewModel() {
    private val _state = MutableStateFlow(RemoteBookListState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.collectLatest {
                imagePathRepository.getAllImage()
                    .collectLatest { images ->
                        _state.update { it1 ->
                            it1.copy(
                            imageUrl = images.map { it.imagePath }
                        ) }
                    }
            }
        }
    }
}