package com.capstone.bookshelf.data.repository_impl

import com.capstone.bookshelf.data.database.dao.ChapterDao
import com.capstone.bookshelf.data.database.entity.ChapterContentEntity
import com.capstone.bookshelf.data.mapper.toDataClass
import com.capstone.bookshelf.domain.repository.ChapterRepository
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