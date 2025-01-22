package com.capstone.bookshelf.di

import androidx.room.Room
import com.capstone.bookshelf.data.database.LocalBookDatabase
import com.capstone.bookshelf.data.repository.BookRepositoryImpl
import com.capstone.bookshelf.data.repository.ChapterRepositoryImpl
import com.capstone.bookshelf.data.repository.ImagePathRepositoryImpl
import com.capstone.bookshelf.data.repository.TableOfContentRepositoryImpl
import com.capstone.bookshelf.domain.book.BookRepository
import com.capstone.bookshelf.domain.book.ChapterRepository
import com.capstone.bookshelf.domain.book.ImagePathRepository
import com.capstone.bookshelf.domain.book.TableOfContentRepository
import com.capstone.bookshelf.presentation.SelectedBookViewModel
import com.capstone.bookshelf.presentation.bookdetail.BookDetailViewModel
import com.capstone.bookshelf.presentation.booklist.BookListViewModel
import com.capstone.bookshelf.presentation.booklist.component.AsyncImportBookViewModel
import com.capstone.bookshelf.util.DataStoreManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val singleModule = module {
    single {
        Room.databaseBuilder(get(), LocalBookDatabase::class.java, LocalBookDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration(false).build()
    }
}
val databaseModule = module{
    single { get<LocalBookDatabase>().bookDao }
    single { get<LocalBookDatabase>().chapterDao }
    single { get<LocalBookDatabase>().tableOfContentDao }
    single { get<LocalBookDatabase>().imagePathDao }
}
val repositoryModule = module {
    singleOf(::BookRepositoryImpl).bind<BookRepository>()
    singleOf(::ChapterRepositoryImpl).bind<ChapterRepository>()
    singleOf(::TableOfContentRepositoryImpl).bind<TableOfContentRepository>()
    singleOf(::ImagePathRepositoryImpl).bind<ImagePathRepository>()
}
val viewModelModule = module {
    viewModelOf(::SelectedBookViewModel)
    viewModelOf(::BookListViewModel)
    viewModelOf(::AsyncImportBookViewModel)
    viewModelOf(::BookDetailViewModel)
}
val dataStoreModule = module {
    single{ DataStoreManager(androidContext())}
}