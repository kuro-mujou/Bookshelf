package com.capstone.bookshelf.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.capstone.bookshelf.data.database.entity.BookCategoryCrossRef
import com.capstone.bookshelf.data.database.entity.BookEntity
import com.capstone.bookshelf.data.database.entity.BookWithCategories
import com.capstone.bookshelf.data.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBook(book: BookEntity): Long

    @Transaction
    @Query("SELECT * FROM books")
    fun readAllBooks(): Flow<List<BookEntity>>

    @Transaction
    @Query("SELECT * FROM books WHERE bookId = :bookId LIMIT 1")
    fun getBookAsFlow(bookId: String): Flow<BookEntity>

    @Query("SELECT * FROM books WHERE isRecentRead > 0 ORDER BY isRecentRead ASC")
    fun getTop5RecentBooksFlow(): Flow<List<BookEntity>>

    @Query("""
        UPDATE books
        SET isRecentRead = isRecentRead - 1
        WHERE isRecentRead > :deletedRank
    """)
    suspend fun compactRanksAfterDeletion(deletedRank: Int)

    @Query("""
        UPDATE books
        SET isRecentRead = isRecentRead + 1
        WHERE isRecentRead > 0 AND isRecentRead < :oldRank
    """)
    suspend fun shiftRanksBefore(oldRank: Int)

    @Query("""
        UPDATE books
        SET isRecentRead = isRecentRead + 1
        WHERE isRecentRead BETWEEN 1 AND 5
    """)
    suspend fun shiftRanksForNew()

    @Query("UPDATE books SET isRecentRead = 0 WHERE isRecentRead = 6")
    suspend fun clearOldest()

    @Query("UPDATE books SET isRecentRead = 1 WHERE bookId = :bookId")
    suspend fun markAsMostRecent(bookId: String)

    @Query("SELECT isRecentRead FROM books WHERE bookId = :bookId")
    suspend fun getRecentRank(bookId: String): Int?

    @Transaction
    @Query("SELECT * FROM books ORDER BY isFavorite DESC")
    fun readAllBooksSortByFavorite(): Flow<List<BookEntity>>

    @Transaction
    @Query("SELECT * FROM books WHERE bookId = :bookId LIMIT 1")
    suspend fun getBook(bookId: String): BookEntity?

    @Transaction
    @Query("SELECT * FROM books WHERE title = :title LIMIT 1")
    suspend fun isBookExist(title: String): BookEntity?

    @Query("UPDATE books SET isFavorite = :isFavorite WHERE bookId = :bookId")
    suspend fun setBookAsFavorite(bookId: String, isFavorite: Boolean)

    @Query("UPDATE books SET currentChapter = :chapterIndex WHERE bookId = :bookId")
    suspend fun saveBookInfoChapterIndex(bookId: String, chapterIndex: Int)

    @Query("UPDATE books SET currentParagraph = :paragraphIndex WHERE bookId = :bookId")
    suspend fun saveBookInfoParagraphIndex(bookId: String, paragraphIndex: Int)

    @Query("UPDATE books SET totalChapter = :totalChapter WHERE bookId = :bookId")
    suspend fun saveBookInfoTotalChapter(bookId: String, totalChapter: Int)

    @Query("UPDATE books SET title = :title WHERE bookId = :bookId")
    suspend fun saveBookInfoTitle(bookId: String, title: String)

    @Query("UPDATE books SET authors = :authors WHERE bookId = :bookId")
    suspend fun saveBookInfoAuthors(bookId: String, authors: List<String>)

    @Query("""
        UPDATE books
        SET 
            totalChapter = totalChapter - 1,
            currentChapter = CASE 
                WHEN :deletedChapter <= currentChapter THEN currentChapter - 1
                ELSE currentChapter
            END
        WHERE bookId = :bookId
    """)
    suspend fun updateCurrentChapterIndexOnDelete(bookId: String, deletedChapter: Int)

    @Transaction
    @Delete
    suspend fun deleteBooks(bookEntity: BookEntity)

    @Transaction
    @Query("SELECT * FROM books WHERE bookId = :bookId")
    fun getFlowBookWithCategories(bookId: String): Flow<BookWithCategories>

    @Transaction
    @Query("SELECT * FROM books WHERE bookId = :bookId")
    suspend fun getBookWithCategories(bookId: String): BookWithCategories?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBookCategoryCrossRef(crossRef: BookCategoryCrossRef)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryEntity?

    @Transaction
    suspend fun addCategoryToBook(bookId: String, category: Int) {
        insertBookCategoryCrossRef(BookCategoryCrossRef(bookId, category))
    }

    @Query("DELETE FROM book_category_cross_ref WHERE bookId = :bookId")
    suspend fun deleteAllCategoryFromBook(bookId: String)

    @Query("DELETE FROM book_category_cross_ref WHERE bookId = :bookId AND categoryId = :categoryId")
    suspend fun deleteCategoryFromBook(bookId: String, categoryId: Int)

    @Transaction
    @Query("SELECT * FROM categories")
    fun getBookCategory(): Flow<List<CategoryEntity>>

    @Transaction
    @Query("""
        SELECT DISTINCT b.* FROM books b
        INNER JOIN book_category_cross_ref bc ON b.bookId = bc.bookId
        WHERE bc.categoryId IN (:categoryIds)
    """)
    fun getBooksByAnyCategory(categoryIds: List<Int>): Flow<List<BookEntity>>
}
