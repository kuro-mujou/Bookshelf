package com.capstone.bookshelf.data.book.network

import com.capstone.bookshelf.core.domain.DataError
import com.capstone.bookshelf.core.domain.Result
import com.capstone.bookshelf.data.book.dto.BookWorkDto
import com.capstone.bookshelf.data.book.dto.SearchResponseDto

interface RemoteBookDataSource {
    suspend fun searchBooks(
        query: String,
        resultLimit: Int? = null
    ): Result<SearchResponseDto, DataError.Remote>

    suspend fun getBookDetails(bookWorkId: String): Result<BookWorkDto, DataError.Remote>
}