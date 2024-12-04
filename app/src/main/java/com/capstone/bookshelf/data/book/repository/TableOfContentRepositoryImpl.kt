package com.capstone.bookshelf.data.book.repository

import com.capstone.bookshelf.data.book.database.dao.TableOfContentDao
import com.capstone.bookshelf.data.book.database.entity.TableOfContentEntity
import com.capstone.bookshelf.domain.book.TableOfContentRepository

class TableOfContentRepositoryImpl(
    private val tableOfContentDao: TableOfContentDao
): TableOfContentRepository {
    override suspend fun saveTableOfContent(tocEntity: TableOfContentEntity): Long {
        return tableOfContentDao.insertTableOfContent(tocEntity)
    }
    override suspend fun getTableOfContents(bookId: Int): List<TableOfContentEntity> {
        return tableOfContentDao.getTableOfContents(bookId)
    }
}