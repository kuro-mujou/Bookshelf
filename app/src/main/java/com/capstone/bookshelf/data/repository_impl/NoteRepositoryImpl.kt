package com.capstone.bookshelf.data.repository_impl

import com.capstone.bookshelf.data.database.dao.NoteDao
import com.capstone.bookshelf.data.mapper.toDataClass
import com.capstone.bookshelf.data.mapper.toEntity
import com.capstone.bookshelf.domain.repository.NoteRepository
import com.capstone.bookshelf.domain.wrapper.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val noteDao: NoteDao
) : NoteRepository {
    override suspend fun getNotes(bookId: String): Flow<List<Note>> {
        return noteDao.getNotes(bookId).map { noteEntityList ->
            noteEntityList.reversed().map { noteEntity ->
                noteEntity.toDataClass()
            }
        }
    }
    override suspend fun upsertNote(note: Note) {
        noteDao.upsertBasedOnIds(note.toEntity())
    }
    override suspend fun deleteNote(noteId : Int) {
        noteDao.deleteNote(noteId)
    }
}