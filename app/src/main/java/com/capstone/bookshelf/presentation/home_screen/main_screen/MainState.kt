package com.capstone.bookshelf.presentation.home_screen.main_screen

import com.capstone.bookshelf.domain.wrapper.Book

data class MainState(
    val recentBooks: List<Book> = emptyList(),
)