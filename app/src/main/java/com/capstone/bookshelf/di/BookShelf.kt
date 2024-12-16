package com.capstone.bookshelf.di

import android.app.Application
import com.capstone.bookshelf.presentation.bookcontent.di_multi_module.bookContentModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BookShelf : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BookShelf)
            modules(
                singleModule,
                databaseModule,
                repositoryModule,
                viewModelModule,
                bookContentModule
            )
        }
    }
}