package com.capstone.bookshelf.presentation.bookcontent.drawer

import com.capstone.bookshelf.domain.wrapper.Note
import com.capstone.bookshelf.domain.wrapper.TableOfContent

sealed interface DrawerContainerAction {
    data class UpdateDrawerState(val drawerState: Boolean) : DrawerContainerAction
    data class UpdateCurrentTOC(val toc: Int) : DrawerContainerAction
    data class DeleteTocItem(val tocItem: TableOfContent) : DrawerContainerAction
    data class UpdateIsFavorite(val isFavorite: Boolean) : DrawerContainerAction
    data class DeleteBookmark(val tocId: Int) : DrawerContainerAction
    data class AddNote(val noteBody: String, val noteInput: String, val tocId: Int, val contentId: Int) : DrawerContainerAction
    data class EditNote(val note: Note, val newInput: String) : DrawerContainerAction
    data class UpdateSelectedNote(val index: Int) : DrawerContainerAction
    data class DeleteNote(val note: Note) : DrawerContainerAction
    data object UndoDeleteNote : DrawerContainerAction
    data object DisableUndoDeleteNote : DrawerContainerAction
    data object UndoDeleteBookmark : DrawerContainerAction
    data object DisableUndoDeleteBookmark : DrawerContainerAction
}