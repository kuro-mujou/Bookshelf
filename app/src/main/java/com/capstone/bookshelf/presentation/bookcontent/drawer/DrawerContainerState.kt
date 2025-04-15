package com.capstone.bookshelf.presentation.bookcontent.drawer

import com.capstone.bookshelf.domain.wrapper.TableOfContent

data class DrawerContainerState (
    val drawerState : Boolean = false,
    val tableOfContents : List<TableOfContent> = emptyList(),
    val currentTOC : TableOfContent? = null,
    var undoList : List<Int> = emptyList(),
    val enableUndo : Boolean = false,
)