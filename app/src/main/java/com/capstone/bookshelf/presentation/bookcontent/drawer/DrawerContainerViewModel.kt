package com.capstone.bookshelf.presentation.bookcontent.drawer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.capstone.bookshelf.app.Route
import com.capstone.bookshelf.data.database.entity.ChapterContentEntity
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.domain.repository.ChapterRepository
import com.capstone.bookshelf.domain.repository.ImagePathRepository
import com.capstone.bookshelf.domain.repository.TableOfContentRepository
import com.capstone.bookshelf.domain.wrapper.TableOfContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DrawerContainerViewModel(
    private val tableOfContentRepository: TableOfContentRepository,
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val imagePathRepository: ImagePathRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId = savedStateHandle.toRoute<Route.BookContent>().bookId
    private val _state = MutableStateFlow(DrawerContainerState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )
    fun onAction(action: DrawerContainerAction) {
        when (action) {
            is DrawerContainerAction.UpdateDrawerState -> {
                _state.value = _state.value.copy(
                    drawerState = action.drawerState
                )
            }

            is DrawerContainerAction.UpdateCurrentTOC -> {
                val tocList = _state.value.tableOfContents
                if (action.toc in tocList.indices) {
                    _state.value = _state.value.copy(
                        currentTOC = tocList[action.toc]
                    )
                } else {
                    viewModelScope.launch {
                        val currentTOC = tableOfContentRepository.getTableOfContent(bookId, action.toc)
                        _state.value = _state.value.copy(
                            currentTOC = currentTOC
                        )
                    }
                }
            }

            is DrawerContainerAction.AddChapter -> {
                viewModelScope.launch {
                    val currentSize = _state.value.tableOfContents.size
                    val newChapter = TableOfContent(
                        bookId = bookId,
                        title = action.chapter,
                        index = currentSize
                    )
                    tableOfContentRepository.addChapter(bookId, newChapter)
                    bookRepository.saveBookInfoTotalChapter(bookId, currentSize + 1)
                    bookRepository.saveBookInfoChapterIndex(bookId, currentSize)
                    val newChapterContent = ChapterContentEntity(
                        bookId = bookId,
                        tocId = currentSize,
                        chapterTitle = action.chapter,
                        content = emptyList()
                    )
                    chapterRepository.saveChapterContent(newChapterContent)
                }
            }
        }
    }
    init {
        viewModelScope.launch {
            tableOfContentRepository
                .getTableOfContents(bookId)
                .collectLatest{ tableOfContents ->
                    _state.update { it.copy(
                        tableOfContents = tableOfContents
                    ) }
                }
        }
    }
}