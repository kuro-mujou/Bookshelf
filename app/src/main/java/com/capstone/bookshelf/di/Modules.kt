package com.capstone.bookshelf.di

import androidx.room.Room
import com.capstone.bookshelf.core.data.HttpClientFactory
import com.capstone.bookshelf.data.book.database.LocalBookDatabase
import com.capstone.bookshelf.data.book.network.KtorRemoteBookDataSource
import com.capstone.bookshelf.data.book.network.RemoteBookDataSource
import com.capstone.bookshelf.data.book.repository.BookRepositoryImpl
import com.capstone.bookshelf.data.book.repository.BookSettingRepositoryImpl
import com.capstone.bookshelf.data.book.repository.ChapterRepositoryImpl
import com.capstone.bookshelf.data.book.repository.ImagePathRepositoryImpl
import com.capstone.bookshelf.data.book.repository.TableOfContentRepositoryImpl
import com.capstone.bookshelf.data.setting.database.SettingDatabase
import com.capstone.bookshelf.data.setting.repository.SettingRepositoryImpl
import com.capstone.bookshelf.domain.book.BookRepository
import com.capstone.bookshelf.domain.book.BookSettingRepository
import com.capstone.bookshelf.domain.book.ChapterRepository
import com.capstone.bookshelf.domain.book.ImagePathRepository
import com.capstone.bookshelf.domain.book.TableOfContentRepository
import com.capstone.bookshelf.domain.setting.SettingRepository
import com.capstone.bookshelf.presentation.SelectedBookViewModel
import com.capstone.bookshelf.presentation.main.RootViewModel
import com.capstone.bookshelf.presentation.main.booklist.LibraryViewModel
import com.capstone.bookshelf.presentation.main.booklist.local.LocalBookListViewModel
import com.capstone.bookshelf.presentation.main.booklist.remote.RemoteBookListViewModel
import com.capstone.bookshelf.presentation.main.component.ImportBookViewModel
import com.capstone.bookshelf.presentation.main.homepage.HomePageViewModel
import com.capstone.bookshelf.presentation.main.search.SearchPageViewModel
import com.capstone.bookshelf.presentation.main.setting.SettingViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val singleModule = module {
    single { HttpClientFactory.create(get()) }
    single {
        Room.databaseBuilder(get(), LocalBookDatabase::class.java, LocalBookDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration(false).build()
    }
    single {
        Room.databaseBuilder(get(), SettingDatabase::class.java, SettingDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration(false).build()
    }
}
val databaseModule = module{
    single { get<LocalBookDatabase>().bookDao }
    single { get<LocalBookDatabase>().bookSettingDao }
    single { get<LocalBookDatabase>().chapterDao }
    single { get<LocalBookDatabase>().tableOfContentDao }
    single { get<LocalBookDatabase>().imagePathDao }
    single { get<SettingDatabase>().settingDao }
}
val repositoryModule = module {
    singleOf(::BookRepositoryImpl).bind<BookRepository>()
    singleOf(::BookSettingRepositoryImpl).bind<BookSettingRepository>()
    singleOf(::ChapterRepositoryImpl).bind<ChapterRepository>()
    singleOf(::TableOfContentRepositoryImpl).bind<TableOfContentRepository>()
    singleOf(::ImagePathRepositoryImpl).bind<ImagePathRepository>()
    singleOf(::SettingRepositoryImpl).bind<SettingRepository>()
    singleOf(::KtorRemoteBookDataSource).bind<RemoteBookDataSource>()
}
val viewModelModule = module {
    viewModelOf(::SelectedBookViewModel)
    viewModelOf(::RootViewModel)
    viewModelOf(::LibraryViewModel)
    viewModelOf(::LocalBookListViewModel)
    viewModelOf(::RemoteBookListViewModel)
    viewModelOf(::ImportBookViewModel)
    viewModelOf(::HomePageViewModel)
    viewModelOf(::SearchPageViewModel)
    viewModelOf(::SettingViewModel)
}