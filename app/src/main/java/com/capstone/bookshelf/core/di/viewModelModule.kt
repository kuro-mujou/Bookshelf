package com.capstone.bookshelf.core.di

import com.capstone.bookshelf.core.presentation.SettingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingViewModelModule = module {
    viewModel { SettingViewModel(get()) }
}