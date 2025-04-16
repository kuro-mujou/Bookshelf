package com.capstone.bookshelf.di

import androidx.room.Room
import com.capstone.bookshelf.data.database.LocalBookDatabase
import com.capstone.bookshelf.data.repository_impl.BookRepositoryImpl
import com.capstone.bookshelf.data.repository_impl.ChapterRepositoryImpl
import com.capstone.bookshelf.data.repository_impl.ImagePathRepositoryImpl
import com.capstone.bookshelf.data.repository_impl.MusicPathRepositoryImpl
import com.capstone.bookshelf.data.repository_impl.NoteRepositoryImpl
import com.capstone.bookshelf.data.repository_impl.TableOfContentRepositoryImpl
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.domain.repository.ChapterRepository
import com.capstone.bookshelf.domain.repository.ImagePathRepository
import com.capstone.bookshelf.domain.repository.MusicPathRepository
import com.capstone.bookshelf.domain.repository.NoteRepository
import com.capstone.bookshelf.domain.repository.TableOfContentRepository
import com.capstone.bookshelf.presentation.SelectedBookViewModel
import com.capstone.bookshelf.presentation.bookdetail.BookDetailViewModel
import com.capstone.bookshelf.presentation.booklist.BookListViewModel
import com.capstone.bookshelf.presentation.booklist.component.AsyncImportBookViewModel
import com.capstone.bookshelf.presentation.bookwriter.BookWriterViewModel
import com.capstone.bookshelf.util.DataStoreManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val singleModule = module {
    single {
        Room.databaseBuilder(get(), LocalBookDatabase::class.java, LocalBookDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration().build()
    }
}
val databaseModule = module{
    single { get<LocalBookDatabase>().bookDao }
    single { get<LocalBookDatabase>().chapterDao }
    single { get<LocalBookDatabase>().tableOfContentDao }
    single { get<LocalBookDatabase>().imagePathDao }
    single { get<LocalBookDatabase>().musicPathDao }
    single { get<LocalBookDatabase>().noteDao }
}
val repositoryModule = module {
    singleOf(::BookRepositoryImpl).bind<BookRepository>()
    singleOf(::ChapterRepositoryImpl).bind<ChapterRepository>()
    singleOf(::TableOfContentRepositoryImpl).bind<TableOfContentRepository>()
    singleOf(::ImagePathRepositoryImpl).bind<ImagePathRepository>()
    singleOf(::MusicPathRepositoryImpl).bind<MusicPathRepository>()
    singleOf(::NoteRepositoryImpl).bind<NoteRepository>()
}
val viewModelModule = module {
    viewModelOf(::SelectedBookViewModel)
    viewModelOf(::BookListViewModel)
    viewModelOf(::AsyncImportBookViewModel)
    viewModelOf(::BookDetailViewModel)
    viewModelOf(::BookWriterViewModel)
}
val dataStoreModule = module {
    single{ DataStoreManager(androidContext())}
}