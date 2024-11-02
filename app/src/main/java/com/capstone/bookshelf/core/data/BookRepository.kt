package com.capstone.bookshelf.core.data

import com.capstone.bookshelf.core.data.dao.BookDao
import com.capstone.bookshelf.core.domain.BookEntity
import com.capstone.bookshelf.core.domain.ChapterContentEntity
import com.capstone.bookshelf.core.domain.TableOfContentEntity
import kotlinx.coroutines.flow.Flow

class BookRepository(
    val bookDao: BookDao,
) {
    suspend fun saveBook(book: BookEntity): Long {
        return bookDao.insertBook(book)
    }

    suspend fun saveTableOfContent(tocEntity: TableOfContentEntity): Long {
        return bookDao.insertTableOfContent(tocEntity)
    }

    suspend fun saveChapterContent(chapterContentEntity: ChapterContentEntity) {
        bookDao.insertChapterContent(chapterContentEntity)
    }
    suspend fun isBookAlreadyImported(title: String): Boolean {
        return bookDao.getBookByTitleAndAuthor(title) != null
    }

    fun getBookById(id: Int): Flow<BookEntity> {
        return bookDao.getBookById(id)
    }

    suspend fun getChapterContent(bookId: Int, tocId: Int): ChapterContentEntity? {
        return bookDao.getChapterContent(bookId, tocId)
    }
    fun readAllBooksSortByFavorite(): Flow<List<BookEntity>> {
        return bookDao.readAllBooksSortByFavorite()
    }
    fun readAllBooks(): Flow<List<BookEntity>> {
        return bookDao.readAllBooks()
    }
    suspend fun setBookAsFavorite(bookId: Int, isFavorite: Boolean) {
        bookDao.setFavoriteBook( isFavorite, bookId)
    }

    suspend fun getTableOfContents(bookId: Int): List<TableOfContentEntity> {
        return bookDao.getTableOfContents(bookId)
    }

    suspend fun saveBookInfo(bookId: Int, chapterIndex: Int) {
        bookDao.saveBookInfo(bookId,chapterIndex)
    }
}
