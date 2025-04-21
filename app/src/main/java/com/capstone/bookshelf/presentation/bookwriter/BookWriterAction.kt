package com.capstone.bookshelf.presentation.bookwriter

import android.content.Context
import androidx.core.uri.Uri
import com.capstone.bookshelf.presentation.bookwriter.component.Paragraph

sealed interface BookWriterAction {
    data class AddBookInfo(
        val context: Context,
        val bookTitle: String,
        val authorName: String,
        val coverImagePath: String
    ) : BookWriterAction

    data class AddChapter(
        val chapterTitle: String,
        val headerSize: String,
        val totalTocSize: Int,
        val currentFontSize: Float
    ) : BookWriterAction

    data class AddImage(
        val context: Context,
        val chapterIndex: Int,
        val paragraphIndex: Int,
        val paragraphId: String,
        val imageUri: Uri
    ) : BookWriterAction

    data class AddParagraphAbove(
        val anchorParagraphId: String,
        val newParagraph: Paragraph
    ) : BookWriterAction

    data class AddParagraphBelow(
        val anchorParagraphId: String,
        val newParagraph: Paragraph
    ) : BookWriterAction

    data class UpdateItemMenuVisible(
        val paragraphId: String,
        val visible: Boolean
    ) : BookWriterAction

    data class SaveChapter(val currentChapterIndex: Int) : BookWriterAction
    data class MoveParagraphUp(val paragraphId: String) : BookWriterAction
    data class MoveParagraphDown(val paragraphId: String) : BookWriterAction
    data class DeleteParagraph(val paragraphId: String) : BookWriterAction
    data class UpdateTriggerLoadChapter(val triggerLoadChapter: Boolean) : BookWriterAction
    data class UpdateSelectedItem(val selectedItem: String) : BookWriterAction
    data class UpdateTriggerScroll(val triggerScroll: Boolean) : BookWriterAction
    data class SetFocusTarget(val paragraphId: String) : BookWriterAction
}