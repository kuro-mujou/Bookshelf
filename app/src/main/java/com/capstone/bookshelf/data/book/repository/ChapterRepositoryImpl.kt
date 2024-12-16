package com.capstone.bookshelf.data.book.repository

import com.capstone.bookshelf.data.book.database.dao.ChapterDao
import com.capstone.bookshelf.data.book.database.entity.ChapterContentEntity
import com.capstone.bookshelf.data.mapper.toDataClass
import com.capstone.bookshelf.domain.book.ChapterRepository
import com.capstone.bookshelf.domain.wrapper.Chapter

class ChapterRepositoryImpl(
    private val chapterDao: ChapterDao
): ChapterRepository {
    override suspend fun getChapterContent(bookId: String, tocId: Int): Chapter? {
        return chapterDao
            .getChapterContent(bookId, tocId)
            ?.toDataClass()
    }
    override suspend fun saveChapterContent(chapterContentEntity: ChapterContentEntity) {
        chapterDao.insertChapterContent(chapterContentEntity)
    }
}