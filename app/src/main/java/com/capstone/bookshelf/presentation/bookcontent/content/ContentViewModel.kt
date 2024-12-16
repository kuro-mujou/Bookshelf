package com.capstone.bookshelf.presentation.bookcontent.content

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.capstone.bookshelf.app.Route
import com.capstone.bookshelf.domain.book.ChapterRepository
import com.capstone.bookshelf.domain.wrapper.Chapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ContentViewModel(
    private val chapterRepository: ChapterRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val bookId = savedStateHandle.toRoute<Route.BookContent>().bookId

    private val _chapterContent: MutableState<Chapter?> = mutableStateOf(null)
    val chapterContent: State<Chapter?> = _chapterContent
    private val _state = MutableStateFlow(ContentState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: ContentAction) {
        when(action){
            is ContentAction.UpdateFlagTriggerAdjustScroll -> {
                _state.value = _state.value.copy(
                    flagTriggerAdjustScroll = action.value
                )
            }

            is ContentAction.UpdateFlagStartScrolling -> {
                _state.value = _state.value.copy(
                    flagStartScrolling = action.value
                )
            }

            is ContentAction.UpdateFlagScrollAdjusted -> {
                _state.value = _state.value.copy(
                    flagScrollAdjusted = action.value
                )
            }

            is ContentAction.UpdateFlagStartAdjustScroll -> {
                _state.value = _state.value.copy(
                    flagStartAdjustScroll = action.value
                )
            }

            is ContentAction.UpdateFirstVisibleItemIndex -> {
                _state.value = _state.value.copy(
                    firstVisibleItemIndex = action.index
                )
            }

            is ContentAction.UpdateLastVisibleItemIndex -> {
                _state.value = _state.value.copy(
                    lastVisibleItemIndex = action.index
                )
            }

            is ContentAction.UpdateCurrentChapterIndex -> {
                _state.value = _state.value.copy(
                    currentChapterIndex = action.index
                )
            }

            is ContentAction.UpdateScreenHeight -> {
                _state.value = _state.value.copy(
                    screenHeight = action.value
                )
            }

            is ContentAction.UpdateScreenWidth -> {
                _state.value = _state.value.copy(
                    screenWidth = action.value
                )
            }
        }
    }
    suspend fun getChapter(page: Int) {
        _chapterContent.value = chapterRepository.getChapterContent(bookId, page)
    }
}