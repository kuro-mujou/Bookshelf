package com.capstone.bookshelf.presentation.bookcontent.drawer

import com.capstone.bookshelf.domain.wrapper.Note
import com.capstone.bookshelf.domain.wrapper.TableOfContent

data class DrawerContainerState (
    val drawerState : Boolean = false,
    val tableOfContents : List<TableOfContent> = emptyList(),
    var notes : List<Note> = emptyList(),
    val currentTOC : TableOfContent? = null,
    var undoBookmarkList : List<Int> = emptyList(),
    var undoNoteList : List<Note> = emptyList(),
    val enableUndoDeleteBookmark : Boolean = false,
    val enableUndoDeleteNote : Boolean = false,
    val currentSelectedNote : Int = -1
)