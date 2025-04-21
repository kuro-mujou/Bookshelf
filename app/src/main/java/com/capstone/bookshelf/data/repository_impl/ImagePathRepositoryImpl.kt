package com.capstone.bookshelf.data.repository_impl

import com.capstone.bookshelf.data.database.dao.ImagePathDao
import com.capstone.bookshelf.data.database.entity.ImagePathEntity
import com.capstone.bookshelf.domain.repository.ImagePathRepository

class ImagePathRepositoryImpl(
    private val imageDao: ImagePathDao
) : ImagePathRepository {
    override suspend fun getImagePathsByBookId(bookId: List<String>): List<ImagePathEntity> {
        return imageDao.getImagePathsByBookId(bookId)
    }

    override suspend fun deleteByBookId(bookId: List<String>) {
        imageDao.deleteByBookId(bookId)
    }

    override suspend fun saveImagePath(bookID: String, coverImagePath: List<String>) {
        val imagePathEntity = coverImagePath.map {
            ImagePathEntity(bookId = bookID, imagePath = it)
        }
        imageDao.saveImagePath(imagePathEntity)
    }
    override suspend fun deleteImagePathByPath(imagePath: String): Int {
        return imageDao.deleteImagePathByPath(imagePath)
    }
    override suspend fun insertImagePath(imagePathEntity: ImagePathEntity): Long {
        return imageDao.insertImagePath(imagePathEntity)
    }
}