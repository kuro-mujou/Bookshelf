package com.capstone.bookshelf.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.capstone.bookshelf.data.database.entity.ImagePathEntity

@Dao
interface ImagePathDao {

    @Transaction
    @Query("SELECT * FROM image_path WHERE bookId IN (:bookId)")
    suspend fun getImagePathsByBookId(bookId: List<String>): List<ImagePathEntity>

    @Transaction
    @Query("DELETE FROM image_path WHERE bookId IN (:bookId)")
    suspend fun deleteByBookId(bookId: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveImagePath(imagePathEntity: List<ImagePathEntity>)

    @Query("DELETE FROM image_path WHERE imagePath = :path")
    suspend fun deleteImagePathByPath(path: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImagePath(imagePath: ImagePathEntity): Long
}