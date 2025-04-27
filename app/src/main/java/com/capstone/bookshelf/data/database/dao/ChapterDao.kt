package com.capstone.bookshelf.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.capstone.bookshelf.data.database.entity.ChapterContentEntity

@Dao
interface ChapterDao {
    @Transaction
    @Query("SELECT * FROM chapter_content WHERE bookId = :bookId AND tocId = :tocId ORDER BY tocId ASC")
    suspend fun getChapterContent(bookId: String, tocId: Int): ChapterContentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertChapterContent(chapterContent: ChapterContentEntity)

    @Query("UPDATE chapter_content SET content = :content WHERE bookId = :bookId AND tocId = :tocId")
    suspend fun updateChapterContent(bookId: String, tocId: Int, content: List<String>)

    @Query("DELETE FROM chapter_content WHERE bookId = :bookId AND tocId = :tocId")
    suspend fun deleteChapterContent(bookId: String, tocId: Int)

    @Query("UPDATE chapter_content SET tocId = tocId - 1 WHERE bookId = :bookId AND tocId > :tocId")
    suspend fun updateChapterIndexOnDelete(bookId: String, tocId: Int)

    @Query("UPDATE chapter_content SET tocId = tocId + 1 WHERE bookId = :bookId AND tocId > :tocId")
    suspend fun updateChapterIndexOnInsert(bookId: String, tocId: Int)

    @Query("""
        UPDATE chapter_content
        SET `tocId` = `tocId` + 1
        WHERE bookId = :bookId
        AND `tocId` >= :endIndex
        AND `tocId` < :startIndex
    """)
    suspend fun shiftIndexesDown(bookId: String, startIndex: Int, endIndex: Int)

    @Query("""
        UPDATE chapter_content
        SET `tocId` = `tocId` - 1
        WHERE bookId = :bookId
        AND `tocId` > :startIndex
        AND `tocId` <= :endIndex
    """)
    suspend fun shiftIndexesUp(bookId: String, startIndex: Int, endIndex: Int)

    @Query("""
        UPDATE chapter_content
        SET `tocId` = :newIndex
        WHERE chapterContentId = :chapterContentId
    """)
    suspend fun updateDraggedItem(chapterContentId: Int, newIndex: Int)

    @Transaction
    suspend fun reorderChapterContent(bookId: String, chapterContentId: Int, startIndex: Int, endIndex: Int) {
        if (startIndex == endIndex) return

        if (startIndex > endIndex) {
            shiftIndexesDown(bookId, startIndex, endIndex)
        } else {
            shiftIndexesUp(bookId, startIndex, endIndex)
        }

        updateDraggedItem(chapterContentId, endIndex)
    }
}