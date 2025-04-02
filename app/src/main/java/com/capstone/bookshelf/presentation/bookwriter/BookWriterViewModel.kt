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
import kotlinx.coroutines.flow.update
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
                    val bookIdTemp = BigInteger(1, md.digest((action.bookTitle + "(Draft)").toByteArray())).toString(16).padStart(32, '0')
                    val book = BookEntity(
                        bookId = bookIdTemp,
                        title = action.bookTitle + "(Draft)",
                        authors = listOf(action.authorName),
                        coverImagePath = coverImagePath,
                        categories = emptyList(),
                        description = null,
                        totalChapter = 0,
                        currentChapter = 0,
                        currentParagraph = 0,
                        storagePath = null,
                        isEditable = true,
                        fileType = "editable epub"
                    )
                    _book.value = EmptyBook(
                        id = bookIdTemp,
                        title = action.bookTitle + "(Draft)",
                        authors = listOf(action.authorName),
                        coverImagePath = coverImagePath,
                        categories = emptyList(),
                        description = null,
                        totalChapter = 0,
                        currentChapter = 0,
                        currentParagraph = 0,
                        storagePath = null,
                        isEditable = true,
                        fileType = "editable epub"
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
                _bookWriterState.update { it.copy(
                    addIndex = action.newAddIndex
                ) }
            }
            is BookWriterAction.UpdateAddType -> {
                _bookWriterState.update { it.copy(
                    addType = action.newAddType
                ) }
            }
            is BookWriterAction.UpdateAddingState -> {
                _bookWriterState.update { it.copy(
                    addingState = action.onAdding
                ) }
            }
            is BookWriterAction.UpdateSelectedItem -> {
                _bookWriterState.update { it.copy(
                    selectedItem = action.selectedItem
                ) }
            }
            is BookWriterAction.UpdateTriggerScroll -> {
                _bookWriterState.update { it.copy(
                    triggerScroll = action.triggerScroll
                ) }
            }
            is BookWriterAction.ToggleBold -> {
                _bookWriterState.update { it.copy(
                    toggleBold = !it.toggleBold
                ) }
//                _bookWriterState.value = _bookWriterState.value.copy(
//                    toggleBold = !_bookWriterState.value.toggleBold
//                )
            }
            is BookWriterAction.ToggleItalic -> {
                _bookWriterState.update { it.copy(
                    toggleItalic = !it.toggleItalic
                ) }
//                _bookWriterState.value = _bookWriterState.value.copy(
//                    toggleItalic = !_bookWriterState.value.toggleItalic
//                )
            }
            is BookWriterAction.ToggleUnderline -> {
                _bookWriterState.update { it.copy(
                    toggleUnderline = !it.toggleUnderline,
                    toggleStrikethrough = false,
                ) }
//                _bookWriterState.value = _bookWriterState.value.copy(
//                    toggleUnderline = !_bookWriterState.value.toggleUnderline,
//                    toggleStrikethrough = false,
//                )
            }
            is BookWriterAction.ToggleStrikethrough -> {
                _bookWriterState.update { it.copy(
                    toggleStrikethrough = !it.toggleStrikethrough,
                    toggleUnderline = false,
                ) }
//                _bookWriterState.value = _bookWriterState.value.copy(
//                    toggleStrikethrough = !_bookWriterState.value.toggleStrikethrough,
//                    toggleUnderline = false,
//                )
            }
            is BookWriterAction.ToggleAlign -> {
                _bookWriterState.update { it.copy(
                    toggleAlign = if(it.toggleAlign < 3) it.toggleAlign + 1 else 1
                ) }
//                _bookWriterState.value = _bookWriterState.value.copy(
//                    toggleAlign = if(_bookWriterState.value.toggleAlign < 3) _bookWriterState.value.toggleAlign + 1 else 1
//                )
            }
            is BookWriterAction.UpdateAlignState -> {
                _bookWriterState.update { it.copy(
                    toggleAlign = action.alignState
                ) }
//                _bookWriterState.value = _bookWriterState.value.copy(
//                    toggleAlign = action.alignState
//                )
            }
            is BookWriterAction.UpdateBoldState -> {
                _bookWriterState.update { it.copy(
                    toggleBold = action.boldState
                ) }
//                _bookWriterState.value = _bookWriterState.value.copy(
//                    toggleBold = action.boldState
//                )
            }
            is BookWriterAction.UpdateItalicState -> {
                _bookWriterState.update { it.copy(
                    toggleItalic = action.italicState
                ) }
//                _bookWriterState.value = _bookWriterState.value.copy(
//                    toggleItalic = action.italicState
//                )
            }
            is BookWriterAction.UpdateStrikethroughState -> {
                _bookWriterState.update { it.copy(
                    toggleStrikethrough = action.strikethroughState
                ) }
//                _bookWriterState.value = _bookWriterState.value.copy(
//                    toggleStrikethrough = action.strikethroughState
//                )
            }
            is BookWriterAction.UpdateUnderlineState -> {
                _bookWriterState.update { it.copy(
                    toggleUnderline = action.underlineState
                ) }
//                _bookWriterState.value = _bookWriterState.value.copy(
//                    toggleUnderline = action.underlineState
//                )
            }
        }
    }
}