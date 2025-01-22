package com.capstone.bookshelf.data.repository

import com.capstone.bookshelf.data.database.dao.BookDao
import com.capstone.bookshelf.data.database.entity.BookEntity
import com.capstone.bookshelf.data.mapper.toDataClass
import com.capstone.bookshelf.data.mapper.toEntity
import com.capstone.bookshelf.domain.book.BookRepository
import com.capstone.bookshelf.domain.wrapper.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookRepositoryImpl(
    private val bookDao: BookDao,
) : BookRepository {
    override suspend fun insertBook(book: BookEntity): Long {
        return bookDao.insertBook(book)
    }
    override suspend fun isBookExist(title: String): Boolean {
        return bookDao.isBookExist(title) != null
    }
    override fun readAllBooksSortByFavorite(): Flow<List<Book>> {
        return bookDao
            .readAllBooksSortByFavorite()
            .map { bookEntity->
                bookEntity.map{it.toDataClass()}
            }
    }
    override fun readAllBooks(): Flow<List<Book>> {
        return bookDao
            .readAllBooks()
            .map { bookEntity->
                bookEntity.map{it.toDataClass()}
            }
    }
    override suspend fun setBookAsFavorite(bookId: String, isFavorite: Boolean) {
        bookDao.setBookAsFavorite(bookId, isFavorite)
    }
    override suspend fun saveBookInfo(bookId: String, chapterIndex: Int) {
        bookDao.saveBookInfo(bookId,chapterIndex)
    }
    override suspend fun deleteBooks(books: List<Book>) {
        val bookEntities = books.map {
            it.toEntity()
        }
        bookDao.deleteBooks(bookEntities)
    }
    override suspend fun getBookById(bookId: String): Book {
        return bookDao.getBookById(bookId).toDataClass()
    }
}
