package com.capstone.bookshelf.data.book.repository

import com.capstone.bookshelf.data.book.database.dao.ImagePathDao
import com.capstone.bookshelf.data.book.database.entity.ImagePathEntity
import com.capstone.bookshelf.domain.book.ImagePathRepository
import kotlinx.coroutines.flow.Flow

class ImagePathRepositoryImpl(
    private val imageDao: ImagePathDao
) : ImagePathRepository {
    override fun getAllImage(): Flow<List<ImagePathEntity>> {
        return imageDao.getAllImage()
    }
    override suspend fun getImagePathsByBookId(bookId: List<String>): List<ImagePathEntity> {
        return imageDao.getImagePathsByBookId(bookId)
    }

    override suspend fun deleteByBookId(bookId: List<String>) {
        imageDao.deleteByBookId(bookId)
    }

    override suspend fun saveImagePath(bookID: String, coverImagePath: List<String>){
        val imagePathEntity = coverImagePath.map{
            ImagePathEntity(bookId = bookID, imagePath = it)
        }
        imageDao.saveImagePath(imagePathEntity)
    }
}