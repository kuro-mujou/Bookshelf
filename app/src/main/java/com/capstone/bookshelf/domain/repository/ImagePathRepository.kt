package com.capstone.bookshelf.domain.repository

import com.capstone.bookshelf.data.database.entity.ImagePathEntity

interface ImagePathRepository {
    suspend fun getImagePathsByBookId(bookId: List<String>): List<ImagePathEntity>
    suspend fun deleteByBookId(bookId: List<String>)
    suspend fun saveImagePath(bookID: String, coverImagePath: List<String>)
}