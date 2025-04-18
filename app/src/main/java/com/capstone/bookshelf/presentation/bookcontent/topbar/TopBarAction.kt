package com.capstone.bookshelf.presentation.bookcontent.topbar

sealed interface TopBarAction {
    data class UpdateVisibility(val visibility: Boolean) : TopBarAction
}