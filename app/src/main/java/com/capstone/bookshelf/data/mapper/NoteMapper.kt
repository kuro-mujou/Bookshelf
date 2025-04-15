package com.capstone.bookshelf.data.mapper

import com.capstone.bookshelf.data.database.entity.NoteEntity
import com.capstone.bookshelf.domain.wrapper.Note

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        bookId = bookId,
        contentIndex = contentIndex,
        contentDetail = contentDetail,
        noteContent = noteContent,
        timestamp = timestamp
    )
}

fun NoteEntity.toDataClass(): Note {
    return Note(
        noteId = noteId,
        bookId = bookId,
        contentIndex = contentIndex,
        contentDetail = contentDetail,
        noteContent = noteContent,
        timestamp = timestamp
    )
}