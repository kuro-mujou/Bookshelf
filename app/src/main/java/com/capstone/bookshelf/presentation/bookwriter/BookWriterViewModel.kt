package com.capstone.bookshelf.presentation.bookwriter

import android.graphics.BitmapFactory
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.focus.FocusRequester
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.data.database.entity.BookEntity
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.domain.repository.ChapterRepository
import com.capstone.bookshelf.domain.repository.ImagePathRepository
import com.capstone.bookshelf.domain.repository.TableOfContentRepository
import com.capstone.bookshelf.domain.wrapper.EmptyBook
import com.capstone.bookshelf.presentation.bookwriter.component.Paragraph
import com.capstone.bookshelf.util.saveImageToPrivateStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.security.MessageDigest

class BookWriterViewModel(
    private val bookRepository: BookRepository,
    private val tableOfContentsRepository: TableOfContentRepository,
    private val chapterRepository: ChapterRepository,
    private val imagePathRepository: ImagePathRepository
) : ViewModel() {
    private val md = MessageDigest.getInstance("MD5")
    private val _bookID = MutableStateFlow("")
    private val _book = MutableStateFlow(EmptyBook())

    val bookID: StateFlow<String> = _bookID.asStateFlow()
    val book: StateFlow<EmptyBook> = _book.asStateFlow()

    private val _bookWriterState = MutableStateFlow(BookWriterState())
    val bookWriterState: StateFlow<BookWriterState> = _bookWriterState.asStateFlow()

    var paragraphList = mutableStateListOf<Paragraph>()
        private set
    var focusRequesterList = mutableStateListOf<FocusRequester>()
        private set

    fun onAction(action: BookWriterAction){
        when(action){
            is BookWriterAction.AddBookInfo ->{
                viewModelScope.launch {
                    var coverImagePath = action.coverImagePath
                    if (coverImagePath != "error") {
                        action.context.contentResolver.openInputStream(coverImagePath.toUri()).use {
                            val bitmap = BitmapFactory.decodeStream(it)
                            coverImagePath = saveImageToPrivateStorage(
                                context = action.context,
                                bitmap = bitmap,
                                filename = "cover_${action.bookTitle}"
                            )
                        }
                    }
                    val bookIdTemp = BigInteger(1, md.digest(action.bookTitle.toByteArray())).toString(16).padStart(32, '0')
                    val book = BookEntity(
                        bookId = bookIdTemp,
                        title = action.bookTitle,
                        authors = listOf(action.authorName),
                        coverImagePath = coverImagePath,
                        categories = emptyList(),
                        description = null,
                        totalChapter = 0,
                        storagePath = null,
                        isEditable = true
                    )
                    _book.value = EmptyBook(
                        id = _bookID.value,
                        title = action.bookTitle,
                        authors = listOf(action.authorName),
                        coverImagePath = coverImagePath,
                        categories = emptyList(),
                        description = null,
                        totalChapter = 0,
                        storagePath = null,
                        isEditable = true
                    )
                    _bookID.value = bookIdTemp
                    bookRepository.insertBook(book)
                    if (coverImagePath != "error") {
                        imagePathRepository.saveImagePath(bookIdTemp, listOf(coverImagePath))
                    }
                }
            }

            is BookWriterAction.AddChapter -> {

            }
            is BookWriterAction.AddImage -> {
                action.context.contentResolver.openInputStream(action.imageUri.toUri()).use {
//                    val bitmap = BitmapFactory.decodeStream(it)
//                    coverImagePath = saveImageToPrivateStorage(
//                        context = action.context,
//                        bitmap = bitmap,
//                        filename = "cover_${action.bookTitle}"
//                    )
                }
            }
            is BookWriterAction.UpdateAddIndex ->{
                _bookWriterState.value = _bookWriterState.value.copy(
                    addIndex = action.newAddIndex
                )
            }
            is BookWriterAction.UpdateAddType -> {
                _bookWriterState.value = _bookWriterState.value.copy(
                    addType = action.newAddType
                )
            }
            is BookWriterAction.UpdateAddingState -> {
                _bookWriterState.value = _bookWriterState.value.copy(
                    addingState = action.onAdding
                )
            }
            is BookWriterAction.UpdateSelectedItem -> {
                _bookWriterState.value = _bookWriterState.value.copy(
                    selectedItem = action.selectedItem
                )
            }
            is BookWriterAction.UpdateTriggerScroll -> {
                _bookWriterState.value = _bookWriterState.value.copy(
                    triggerScroll = action.triggerScroll
                )
            }
            is BookWriterAction.ToggleBold -> {
                _bookWriterState.value = _bookWriterState.value.copy(
                    toggleBold = !_bookWriterState.value.toggleBold
                )
            }
            is BookWriterAction.ToggleItalic -> {
                _bookWriterState.value = _bookWriterState.value.copy(
                    toggleItalic = !_bookWriterState.value.toggleItalic
                )
            }
            is BookWriterAction.ToggleUnderline -> {
                _bookWriterState.value = _bookWriterState.value.copy(
                    toggleUnderline = !_bookWriterState.value.toggleUnderline,
                    toggleStrikethrough = false,
                )
            }
            is BookWriterAction.ToggleStrikethrough -> {
                _bookWriterState.value = _bookWriterState.value.copy(
                    toggleStrikethrough = !_bookWriterState.value.toggleStrikethrough,
                    toggleUnderline = false,
                )
            }
            is BookWriterAction.ToggleAlign -> {
                _bookWriterState.value = _bookWriterState.value.copy(
                    toggleAlign = if(_bookWriterState.value.toggleAlign < 3) _bookWriterState.value.toggleAlign + 1 else 1
                )
            }
            is BookWriterAction.UpdateAlignState -> {
                _bookWriterState.value = _bookWriterState.value.copy(
                    toggleAlign = action.alignState
                )
            }
            is BookWriterAction.UpdateBoldState -> {
                _bookWriterState.value = _bookWriterState.value.copy(
                    toggleBold = action.boldState
                )
            }
            is BookWriterAction.UpdateItalicState -> {
                _bookWriterState.value = _bookWriterState.value.copy(
                    toggleItalic = action.italicState
                )
            }
            is BookWriterAction.UpdateStrikethroughState -> {
                _bookWriterState.value = _bookWriterState.value.copy(
                    toggleStrikethrough = action.strikethroughState
                )
            }
            is BookWriterAction.UpdateUnderlineState -> {
                _bookWriterState.value = _bookWriterState.value.copy(
                    toggleUnderline = action.underlineState
                )
            }
        }
    }
}