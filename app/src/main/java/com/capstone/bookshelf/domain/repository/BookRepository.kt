package com.capstone.bookshelf.domain.repository

import com.capstone.bookshelf.data.database.entity.BookEntity
import com.capstone.bookshelf.domain.wrapper.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun readAllBooks(): Flow<List<Book>>
    fun readAllBooksSortByFavorite(): Flow<List<Book>>

    suspend fun insertBook(book: BookEntity): Long
    suspend fun isBookExist(title: String): Boolean
    suspend fun setBookAsFavorite(bookId: String, isFavorite: Boolean)
    suspend fun saveBookInfoChapterIndex(bookId: String, chapterIndex: Int)
    suspend fun saveBookInfoParagraphIndex(bookId: String, paragraphIndex: Int)
    suspend fun deleteBooks(books: List<Book>)
}