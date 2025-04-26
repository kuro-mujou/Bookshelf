package com.capstone.bookshelf.presentation.bookwriter

import android.graphics.BitmapFactory
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.capstone.bookshelf.app.Route
import com.capstone.bookshelf.data.database.entity.BookEntity
import com.capstone.bookshelf.data.database.entity.ChapterContentEntity
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.domain.repository.ChapterRepository
import com.capstone.bookshelf.domain.repository.ImagePathRepository
import com.capstone.bookshelf.domain.repository.TableOfContentRepository
import com.capstone.bookshelf.domain.wrapper.EmptyBook
import com.capstone.bookshelf.domain.wrapper.TableOfContent
import com.capstone.bookshelf.presentation.bookwriter.component.FormatAction
import com.capstone.bookshelf.presentation.bookwriter.component.Paragraph
import com.capstone.bookshelf.presentation.bookwriter.component.ParagraphType
import com.capstone.bookshelf.util.calculateHeaderSizes
import com.capstone.bookshelf.util.deleteImageFromPrivateStorage
import com.capstone.bookshelf.util.saveImageToPrivateStorage
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.math.BigInteger
import java.security.MessageDigest
import java.util.Collections
import java.util.UUID

class BookWriterViewModel(
    private val bookRepository: BookRepository,
    private val tableOfContentsRepository: TableOfContentRepository,
    private val chapterRepository: ChapterRepository,
    private val imagePathRepository: ImagePathRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val selectedBookId = savedStateHandle.toRoute<Route.BookContent>().bookId
    private val md = MessageDigest.getInstance("MD5")
    private val _bookID = MutableStateFlow("")
    private val _book = MutableStateFlow(EmptyBook())

    val bookID: StateFlow<String> = _bookID.asStateFlow()
    val book: StateFlow<EmptyBook> = _book.asStateFlow()

    private val _bookWriterState = MutableStateFlow(BookWriterState())
    val bookWriterState: StateFlow<BookWriterState> = _bookWriterState.asStateFlow()

    fun onAction(action: BookWriterAction) {
        when (action) {
            is BookWriterAction.AddBookInfo -> {
                viewModelScope.launch {
                    val randomId = UUID.randomUUID().toString()
                    var coverImagePath = action.coverImagePath
                    if (coverImagePath != "error") {
                        action.context.contentResolver.openInputStream(coverImagePath.toUri()).use {
                            val bitmap = BitmapFactory.decodeStream(it)
                            coverImagePath = saveImageToPrivateStorage(
                                context = action.context,
                                bitmap = bitmap,
                                filename = "cover_${randomId}"
                            )
                        }
                    }
                    val bookIdTemp = BigInteger(
                        1,
                        md.digest(("$randomId(Draft)").toByteArray())
                    ).toString(16).padStart(32, '0')
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
                viewModelScope.launch {
                    tableOfContentsRepository.updateTableOfContentIndexOnInsert(selectedBookId,action.currentTocIndex)
                    chapterRepository.updateChapterIndexOnInsert(selectedBookId,action.currentTocIndex)
                    yield()
                    val newChapter = TableOfContent(
                        bookId = selectedBookId,
                        title = action.chapterTitle,
                        index = if(action.totalTocSize == 0) 0 else action.currentTocIndex + 1,
                        isFavorite = false
                    )
                    tableOfContentsRepository.addChapter(selectedBookId, newChapter)
                    bookRepository.saveBookInfoTotalChapter(selectedBookId, action.totalTocSize + 1)
                    bookRepository.saveBookInfoChapterIndex(selectedBookId, action.currentTocIndex + 1)
                    val contentList = mutableListOf<String>()
                    val headingHtml =
                        "<${action.headerSize.lowercase()}>${action.chapterTitle}</${action.headerSize.lowercase()}>"
                    val headingContent = preprocessHtmlHeadings(headingHtml, action.currentFontSize)
                    contentList.add(headingContent)
                    val newChapterContent = ChapterContentEntity(
                        bookId = selectedBookId,
                        tocId = if(action.totalTocSize == 0) 0 else action.currentTocIndex + 1,
                        chapterTitle = action.chapterTitle,
                        content = contentList
                    )
                    chapterRepository.saveChapterContent(newChapterContent)
                    yield()
                    _bookWriterState.update {
                        it.copy(
                            triggerLoadChapter = true
                        )
                    }
                }
            }

            is BookWriterAction.AddImage -> {
                viewModelScope.launch {
                    if (bookWriterState.value.contentList
                            .firstOrNull {
                                it.id == action.paragraphId
                            }?.richTextState?.toText()
                            .isNullOrEmpty()
                    ) {
                        action.context.contentResolver.openInputStream(action.imageUri).use {
                            val bitmap = BitmapFactory.decodeStream(it)
                            val coverImagePath = saveImageToPrivateStorage(
                                context = action.context,
                                bitmap = bitmap,
                                filename = "image_${selectedBookId}_${action.paragraphId}"
                            )
                            imagePathRepository.saveImagePath(
                                selectedBookId,
                                listOf(coverImagePath)
                            )
                            _bookWriterState.update { currentState ->
                                val updatedList = currentState.contentList.map { paragraph ->
                                    if (paragraph.id == action.paragraphId) {
                                        val newRichTextState = paragraph.richTextState.apply {
                                            setText(coverImagePath)
                                        }
                                        paragraph.copy(richTextState = newRichTextState)
                                    } else {
                                        paragraph
                                    }
                                }
                                currentState.copy(contentList = updatedList)
                            }
                        }
                    }
                }
            }

            is BookWriterAction.UpdateSelectedItem -> {
                _bookWriterState.update {
                    it.copy(
                        selectedItem = action.selectedItem
                    )
                }
            }

            is BookWriterAction.UpdateTriggerScroll -> {
                _bookWriterState.update {
                    it.copy(
                        triggerScroll = action.triggerScroll
                    )
                }
            }

            is BookWriterAction.UpdateItemMenuVisible -> {
                _bookWriterState.update { currentState ->
                    val updatedList = currentState.contentList.map { paragraph ->
                        if (paragraph.id == action.paragraphId) {
                            paragraph.copy(isControllerVisible = action.visible)
                        } else {
                            if (paragraph.isControllerVisible) {
                                paragraph.copy(isControllerVisible = false)
                            } else {
                                paragraph
                            }
                        }
                    }
                    currentState.copy(contentList = updatedList)
                }
            }

            is BookWriterAction.AddParagraphAbove -> {
                _bookWriterState.update { currentState ->
                    val currentList = currentState.contentList
                    val anchorIndex = currentList.indexOfFirst { it.id == action.anchorParagraphId }
                    if (anchorIndex < 0) {
                        return@update currentState
                    }
                    if (anchorIndex == 0) {
                        return@update currentState
                    }
                    val insertionIndex = anchorIndex
                    val updatedList = currentList.take(insertionIndex) +
                            listOf(action.newParagraph) +
                            currentList.drop(insertionIndex)
                    currentState.copy(
                        contentList = updatedList,
                        itemToFocusId = action.newParagraph.id
                    )
                }
            }

            is BookWriterAction.AddParagraphBelow -> {
                _bookWriterState.update { currentState ->
                    val currentList = currentState.contentList
                    val anchorIndex = currentList.indexOfFirst { it.id == action.anchorParagraphId }
                    if (anchorIndex < 0) {
                        return@update currentState
                    }
                    val insertionIndex = anchorIndex + 1
                    val updatedList = currentList.take(insertionIndex) +
                            listOf(action.newParagraph) +
                            currentList.drop(insertionIndex)
                    currentState.copy(
                        contentList = updatedList,
                        itemToFocusId = action.newParagraph.id
                    )
                }
            }

            is BookWriterAction.MoveParagraphUp -> {
                _bookWriterState.update { currentState ->
                    val currentList = currentState.contentList
                    val currentIndex = currentList.indexOfFirst { it.id == action.paragraphId }
                    if (currentIndex <= 1) {
                        return@update currentState
                    }
                    val mutableList = currentList.toMutableList()
                    Collections.swap(mutableList, currentIndex, currentIndex - 1)
                    currentState.copy(contentList = mutableList)
                }
            }

            is BookWriterAction.MoveParagraphDown -> {
                _bookWriterState.update { currentState ->
                    val currentList = currentState.contentList
                    val currentIndex = currentList.indexOfFirst { it.id == action.paragraphId }
                    val lastIndex = currentList.lastIndex
                    if (currentIndex < 0 || currentIndex == 0 || currentIndex == lastIndex) {
                        return@update currentState
                    }
                    val mutableList = currentList.toMutableList()
                    Collections.swap(mutableList, currentIndex, currentIndex + 1)
                    currentState.copy(contentList = mutableList)
                }
            }

            is BookWriterAction.DeleteParagraph -> {
                viewModelScope.launch {
                    val paragraphToDelete =
                        _bookWriterState.value.contentList.firstOrNull { it.id == action.paragraphId }
                    if (paragraphToDelete == null) {
                        return@launch
                    }
                    if (paragraphToDelete.type == ParagraphType.IMAGE) {
                        val imagePath =
                            paragraphToDelete.richTextState.toText().takeIf { it.isNotBlank() }
                        if (imagePath != null) {
                            deleteImageFromPrivateStorage(imagePath)
                            imagePathRepository.deleteImagePathByPath(imagePath)
                        }
                    }
                    _bookWriterState.update { currentState ->
                        val currentList = currentState.contentList
                        val indexToDelete = currentList.indexOfFirst { it.id == action.paragraphId }
                        if (indexToDelete <= 0) {
                            return@update currentState
                        }
                        val updatedList =
                            currentList.filterIndexed { index, _ -> index != indexToDelete }
                        currentState.copy(contentList = updatedList)
                    }
                }
            }

            is BookWriterAction.SetFocusTarget -> {
                _bookWriterState.update {
                    it.copy(
                        itemToFocusId = action.paragraphId
                    )
                }
            }

            is BookWriterAction.SaveChapter -> {
                val chapterContents: List<String> = getAllParagraphHtmlContent()
                if (chapterContents.isNotEmpty()) {
                    viewModelScope.launch {
                        chapterRepository.updateChapterContent(
                            selectedBookId,
                            action.currentChapterIndex,
                            chapterContents
                        )
                        tableOfContentsRepository.updateTableOfContentTitle(
                            selectedBookId,
                            action.currentChapterIndex,
                            _bookWriterState.value.htmlTagPattern.replace(
                                _bookWriterState.value.contentList.first().richTextState.toText(),
                                ""
                            )
                        )
                    }
                }
            }

            is BookWriterAction.UpdateTriggerLoadChapter -> {
                _bookWriterState.update {
                    it.copy(
                        triggerLoadChapter = action.triggerLoadChapter
                    )
                }
            }
        }
    }

    fun editTextStyle(action: FormatAction) {
        when (action) {
            FormatAction.BOLD -> {
                _bookWriterState.value.contentList
                    .firstOrNull {
                        it.id == _bookWriterState.value.selectedItem
                    }?.richTextState?.toggleSpanStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    )
            }

            FormatAction.ITALIC -> {
                _bookWriterState.value.contentList
                    .firstOrNull {
                        it.id == _bookWriterState.value.selectedItem
                    }?.richTextState?.toggleSpanStyle(
                        SpanStyle(fontStyle = FontStyle.Italic)
                    )
            }

            FormatAction.UNDERLINE -> {
                _bookWriterState.value.contentList
                    .firstOrNull {
                        it.id == _bookWriterState.value.selectedItem
                    }?.richTextState?.toggleSpanStyle(
                        SpanStyle(textDecoration = TextDecoration.Underline)
                    )
            }

            FormatAction.STRIKETHROUGH -> {
                _bookWriterState.value.contentList
                    .firstOrNull {
                        it.id == _bookWriterState.value.selectedItem
                    }?.richTextState?.toggleSpanStyle(
                        SpanStyle(textDecoration = TextDecoration.LineThrough)
                    )
            }

            FormatAction.CLEAR -> {
                _bookWriterState.value.contentList
                    .firstOrNull {
                        it.id == _bookWriterState.value.selectedItem
                    }?.richTextState?.clearSpanStyles()
            }
        }
    }

    fun loadChapterContent(chapterIndex: Int) {
        viewModelScope.launch {
            val chapter = chapterRepository.getChapterContent(selectedBookId, chapterIndex)
            val newContentList = chapter?.content?.mapIndexed {index, content ->
                val isImage = _bookWriterState.value.linkPattern.containsMatchIn(content)
                Paragraph(
                    type = when {
                        index == 0 -> ParagraphType.TITLE
                        isImage -> ParagraphType.IMAGE
                        else -> ParagraphType.PARAGRAPH
                    },
                    richTextState = RichTextState().apply {
                        setHtml(content)
                    }
                )
            } ?: emptyList()
            _bookWriterState.update {
                it.copy(
                    contentList = newContentList
                )
            }
        }
    }

    fun moveItem(from: Int, to: Int) {
        _bookWriterState.value = _bookWriterState.value.copy(
            contentList = _bookWriterState.value.contentList.move(from, to)
        )
    }

    private fun getAllParagraphHtmlContent(): List<String> {
        val currentContentList = _bookWriterState.value.contentList
        if (currentContentList.isEmpty()) {
            return emptyList()
        }
        return currentContentList.map { paragraph ->
            if(paragraph.type == ParagraphType.IMAGE)
                paragraph.richTextState.toText()
            else
                paragraph.richTextState.toHtml()
        }.filterNot { html ->
            html.trim().matches(Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE))
        }
    }

    private fun preprocessHtmlHeadings(html: String, fontSize: Float): String {
        val headingTextSizes = calculateHeaderSizes(fontSize)
        return html
            .replace(
                Regex("<h1>(.*?)</h1>", RegexOption.IGNORE_CASE),
                "<p style='font-size:${headingTextSizes[0]}px; font-weight:bold; text-align:center;'>$1</p>"
            )
            .replace(
                Regex("<h2>(.*?)</h2>", RegexOption.IGNORE_CASE),
                "<p style='font-size:${headingTextSizes[1]}px; font-weight:bold; text-align:center;'>$1</p>"
            )
            .replace(
                Regex("<h3>(.*?)</h3>", RegexOption.IGNORE_CASE),
                "<p style='font-size:${headingTextSizes[2]}px; font-weight:bold; text-align:center;'>$1</p>"
            )
            .replace(
                Regex("<h4>(.*?)</h4>", RegexOption.IGNORE_CASE),
                "<p style='font-size:${headingTextSizes[3]}px; font-weight:bold; text-align:center;'>$1</p>"
            )
            .replace(
                Regex("<h5>(.*?)</h5>", RegexOption.IGNORE_CASE),
                "<p style='font-size:${headingTextSizes[4]}px; font-weight:bold; text-align:center;'>$1</p>"
            )
            .replace(
                Regex("<h6>(.*?)</h6>", RegexOption.IGNORE_CASE),
                "<p style='font-size:${headingTextSizes[5]}px; font-weight:bold; text-align:center;'>$1</p>"
            )
    }
}

fun <T> List<T>.move(from: Int, to: Int): List<T> {
    if (from !in indices || to !in indices) return this
    if (from == to) return this

    val mutableList = this.toMutableList()
    val item = mutableList.removeAt(from)
    mutableList.add(to, item)
    return mutableList.toList()
}