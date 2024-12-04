package com.capstone.bookshelf.presentation.main.booklist

import com.capstone.bookshelf.core.presentation.UiText

data class LibraryState(
    val selectedTabIndex: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: UiText? = null
)
