package com.capstone.bookshelf.domain.repository

import com.capstone.bookshelf.domain.wrapper.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    suspend fun getNotes(bookId: String): Flow<List<Note>>
    suspend fun upsertNote(note: Note)
    suspend fun deleteNote(noteId: Int)
}