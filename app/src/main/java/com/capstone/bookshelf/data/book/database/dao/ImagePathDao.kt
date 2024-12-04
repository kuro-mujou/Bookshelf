package com.capstone.bookshelf.data.book.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.capstone.bookshelf.data.book.database.entity.ImagePathEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImagePathDao {

    @Query("SELECT * FROM image_path")
    fun getAllImage(): Flow<List<ImagePathEntity>>

    @Transaction
    @Query("SELECT * FROM image_path WHERE bookId IN (:bookId)")
    suspend fun getImagePathsByBookId(bookId: List<String>): List<ImagePathEntity>

    @Transaction
    @Query("DELETE FROM image_path WHERE bookId IN (:bookId)")
    suspend fun deleteByBookId(bookId: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveImagePath(imagePathEntity: List<ImagePathEntity>)
}