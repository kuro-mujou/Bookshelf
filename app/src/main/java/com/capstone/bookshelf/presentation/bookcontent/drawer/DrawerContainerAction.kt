package com.capstone.bookshelf.presentation.bookcontent.drawer

sealed interface DrawerContainerAction {
    data class UpdateDrawerState(val drawerState: Boolean) : DrawerContainerAction
    data class UpdateCurrentTOC(val toc: Int) : DrawerContainerAction
    data class AddChapter(val chapter: String, val headerSize: String) : DrawerContainerAction
}