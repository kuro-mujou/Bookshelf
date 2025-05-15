package com.capstone.bookshelf.data.repository_impl

import com.capstone.bookshelf.data.database.dao.BookDao
import com.capstone.bookshelf.data.database.entity.BookEntity
import com.capstone.bookshelf.data.database.entity.BookWithCategories
import com.capstone.bookshelf.data.mapper.toDataClass
import com.capstone.bookshelf.data.mapper.toEntity
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.domain.wrapper.Book
import com.capstone.bookshelf.domain.wrapper.Category
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

    override fun getBookAsFlow(bookId: String): Flow<Book> {
        return bookDao
            .getBookAsFlow(bookId)
            .map { it.toDataClass() }
    }

    override fun getBookListForMainScreen(): Flow<List<Book>> {
        return bookDao.getTop5RecentBooksFlow().map { bookEntity ->
            bookEntity.map { it.toDataClass() }
        }
    }

    override fun readAllBooksSortByFavorite(): Flow<List<Book>> {
        return bookDao
            .readAllBooksSortByFavorite()
            .map { bookEntity ->
                bookEntity.map { it.toDataClass() }
            }
    }

    override fun readAllBooks(): Flow<List<Book>> {
        return bookDao
            .readAllBooks()
            .map { bookEntity ->
                bookEntity.map { it.toDataClass() }
            }
    }

    override suspend fun getBook(bookId: String): Book? {
        return bookDao.getBook(bookId)?.toDataClass()
    }

    override suspend fun setBookAsFavorite(bookId: String, isFavorite: Boolean) {
        bookDao.setBookAsFavorite(bookId, isFavorite)
    }

    override suspend fun updateRecentRead(bookId: String) {
        val currentRank = bookDao.getRecentRank(bookId) ?: 0

        if (currentRank == 0) {
            bookDao.shiftRanksForNew()
            bookDao.clearOldest()
        } else if (currentRank > 1) {
            bookDao.shiftRanksBefore(currentRank)
        }

        bookDao.markAsMostRecent(bookId)
    }

    override suspend fun saveBookInfoChapterIndex(bookId: String, chapterIndex: Int) {
        bookDao.saveBookInfoChapterIndex(bookId, chapterIndex)
    }

    override suspend fun saveBookInfoParagraphIndex(bookId: String, paragraphIndex: Int) {
        bookDao.saveBookInfoParagraphIndex(bookId, paragraphIndex)
    }

    override suspend fun saveBookInfoTotalChapter(bookId: String, totalChapter: Int) {
        bookDao.saveBookInfoTotalChapter(bookId, totalChapter)
    }

    override suspend fun saveBookInfoTitle(bookId: String, title: String) {
        bookDao.saveBookInfoTitle(bookId, title)
    }

    override suspend fun saveBookInfoAuthors(bookId: String, authors: List<String>) {
        bookDao.saveBookInfoAuthors(bookId, authors)
    }

    override suspend fun deleteBooks(books: List<Book>) {
        val bookEntities = books.map { it.toEntity() }

        val deletedRanks = bookEntities
            .mapNotNull { entity ->
                if (entity.isRecentRead > 0) entity.isRecentRead else null
            }
            .distinct()
            .sorted()

        bookEntities.forEach { bookDao.deleteBooks(it) }
        deletedRanks.forEach { rank ->
            bookDao.compactRanksAfterDeletion(rank)
        }
    }

    override suspend fun updateCurrentChapterIndexOnDelete(bookId: String, deleteIndex: Int) {
        bookDao.updateCurrentChapterIndexOnDelete(bookId, deleteIndex)
    }

    override suspend fun addCategoryToBook(bookId: String, category: Category) {
        bookDao.addCategoryToBook(bookId, category.toEntity().categoryId)
    }

    override suspend fun insertCategory(category: Category): Long {
        return bookDao.insertCategory(category.toEntity())
    }

    override suspend fun deleteCategory(categories: List<Category>) {
        categories.forEach {
            bookDao.deleteCategory(it.toEntity())
        }
    }

    override fun getBookCategory(): Flow<List<Category>> {
        return bookDao.getBookCategory().map { categoryEntity ->
            categoryEntity.map { it.toDataClass() }
        }
    }

    override fun getFlowBookWithCategories(bookId: String): Flow<BookWithCategories> {
        return bookDao.getFlowBookWithCategories(bookId)
    }

    override suspend fun updateBookCategory(bookId: String, categories: List<Category>) {
        val currentCategories = bookDao.getBookWithCategories(bookId)?.categories.orEmpty()

        val currentCategoryIds = currentCategories.map { it.categoryId }.toSet()
        val selectedCategoryIds = categories.filter { it.isSelected }.map { it.id }.toSet()

        val categoriesToRemove = currentCategoryIds - selectedCategoryIds
        val categoriesToAdd = selectedCategoryIds - currentCategoryIds

        categoriesToRemove.forEach { categoryId ->
            bookDao.deleteCategoryFromBook(bookId, categoryId!!)
        }

        categoriesToAdd.forEach { categoryId ->
            bookDao.addCategoryToBook(bookId, categoryId!!)
        }
    }

    override fun getBooksMatchingAnySelectedCategory(selectedCategoryIds: List<Int>): Flow<List<Book>> {
        return bookDao.getBooksByAnyCategory(selectedCategoryIds).map {
            it.map { it.toDataClass() }
        }
    }
}
