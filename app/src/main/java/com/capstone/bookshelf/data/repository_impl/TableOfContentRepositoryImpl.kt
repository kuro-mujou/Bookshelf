package com.capstone.bookshelf.data.repository_impl

import com.capstone.bookshelf.data.database.dao.TableOfContentDao
import com.capstone.bookshelf.data.database.entity.TableOfContentEntity
import com.capstone.bookshelf.data.mapper.toDataClass
import com.capstone.bookshelf.data.mapper.toEntity
import com.capstone.bookshelf.domain.repository.TableOfContentRepository
import com.capstone.bookshelf.domain.wrapper.TableOfContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TableOfContentRepositoryImpl(
    private val tableOfContentDao: TableOfContentDao
) : TableOfContentRepository {
    override suspend fun saveTableOfContent(tocEntity: TableOfContentEntity): Long {
        return tableOfContentDao.insertTableOfContent(tocEntity)
    }

    override suspend fun getTableOfContents(bookId: String): Flow<List<TableOfContent>> {
        return tableOfContentDao
            .getTableOfContents(bookId)
            .map { entity ->
                entity.map { it.toDataClass() }
            }
    }

    override suspend fun getTableOfContent(bookId: String, tocId: Int): TableOfContent? {
        return tableOfContentDao.getTableOfContent(bookId, tocId)?.toDataClass()
    }

    override suspend fun addChapter(bookId: String, chapter: TableOfContent) {
        tableOfContentDao.insertTableOfContent(chapter.toEntity())
    }

    override suspend fun updateTableOfContentFavoriteStatus(bookId: String, index: Int, isFavorite: Boolean) {
        tableOfContentDao.updateTableOfContentFavoriteStatus(bookId, index, isFavorite)
    }

    override suspend fun updateTableOfContentTitle(bookId: String, index: Int, title: String) {
        tableOfContentDao.updateTableOfContentTitle(bookId, index, title)
    }

    override suspend fun deleteTableOfContent(tocId: Int) {
        tableOfContentDao.deleteTableOfContent(tocId)
    }

    override suspend fun updateTableOfContentIndexOnDelete(bookId: String, index: Int) {
        tableOfContentDao.updateTableOfContentIndexOnDelete(bookId, index)
    }
}