package com.capstone.bookshelf.data.book.repository

import com.capstone.bookshelf.data.book.database.dao.ChapterDao
import com.capstone.bookshelf.data.book.database.entity.ChapterContentEntity
import com.capstone.bookshelf.domain.book.ChapterRepository

class ChapterRepositoryImpl(
    private val chapterDao: ChapterDao
): ChapterRepository {
    override suspend fun getChapterContent(bookId: Int, tocId: Int): ChapterContentEntity? {
        return chapterDao.getChapterContent(bookId, tocId)
    }
    override suspend fun saveChapterContent(chapterContentEntity: ChapterContentEntity) {
        chapterDao.insertChapterContent(chapterContentEntity)
    }
}