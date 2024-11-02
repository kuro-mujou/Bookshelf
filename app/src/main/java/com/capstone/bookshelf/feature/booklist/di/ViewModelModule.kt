package com.capstone.bookshelf.feature.booklist.di

import com.capstone.bookshelf.feature.booklist.presentation.BookListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val bookListViewModelModule = module {
    viewModel { BookListViewModel(get()) }
}