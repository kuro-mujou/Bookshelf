package com.capstone.bookshelf.core.di

import android.app.Application
import com.capstone.bookshelf.feature.booklist.di.bookListViewModelModule
import com.capstone.bookshelf.feature.importbook.di.importBookViewModelModule
import com.capstone.bookshelf.feature.readbook.di.BookContentViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BookShelf : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BookShelf)
            modules(
                listOf(
                    settingViewModelModule,
                    databaseModule,
                    importBookViewModelModule,
                    bookListViewModelModule,
                    BookContentViewModelModule
                )
            )
        }
    }
}