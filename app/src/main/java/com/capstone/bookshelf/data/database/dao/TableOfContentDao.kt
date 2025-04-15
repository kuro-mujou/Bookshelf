package com.capstone.bookshelf.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.capstone.bookshelf.data.database.entity.TableOfContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TableOfContentDao {
    @Transaction
    @Query("SELECT * FROM table_of_contents WHERE bookId = :bookId")
    fun getTableOfContents(bookId: String): Flow<List<TableOfContentEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTableOfContent(tableOfContent: TableOfContentEntity): Long

    @Transaction
    @Query("SELECT * FROM table_of_contents WHERE bookId = :bookId AND `index` = :tocId")
    suspend fun getTableOfContent(bookId: String, tocId: Int): TableOfContentEntity?

    @Query("UPDATE table_of_contents SET isFavorite = :isFavorite WHERE bookId = :bookId AND `index` = :index")
    suspend fun updateTableOfContent(bookId: String, index: Int, isFavorite: Boolean)
}