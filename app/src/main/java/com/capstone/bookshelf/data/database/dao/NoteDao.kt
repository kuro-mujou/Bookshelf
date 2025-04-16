package com.capstone.bookshelf.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.capstone.bookshelf.data.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Transaction
    @Query("SELECT * FROM notes WHERE bookId = :bookId")
    fun getNotes(bookId: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addNote(note: NoteEntity)

    @Query("UPDATE notes SET noteInput = :noteInput WHERE noteId = :noteId")
    suspend fun updateNote(noteId: Int, noteInput: String)

    @Query("SELECT * FROM notes WHERE bookId = :bookId AND tocId = :tocId AND contentId = :contentId LIMIT 1")
    suspend fun findNoteByIds(tocId: Int, bookId: String, contentId: Int): NoteEntity?

    @Transaction
    suspend fun upsertBasedOnIds(note: NoteEntity) {
        val existingNote = findNoteByIds(note.tocId, note.bookId, note.contentId)
        if (existingNote != null) {
            updateNote(existingNote.noteId, note.noteInput)
        } else {
            addNote(note)
        }
    }

    @Query("DELETE FROM notes WHERE noteId = :noteId")
    suspend fun deleteNote(noteId: Int)
}