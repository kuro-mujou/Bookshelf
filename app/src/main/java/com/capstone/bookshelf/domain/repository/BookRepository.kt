package com.capstone.bookshelf.domain.repository

import com.capstone.bookshelf.data.database.entity.BookEntity
import com.capstone.bookshelf.data.database.entity.BookWithCategories
import com.capstone.bookshelf.domain.wrapper.Book
import com.capstone.bookshelf.domain.wrapper.Category
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun readAllBooks(): Flow<List<Book>>
    fun readAllBooksSortByFavorite(): Flow<List<Book>>
    fun getBookAsFlow(bookId: String): Flow<Book>
    fun getBookListForMainScreen(): Flow<List<Book>>
    suspend fun getBook(bookId: String): Book?
    suspend fun insertBook(book: BookEntity): Long
    suspend fun isBookExist(title: String): Boolean
    suspend fun setBookAsFavorite(bookId: String, isFavorite: Boolean)
    suspend fun updateRecentRead(bookId: String)
    suspend fun saveBookInfoChapterIndex(bookId: String, chapterIndex: Int)
    suspend fun saveBookInfoParagraphIndex(bookId: String, paragraphIndex: Int)
    suspend fun saveBookInfoTotalChapter(bookId: String, totalChapter: Int)
    suspend fun saveBookInfoTitle(bookId: String, title: String)
    suspend fun saveBookInfoAuthors(bookId: String, authors: List<String>)
    suspend fun deleteBooks(books: List<Book>)
    suspend fun updateCurrentChapterIndexOnDelete(bookId: String, deleteIndex: Int)
    suspend fun addCategoryToBook(bookId: String, category: Category)
    suspend fun insertCategory(category: Category): Long
    suspend fun deleteCategory(categories: List<Category>)
    fun getBookCategory(): Flow<List<Category>>
    fun getFlowBookWithCategories(bookId: String): Flow<BookWithCategories>
    suspend fun updateBookCategory(bookId: String,categories: List<Category>)
    fun getBooksMatchingAnySelectedCategory(selectedCategoryIds: List<Int>): Flow<List<Book>>
}