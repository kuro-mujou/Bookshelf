package com.capstone.bookshelf.presentation.bookwriter

import android.graphics.BitmapFactory
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.data.database.entity.BookEntity
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.domain.repository.ChapterRepository
import com.capstone.bookshelf.domain.repository.ImagePathRepository
import com.capstone.bookshelf.domain.repository.TableOfContentRepository
import com.capstone.bookshelf.domain.wrapper.EmptyBook
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
        }
    }
}