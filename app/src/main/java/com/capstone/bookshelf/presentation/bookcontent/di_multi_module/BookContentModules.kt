package com.capstone.bookshelf.presentation.bookcontent.di_multi_module

import com.capstone.bookshelf.presentation.bookcontent.BookContentRootViewModel
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPaletteViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSViewModel
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerViewModel
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBarViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.font.FontViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val bookContentModule = module {
    viewModelOf(::BookContentRootViewModel)
    viewModelOf(::BottomBarViewModel)
    viewModelOf(::TTSViewModel)
    viewModelOf(::AutoScrollViewModel)
    viewModelOf(::ContentViewModel)
    viewModelOf(::DrawerContainerViewModel)
    viewModelOf(::TopBarViewModel)
    viewModelOf(::ColorPaletteViewModel)
    viewModelOf(::FontViewModel)
}