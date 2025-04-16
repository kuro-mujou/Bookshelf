package com.capstone.bookshelf.data.mapper

import com.capstone.bookshelf.data.database.entity.NoteEntity
import com.capstone.bookshelf.domain.wrapper.Note

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        bookId = bookId,
        tocId = tocId,
        contentId = contentId,
        noteBody = noteBody,
        noteInput = noteInput,
        timestamp = timestamp
    )
}

fun NoteEntity.toDataClass(): Note {
    return Note(
        noteId = noteId,
        bookId = bookId,
        tocId = tocId,
        contentId = contentId,
        noteBody = noteBody,
        noteInput = noteInput,
        timestamp = timestamp
    )
}