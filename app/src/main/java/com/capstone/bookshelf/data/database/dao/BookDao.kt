package com.capstone.bookshelf.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.capstone.bookshelf.data.database.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBook(book: BookEntity): Long

    @Transaction
    @Query("SELECT * FROM books")
    fun readAllBooks(): Flow<List<BookEntity>>

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

    @Transaction
    @Delete
    suspend fun deleteBooks(bookEntities: List<BookEntity>)
}
