package com.capstone.bookshelf.feature.importbook.di

import com.capstone.bookshelf.feature.importbook.presentation.component.ImportBookViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val importBookViewModelModule = module {
    viewModel { ImportBookViewModel(get()) }
}
