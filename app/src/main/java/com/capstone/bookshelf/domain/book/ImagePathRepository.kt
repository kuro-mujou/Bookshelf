package com.capstone.bookshelf.domain.book

import com.capstone.bookshelf.data.book.database.entity.ImagePathEntity
import kotlinx.coroutines.flow.Flow

interface ImagePathRepository {
    fun getAllImage(): Flow<List<ImagePathEntity>>
    suspend fun getImagePathsByBookId(bookId: List<String>): List<ImagePathEntity>
    suspend fun deleteByBookId(bookId: List<String>)
    suspend fun saveImagePath(bookID: String, coverImagePath: List<String>)
}