package com.capstone.bookshelf.presentation.bookwriter

import android.content.Context
import com.capstone.bookshelf.presentation.bookwriter.component.ParagraphType

sealed interface BookWriterAction {
    data class AddBookInfo(
        val context: Context,
        val bookTitle: String,
        val authorName: String,
        val coverImagePath: String
    ): BookWriterAction
    data class AddChapter(val chapterTitle: String): BookWriterAction
    data class AddImage(val context: Context,val imageUri: String): BookWriterAction

    data class UpdateAddingState(val onAdding: Boolean): BookWriterAction
    data class UpdateAddIndex(val newAddIndex: Int): BookWriterAction
    data class UpdateAddType(val newAddType: ParagraphType): BookWriterAction
    data class UpdateSelectedItem(val selectedItem: Int): BookWriterAction
    data class UpdateTriggerScroll(val triggerScroll: Boolean): BookWriterAction

    data object ToggleBold: BookWriterAction
    data object ToggleItalic: BookWriterAction
    data object ToggleUnderline: BookWriterAction
    data object ToggleStrikethrough: BookWriterAction
    data object ToggleAlign: BookWriterAction

    data class UpdateBoldState(val boldState: Boolean): BookWriterAction
    data class UpdateItalicState(val italicState: Boolean): BookWriterAction
    data class UpdateUnderlineState(val underlineState: Boolean): BookWriterAction
    data class UpdateStrikethroughState(val strikethroughState: Boolean): BookWriterAction
    data class UpdateAlignState(val alignState: Int): BookWriterAction
}