package com.capstone.bookshelf.data.repository

import com.capstone.bookshelf.data.database.dao.ImagePathDao
import com.capstone.bookshelf.data.database.entity.ImagePathEntity
import com.capstone.bookshelf.domain.book.ImagePathRepository

class ImagePathRepositoryImpl(
    private val imageDao: ImagePathDao
) : ImagePathRepository {
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