package com.capstone.bookshelf.data.book.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.capstone.bookshelf.data.book.database.entity.TableOfContentEntity

@Dao
interface TableOfContentDao {
    @Transaction
    @Query("SELECT * FROM table_of_contents WHERE bookId = :bookId")
    suspend fun getTableOfContents(bookId: Int): List<TableOfContentEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTableOfContent(tableOfContent: TableOfContentEntity): Long
}