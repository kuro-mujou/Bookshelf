package com.capstone.bookshelf.core.di

import androidx.room.Room
import com.capstone.bookshelf.core.data.BookDatabase
import com.capstone.bookshelf.core.data.BookRepository
import org.koin.dsl.module

val databaseModule = module {
    // Provide Room database
    single {
        Room.databaseBuilder(get(), BookDatabase::class.java, BookDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration(false) // Handle migration
            .build()
    }

    // Provide DAO
    single { get<BookDatabase>().bookDao() }

    // Provide Repository
    single { BookRepository(get()) }
}
