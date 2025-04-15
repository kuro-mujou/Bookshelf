package com.capstone.bookshelf.data.repository_impl

import com.capstone.bookshelf.data.database.dao.NoteDao
import com.capstone.bookshelf.domain.repository.NoteRepository

class NoteRepositoryImpl(
    private val noteDao: NoteDao
) : NoteRepository {
}