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
    @Query("SELECT * FROM chapter_content WHERE bookId = :bookId AND tocId = :tocId")
    suspend fun getChapterContent(bookId: String, tocId: Int): ChapterContentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertChapterContent(chapterContent: ChapterContentEntity)

    @Query("UPDATE chapter_content SET content = :content WHERE bookId = :bookId AND tocId = :tocId")
    suspend fun updateChapterContent(bookId: String, tocId: Int, content: List<String>)
}