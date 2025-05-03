package com.capstone.bookshelf.presentation.home_screen.booklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.domain.repository.ImagePathRepository
import com.capstone.bookshelf.util.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.File

class BookListViewModel(
    private val bookRepository: BookRepository,
    private val imagePathRepository: ImagePathRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _state = MutableStateFlow(BookListState())
    val state = _state
        .onStart {
            observeBookSetting()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    init {
        viewModelScope.launch {
            _state.collectLatest { favorite ->
                if (favorite.isSortedByFavorite) {
                    bookRepository.readAllBooksSortByFavorite()
                        .collectLatest { sortedBooks ->
                            _state.update {
                                it.copy(
                                    bookList = sortedBooks
                                )
                            }
                        }
                } else {
                    bookRepository.readAllBooks()
                        .collectLatest { sortedBooks ->
                            _state.update {
                                it.copy(
                                    bookList = sortedBooks
                                )
                            }
                        }
                }
            }
        }
    }

    fun onAction(action: BookListAction) {
        when (action) {
            is BookListAction.OnBookClick -> {
                _state.update {
                    it.copy(
                        selectedBook = action.book
                    )
                }
            }

            is BookListAction.OnBookLongClick -> {
                _state.update {
                    it.copy(
                        selectedBook = action.book,
                        isOpenBottomSheet = action.isOpenBottomSheet
                    )
                }
            }

            is BookListAction.OnBookBookmarkClick -> {
                viewModelScope.launch {
                    bookRepository.setBookAsFavorite(action.book.id, !action.book.isFavorite)
                }
            }

            is BookListAction.OnBookDeleteClick -> {
                viewModelScope.launch {
                    bookRepository.deleteBooks(listOf(action.book))
                    processDeleteImages(listOf(action.book.id))
                    _state.update {
                        it.copy(
                            isOpenBottomSheet = false
                        )
                    }
                }
            }

            is BookListAction.OnBookListBookmarkClick -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            isSortedByFavorite = action.isSortListByFavorite
                        )
                    }
                    dataStoreManager.setSortByFavorite(action.isSortListByFavorite)
                }
            }

            is BookListAction.OnViewBookDetailClick -> {
                _state.update {
                    it.copy(
                        isOpenBottomSheet = false,
                        selectedBook = action.book
                    )
                }
            }

            is BookListAction.OnBookCheckBoxClick -> {
                if (action.checked) {
                    _state.update {
                        it.copy(
                            selectedBookList = _state.value.selectedBookList + action.book
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            selectedBookList = _state.value.selectedBookList - action.book
                        )
                    }
                }
            }

            is BookListAction.OnDeletingBooks -> {
                _state.update {
                    it.copy(
                        isOnDeleteBooks = action.deleteState
                    )
                }
            }

            is BookListAction.OnConfirmDeleteBooks -> {
                viewModelScope.launch {
                    bookRepository.deleteBooks(_state.value.selectedBookList)
                    yield()
                    processDeleteImages(_state.value.selectedBookList.map { it.id })
                    _state.update {
                        it.copy(
                            selectedBookList = emptyList(),
                            isOnDeleteBooks = false
                        )
                    }
                }
            }

            is BookListAction.UpdateBookListType -> {
                if(action.type != null){
                    _state.update {
                        it.copy(
                            listViewType = action.type
                        )
                    }
                    viewModelScope.launch {
                        dataStoreManager.setBookListView(action.type)
                    }
                } else {
                    if(_state.value.listViewType == 1){
                        _state.update {
                            it.copy(
                                listViewType = 0
                            )
                        }
                        viewModelScope.launch {
                            dataStoreManager.setBookListView(0)
                        }
                    } else if(_state.value.listViewType == 0) {
                        _state.update {
                            it.copy(
                                listViewType = 1
                            )
                        }
                        viewModelScope.launch {
                            dataStoreManager.setBookListView(1)
                        }
                    }
                }
            }
        }
    }

    private fun processDeleteImages(bookIds: List<String>) {
        viewModelScope.launch {
            val imagePaths = imagePathRepository.getImagePathsByBookId(bookIds)
            for (imagePathEntity in imagePaths) {
                val file = File(imagePathEntity.imagePath)
                if (file.exists()) {
                    file.delete()
                }
            }
            imagePathRepository.deleteByBookId(bookIds)
        }
    }

    private fun observeBookSetting() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSortedByFavorite = dataStoreManager.isSortedByFavorite.first()
                )
            }
        }
    }
}