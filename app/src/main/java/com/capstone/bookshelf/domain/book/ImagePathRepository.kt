package com.capstone.bookshelf.domain.book

import com.capstone.bookshelf.data.database.entity.ImagePathEntity
import kotlinx.coroutines.flow.Flow

interface ImagePathRepository {
    suspend fun getImagePathsByBookId(bookId: List<String>): List<ImagePathEntity>
    suspend fun deleteByBookId(bookId: List<String>)
    suspend fun saveImagePath(bookID: String, coverImagePath: List<String>)
}