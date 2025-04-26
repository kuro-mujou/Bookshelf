package com.capstone.bookshelf.presentation.bookcontent.drawer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.capstone.bookshelf.app.Route
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.domain.repository.ChapterRepository
import com.capstone.bookshelf.domain.repository.ImagePathRepository
import com.capstone.bookshelf.domain.repository.NoteRepository
import com.capstone.bookshelf.domain.repository.TableOfContentRepository
import com.capstone.bookshelf.domain.wrapper.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DrawerContainerViewModel(
    private val tableOfContentsRepository: TableOfContentRepository,
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val imagePathRepository: ImagePathRepository,
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val bookId = savedStateHandle.toRoute<Route.BookContent>().bookId
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm  dd-MM-yyyy")
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
                _state.update {
                    it.copy(
                        drawerState = action.drawerState
                    )
                }
            }

            is DrawerContainerAction.UpdateCurrentTOC -> {
                val tocList = _state.value.tableOfContents
                if (action.toc in tocList.indices) {
                    _state.update {
                        it.copy(
                            currentTOC = tocList[action.toc]
                        )
                    }
                } else {
                    viewModelScope.launch {
                        val currentTOC =
                            tableOfContentsRepository.getTableOfContent(bookId, action.toc)
                        _state.update {
                            it.copy(
                                currentTOC = currentTOC
                            )
                        }
                    }
                }
            }

            is DrawerContainerAction.UpdateIsFavorite -> {
                viewModelScope.launch {
                    tableOfContentsRepository.updateTableOfContentFavoriteStatus(
                        bookId,
                        _state.value.currentTOC!!.index,
                        action.isFavorite
                    )
                }
            }

            is DrawerContainerAction.DeleteBookmark -> {
                viewModelScope.launch {
                    tableOfContentsRepository.updateTableOfContentFavoriteStatus(
                        bookId,
                        action.tocId,
                        false
                    )
                }
                _state.value.undoBookmarkList += action.tocId
                _state.update {
                    it.copy(
                        enableUndoDeleteBookmark = true
                    )
                }
            }

            is DrawerContainerAction.UndoDeleteBookmark -> {
                viewModelScope.launch {
                    _state.value.undoBookmarkList.forEach {
                        tableOfContentsRepository.updateTableOfContentFavoriteStatus(
                            bookId,
                            it,
                            true
                        )
                    }
                    _state.update {
                        it.copy(
                            enableUndoDeleteBookmark = false
                        )
                    }
                    _state.value.undoBookmarkList = emptyList()
                }
            }

            is DrawerContainerAction.DisableUndoDeleteBookmark -> {
                _state.update {
                    it.copy(
                        enableUndoDeleteBookmark = false
                    )
                }
                _state.value.undoBookmarkList = emptyList()
            }

            is DrawerContainerAction.AddNote -> {
                viewModelScope.launch {
                    noteRepository.upsertNote(
                        Note(
                            bookId = bookId,
                            tocId = action.tocId,
                            contentId = action.contentId,
                            noteBody = action.noteBody,
                            noteInput = action.noteInput,
                            timestamp = LocalDateTime.now().format(dateTimeFormatter)
                        )
                    )
                }
            }

            is DrawerContainerAction.EditNote -> {
                viewModelScope.launch {
                    noteRepository.upsertNote(
                        Note(
                            noteId = action.note.noteId,
                            bookId = action.note.bookId,
                            tocId = action.note.tocId,
                            contentId = action.note.contentId,
                            noteBody = action.note.noteBody,
                            noteInput = action.newInput,
                            timestamp = LocalDateTime.now().format(dateTimeFormatter)
                        )
                    )
                }
            }

            is DrawerContainerAction.DeleteNote -> {
                viewModelScope.launch {
                    noteRepository.deleteNote(action.note.noteId)
                }
                _state.value.undoNoteList += action.note
                _state.update {
                    it.copy(
                        enableUndoDeleteNote = true,
                        currentSelectedNote = -1
                    )
                }
            }

            is DrawerContainerAction.UpdateSelectedNote -> {
                _state.update {
                    it.copy(
                        currentSelectedNote = action.index
                    )
                }
            }

            is DrawerContainerAction.UndoDeleteNote -> {
                viewModelScope.launch {
                    _state.value.undoNoteList.forEach {
                        noteRepository.upsertNote(it)
                    }
                    _state.update {
                        it.copy(
                            enableUndoDeleteNote = false
                        )
                    }
                }
            }

            is DrawerContainerAction.DisableUndoDeleteNote -> {
                _state.update {
                    it.copy(
                        enableUndoDeleteNote = false
                    )
                }
                _state.value.undoNoteList = emptyList()
            }

            is DrawerContainerAction.DeleteTocItem -> {
                viewModelScope.launch {
                    tableOfContentsRepository.deleteTableOfContent(
                        action.tocItem.bookId,
                        action.tocItem.index
                    )
                    chapterRepository.deleteChapter(
                        action.tocItem.bookId,
                        action.tocItem.index
                    )
                    tableOfContentsRepository.updateTableOfContentIndexOnDelete(
                        action.tocItem.bookId,
                        action.tocItem.index
                    )
                    chapterRepository.updateChapterIndexOnDelete(
                        action.tocItem.bookId,
                        action.tocItem.index
                    )
                    bookRepository.updateCurrentChapterIndexOnDelete(
                        action.tocItem.bookId,
                        action.tocItem.index
                    )
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            tableOfContentsRepository
                .getTableOfContents(bookId)
                .collectLatest { tableOfContents ->
                    _state.update {
                        it.copy(
                            tableOfContents = tableOfContents
                        )
                    }
                }
        }
        viewModelScope.launch {
            noteRepository
                .getNotes(bookId)
                .collectLatest { notes ->
                    _state.update {
                        it.copy(
                            notes = notes
                        )
                    }
                }
        }
    }
}