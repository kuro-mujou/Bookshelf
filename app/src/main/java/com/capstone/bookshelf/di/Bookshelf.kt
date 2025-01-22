package com.capstone.bookshelf.di

import android.app.Application
import com.capstone.bookshelf.presentation.bookcontent.di_multi_module.bookContentModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Bookshelf : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@Bookshelf)
            modules(
                singleModule,
                databaseModule,
                repositoryModule,
                viewModelModule,
                bookContentModule,
                dataStoreModule
            )
        }
    }
}