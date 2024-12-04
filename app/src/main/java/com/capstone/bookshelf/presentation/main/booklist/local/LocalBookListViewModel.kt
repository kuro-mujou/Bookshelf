package com.capstone.bookshelf.presentation.main.booklist.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.domain.book.BookRepository
import com.capstone.bookshelf.domain.book.ImagePathRepository
import com.capstone.bookshelf.domain.setting.SettingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.File

class LocalBookListViewModel(
    private val bookRepository: BookRepository,
    private val settingRepository: SettingRepository,
    private val imagePathRepository: ImagePathRepository
) : ViewModel() {
    private val _state = MutableStateFlow(LocalBookListState())
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
                            _state.update { it.copy(
                                bookList = sortedBooks
                            ) }
                        }
                } else {
                    bookRepository.readAllBooks()
                        .collectLatest { sortedBooks ->
                            _state.update { it.copy(
                                bookList = sortedBooks
                            ) }
                        }
                }
            }
        }
    }
    fun onAction(action: LocalBookListAction) {
        when (action) {
            is LocalBookListAction.OnBookClick -> {

            }
            is LocalBookListAction.OnBookLongClick -> {
                _state.update {
                    it.copy(
                        selectedBook = action.book,
                        isOpenBottomSheet = action.isOpenBottomSheet
                    )
                }
            }
            is LocalBookListAction.OnBookBookmarkClick -> {
                viewModelScope.launch {
                    bookRepository.setBookAsFavorite(action.book.id, !action.book.isFavorite)
                }
            }
            is LocalBookListAction.OnBookDeleteClick -> {
                viewModelScope.launch {
                    bookRepository.deleteBooks(listOf(action.book))
                    processDeleteImages(listOf(action.book.id))
                }
            }
            is LocalBookListAction.OnBookListBookmarkClick -> {
                viewModelScope.launch {
                    settingRepository.updateLocalBookListFavourite(0,action.isSortListByFavorite)
                    _state.update {
                        it.copy(
                            isSortedByFavorite = action.isSortListByFavorite
                        )
                    }
                }
            }
            is LocalBookListAction.OnViewBookDetailClick -> {

            }
            is LocalBookListAction.OnSaveBook -> {
                _state.update {
                    it.copy(
                        isSavingBook = action.save
                    )
                }
            }

            is LocalBookListAction.OnBookCheckBoxClick -> {
                if(action.checked){
                    _state.update {
                        it.copy(
                            selectedBookList = _state.value.selectedBookList + action.book
                        )
                    }
                }else{
                    _state.update {
                        it.copy(
                            selectedBookList = _state.value.selectedBookList - action.book
                        )
                    }
                }
            }

            is LocalBookListAction.OnDeletingBooks -> {
                _state.update {
                    it.copy(
                        isOnDeleteBooks = action.deleteState
                    )
                }
            }

            is LocalBookListAction.OnConfirmDeleteBooks -> {
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

    private fun observeBookSetting(){
        viewModelScope.launch {
            val setting = settingRepository.getSetting(0)
            if(setting != null){
                _state.update {
                    it.copy(
                        isSortedByFavorite = setting.localBookListFavourite
                    )
                }
            }
        }
    }
}