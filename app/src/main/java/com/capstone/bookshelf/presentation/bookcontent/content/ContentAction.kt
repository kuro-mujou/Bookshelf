package com.capstone.bookshelf.presentation.bookcontent.content

import android.content.Context
import android.net.Uri
import android.speech.tts.Voice
import com.capstone.bookshelf.domain.wrapper.Book
import com.capstone.bookshelf.presentation.bookcontent.drawer.component.bookmark.BookmarkStyle
import java.util.Locale

sealed interface ContentAction {
    data object LoadBook : ContentAction
    data object UpdateBookAsRecentRead : ContentAction
    data class SelectedBook(val book: Book) : ContentAction
    data class UpdateBookTitle(val title: String) : ContentAction
    data class UpdateBookAuthors(val authors: String) : ContentAction
    data class UpdateCoverImage(val context: Context, val uri: Uri,val path: String) : ContentAction

    data class UpdateFlagTriggerScrollForNote(val value: Int) : ContentAction
    data class UpdateFlagTriggerAdjustScroll(val value: Boolean) : ContentAction
    data class UpdateFlagStartScrolling(val value: Boolean) : ContentAction
    data class UpdateFlagScrollAdjusted(val value: Boolean) : ContentAction
    data class UpdateFlagStartAdjustScroll(val value: Boolean) : ContentAction
    data class UpdateFirstVisibleItemIndex(val index: Int) : ContentAction
    data class UpdateLastVisibleItemIndex(val index: Int) : ContentAction
    data class UpdateCurrentChapterIndex(val index: Int) : ContentAction
    data class UpdatePreviousChapterIndex(val index: Int) : ContentAction
    data class UpdateScreenWidth(val value: Int) : ContentAction
    data class UpdateScreenHeight(val value: Int) : ContentAction
    data class UpdateBookInfoCurrentChapterIndex(val index: Int) : ContentAction
    data class UpdateBookInfoFirstParagraphIndex(val index: Int) : ContentAction
    data class UpdateChapterHeader(val header: String) : ContentAction
    data class UpdateEnablePagerScroll(val enable: Boolean) : ContentAction
    data class UpdateEnableUndoButton(val enable: Boolean) : ContentAction

    data class UpdateIsSpeaking(val isSpeaking: Boolean) : ContentAction
    data class UpdateIsPaused(val isPaused: Boolean) : ContentAction
    data class UpdateIsFocused(val isFocused: Boolean) : ContentAction
    data class UpdateTTSLanguage(val currentLanguage: Locale) : ContentAction
    data class UpdateTTSVoice(val currentVoice: Voice?) : ContentAction
    data class UpdateTTSSpeed(val currentSpeed: Float) : ContentAction
    data class UpdateTTSPitch(val currentPitch: Float) : ContentAction
    data class UpdateEnableBackgroundMusic(val enable: Boolean) : ContentAction
    data class UpdatePlayerVolume(val volume: Float) : ContentAction
    data class UpdateCurrentReadingParagraph(val pos: Int) : ContentAction

    data class UpdateSelectedFontFamilyIndex(val index: Int) : ContentAction
    data class UpdateFontSize(val fontSize: Int) : ContentAction
    data class UpdateTextAlign(val textAlign: Boolean) : ContentAction
    data class UpdateTextIndent(val textIndent: Boolean) : ContentAction
    data class UpdateLineSpacing(val lineSpacing: Int) : ContentAction
    data class UpdateImagePaddingState(val imagePaddingState: Boolean) : ContentAction
    data class UpdateKeepScreenOn(val keepScreenOn: Boolean) : ContentAction
    data class UpdateSelectedBookmarkStyle(val style: BookmarkStyle) : ContentAction

    data class GetChapterContent(val index: Int) : ContentAction
}