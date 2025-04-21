package com.capstone.bookshelf.presentation.bookcontent.di_multi_module

import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPaletteViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.music.MusicViewModel
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerViewModel
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBarViewModel
import com.capstone.bookshelf.presentation.bookwriter.BookWriterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

@UnstableApi
val bookContentModule = module {
    viewModelOf(::BottomBarViewModel)
    viewModelOf(::AutoScrollViewModel)
    viewModelOf(::ContentViewModel)
    viewModelOf(::DrawerContainerViewModel)
    viewModelOf(::TopBarViewModel)
    viewModelOf(::ColorPaletteViewModel)
    viewModelOf(::MusicViewModel)
    viewModelOf(::BookWriterViewModel)
}