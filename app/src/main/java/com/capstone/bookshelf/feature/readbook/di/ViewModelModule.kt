package com.capstone.bookshelf.feature.readbook.di

import com.capstone.bookshelf.feature.readbook.presentation.BookContentViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val BookContentViewModelModule = module {
    viewModel { BookContentViewModel(get(), get()) }
}
