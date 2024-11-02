package com.capstone.bookshelf.feature.readbook.presentation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.core.data.BookRepository
import com.capstone.bookshelf.core.domain.BookEntity
import com.capstone.bookshelf.core.domain.ChapterContentEntity
import com.capstone.bookshelf.core.domain.TableOfContentEntity
import com.capstone.bookshelf.core.util.RequestState
import com.capstone.bookshelf.feature.readbook.presentation.component.content.HeaderText
import com.capstone.bookshelf.feature.readbook.presentation.component.content.ImageComponent
import com.capstone.bookshelf.feature.readbook.presentation.component.content.ParagraphText
import com.capstone.bookshelf.feature.readbook.presentation.component.content.ZoomableImage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BookContentViewModel(
    private val repository: BookRepository,
    private val bookId: Int
) : ViewModel() {

    private val _tableOfContents : MutableState<List<TableOfContentEntity>> = mutableStateOf(emptyList())
    val tableOfContents: State<List<TableOfContentEntity>> = _tableOfContents

    private var _book: MutableState<RequestState<BookEntity>> =
        mutableStateOf(RequestState.Loading)
    val book: State<RequestState<BookEntity>> = _book

    private val _chapterContent: MutableState<ChapterContentEntity?> = mutableStateOf(null)
    val chapterContent: State<ChapterContentEntity?> = _chapterContent


    init {
        viewModelScope.launch {
            repository.getBookById(bookId).collectLatest {data->
                _book.value = RequestState.Success(
                    data = data
                )
            }

        }
    }
    fun getTableOfContents(bookId: Int) {
        viewModelScope.launch {
            _tableOfContents.value = repository.getTableOfContents(bookId)
        }
    }
    suspend fun getChapterContent(tocId: Int){
        _chapterContent.value = repository.getChapterContent(bookId,tocId)
    }
    fun saveBookInfo(bookId: Int,chapterId: Int){
        viewModelScope.launch {
            repository.saveBookInfo(bookId,chapterId)
        }
    }
    @SuppressLint("SdCardPath")
    fun parseListToComposableList(
        textStyle: TextStyle,
        paragraphs: List<String>
    ): MutableList<@Composable (Boolean,Boolean) -> Unit>{
        val composable = mutableListOf<@Composable (Boolean,Boolean) -> Unit>()
        convertToAnnotatedStrings(paragraphs).forEach {
            val linkPattern = Regex("""/data/user/0/com\.capstone\.bookshelf/files/[^ ]*""")
            val headerPatten = Regex("""<h([1-6])[^>]*>(.*?)</h([1-6])>""")
            if(linkPattern.containsMatchIn(it)) {
                composable.add{ _, _ ->
                    ImageComponent(ZoomableImage(it.text))
                }
            }else if(headerPatten.containsMatchIn(it)) {
                composable.add { isHighlighted, isSpeaking ->
                    HeaderText(it.toString(), textStyle, isHighlighted, isSpeaking)
                }
            } else{
                composable.add {isHighlighted,isSpeaking ->
                    ParagraphText(it,textStyle,isHighlighted,isSpeaking)
                }
            }
        }
        return composable
    }

    @SuppressLint("SdCardPath")
    private fun cleanString(input: String): String {
        val htmlTagPattern = Regex(pattern = """<[^>]+>""")
        val linkPattern = Regex("""/data/user/0/com\.capstone\.bookshelf/files/[^ ]*""")
        var result = htmlTagPattern.replace(input, replacement = "")
        result = linkPattern.replace(result, replacement = " ")
        return result
    }
    private fun removeHtmlTagsFromList(list: List<String>): List<String> {
        return list.map { cleanString(it) }
    }
    private fun convertToAnnotatedStrings(paragraphs: List<String>): List<AnnotatedString> {
        return paragraphs.map { paragraph ->
            buildAnnotatedString {
                val stack = mutableListOf<String>()
                var currentIndex = 0

                while (currentIndex < paragraph.length) {
                    when {
                        paragraph.startsWith("<b>", currentIndex) -> {
                            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                            stack.add("b")
                            currentIndex += 3
                        }
                        paragraph.startsWith("</b>", currentIndex) -> {
                            if (stack.lastOrNull() == "b") {
                                pop()
                                stack.removeAt(stack.lastIndex)
                            }
                            currentIndex += 4
                        }
                        paragraph.startsWith("<i>", currentIndex) -> {
                            pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                            stack.add("i")
                            currentIndex += 3
                        }
                        paragraph.startsWith("</i>", currentIndex) -> {
                            if (stack.lastOrNull() == "i") {
                                pop()
                                stack.removeAt(stack.lastIndex)
                            }
                            currentIndex += 4
                        }
                        paragraph.startsWith("<u>", currentIndex) -> {
                            pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                            stack.add("u")
                            currentIndex += 3
                        }
                        paragraph.startsWith("</u>", currentIndex) -> {
                            if (stack.lastOrNull() == "u") {
                                pop()
                                stack.removeAt(stack.lastIndex)
                            }
                            currentIndex += 4
                        }
                        else -> {
                            append(paragraph[currentIndex])
                            currentIndex++
                        }
                    }
                }
            }
        }
    }
}