package com.capstone.bookshelf.data.repository

import com.capstone.bookshelf.data.database.dao.TableOfContentDao
import com.capstone.bookshelf.data.database.entity.TableOfContentEntity
import com.capstone.bookshelf.data.mapper.toDataClass
import com.capstone.bookshelf.domain.book.TableOfContentRepository
import com.capstone.bookshelf.domain.wrapper.TableOfContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TableOfContentRepositoryImpl(
    private val tableOfContentDao: TableOfContentDao
): TableOfContentRepository {
    override suspend fun saveTableOfContent(tocEntity: TableOfContentEntity): Long {
        return tableOfContentDao.insertTableOfContent(tocEntity)
    }
    override suspend fun getTableOfContents(bookId: String): Flow<List<TableOfContent>> {
        return tableOfContentDao
            .getTableOfContents(bookId)
            .map { entity->
                entity.map{it.toDataClass()}
            }
    }
    override suspend fun getTableOfContent(bookId: String,tocId: Int): TableOfContent {
        return tableOfContentDao.getTableOfContent(bookId,tocId)!!.toDataClass()
    }
}