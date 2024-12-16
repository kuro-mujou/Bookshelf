package com.capstone.bookshelf.domain.book

import com.capstone.bookshelf.data.book.database.entity.BookEntity
import com.capstone.bookshelf.domain.wrapper.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun readAllBooks(): Flow<List<Book>>
    fun readAllBooksSortByFavorite(): Flow<List<Book>>

    suspend fun insertBook(book: BookEntity): Long
    suspend fun isBookExist(title: String): Boolean
    suspend fun setBookAsFavorite(bookId: String, isFavorite: Boolean)
    suspend fun saveBookInfo(bookId: String, chapterIndex: Int)
    suspend fun deleteBooks(books: List<Book>)
}