package com.capstone.bookshelf.feature.booklist.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.core.data.BookRepository
import com.capstone.bookshelf.core.domain.BookEntity
import com.capstone.bookshelf.core.util.RequestState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BookListViewModel(
    private val repository: BookRepository
) : ViewModel() {
    private var _sortedByFavorite = MutableStateFlow(false)

    private var _books: MutableState<RequestState<List<BookEntity>>> =
        mutableStateOf(RequestState.Loading)
    val books: State<RequestState<List<BookEntity>>> = _books

    init {
        viewModelScope.launch {
            _sortedByFavorite.collectLatest { favorite ->
                if (favorite) {
                    repository.readAllBooksSortByFavorite()
                        .collectLatest { sortedBooks ->
                            _books.value = RequestState.Success(
                                data = sortedBooks
                            )
                        }
                } else {
                    repository.readAllBooks()
                        .collectLatest { allBooks ->
                            _books.value = RequestState.Success(
                                data = allBooks
                            )
                        }
                }
            }
        }
    }
    fun toggleSortByFavorite(toggle: Boolean) {
        _sortedByFavorite.value = toggle
    }

    fun toggleFavorite(bookId: Int, isFavorite: Boolean) = viewModelScope.launch {
        repository.setBookAsFavorite(bookId, isFavorite)
    }
}