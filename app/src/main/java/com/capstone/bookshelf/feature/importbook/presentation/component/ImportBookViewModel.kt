package com.capstone.bookshelf.feature.importbook.presentation.component

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.core.data.BookRepository
import com.capstone.bookshelf.core.domain.BookEntity
import com.capstone.bookshelf.core.domain.ChapterContentEntity
import com.capstone.bookshelf.core.domain.TableOfContentEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.domain.TOCReference
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImportBookViewModel(
    private val repository: BookRepository
) : ViewModel() {

    private val _totalChapters = mutableIntStateOf(0)
    private var tableOfContents = mutableListOf<TOCReference>()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> get() = _progress

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> get() = _message


    init {
        _isLoading.value = false
        _totalChapters.intValue = 0
        _progress.value = 0f
        _message.value = ""
    }

    fun processAndSaveBook(
        book: Book,
        context: Context,
        cacheFilePath: String
    ) = viewModelScope.launch {
            _isLoading.value = true
            try {
                val title = book.title
                val isAlreadyImported = repository.isBookAlreadyImported(title)
                if (isAlreadyImported) {
                    Toast.makeText(context, "Book already imported", Toast.LENGTH_SHORT).show()
                } else {
                    _message.value = "Saving book info..."
                    val bookID = saveBookInfo(book, context)
                    yield()
                    _message.value = "Saving chapters..."
                    val tocIDs = saveTableOfContents(bookID)
                    yield()
                    _totalChapters.intValue = tocIDs.size
                    processAndSaveChapters(book, bookID, tocIDs, context)
                    Toast.makeText(context, "Successfully saved book", Toast.LENGTH_SHORT).show()
                }
            } finally {
                _isLoading.value = false
                _totalChapters.intValue = 0
                _progress.value = 0f
                _message.value = ""
                deleteCacheFile(context, cacheFilePath)
            }
        }

    private suspend fun saveBookInfo(book: Book, context: Context): Long {
        val coverImage = try {
            book.coverImage
        } catch (e: Exception) {
            null
        }
        val coverImagePath = if (coverImage != null) {
            saveImageToPrivateStorage(context, book.coverImage, "cover_${book.title}")
        } else {
            "error"
        }
        tableOfContents = flattenTocReferences(book.tableOfContents.tocReferences)
        _totalChapters.intValue = tableOfContents.size
        val bookEntity = BookEntity(
            title = book.title,
            coverImagePath = coverImagePath,
            author = normalizeAuthorName(book.metadata.authors),
            totalChapter = _totalChapters.intValue
        )
        _progress.value = (1f / (_totalChapters.intValue + 2).toFloat())
        return repository.saveBook(bookEntity)
    }

    private suspend fun saveTableOfContents(bookID: Long): List<Int> {
        val tocIDs = mutableListOf<Int>()
        tableOfContents.forEachIndexed { index, tocReference ->
            val tocEntity = TableOfContentEntity(
                bookId = bookID.toInt(),
                title = tocReference.title,
                index = index
            )
            repository.saveTableOfContent(tocEntity)
            tocIDs.add(index)
            yield()
        }
        _progress.value = (2f / (_totalChapters.intValue + 2).toFloat())
        return tocIDs
    }

    private suspend fun processAndSaveChapters(
        book: Book,
        bookID: Long,
        tocIDs: List<Int>,
        context: Context
    ) {
        tocIDs.forEachIndexed { i, id ->
            val chapterContent = loadChapterContent(tableOfContents, i)
            val paragraphs = chapterContent?.let {
                parseChapterToList(
                    html = it,
                    book = book,
                    context = context,
                    bookID = bookID.toInt(),
                    tocID = id
                )
            }
            yield()
            val chapterEntity = paragraphs?.let {
                ChapterContentEntity(
                    tocId = id,
                    bookId = bookID.toInt(),
                    chapterTitle = tableOfContents[i].title,
                    content = it,
                )
            }
            if (chapterEntity != null) {
                repository.saveChapterContent(chapterEntity)
                yield()
            }
            _progress.value = ((i + 3).toFloat() / (_totalChapters.intValue + 2).toFloat())
            _message.value = "Saving chapter ${i+1} of ${_totalChapters.intValue}..."
            yield()
        }
    }

    private fun flattenTocReferences(tocReferences: List<TOCReference>): MutableList<TOCReference> {
        val flattenedReferences = mutableListOf<TOCReference>()
        tocReferences.forEach { reference ->
            flattenedReferences.add(reference)
            if (reference.children != null) {
                flattenedReferences.addAll(flattenTocReferences(reference.children))
            }
        }
        return flattenedReferences
    }

    private fun normalizeAuthorName(authors: List<Author>): String {
        val normalizedName = mutableListOf<String>()
        authors.forEach{author ->
            normalizedName.add(author.firstname + " " + author.lastname)
        }
        return normalizedName.joinToString(", ")
    }

    private fun loadChapterContent(
        tableOfContents: List<TOCReference>,
        chapterIndex: Int
    ): String? {
        var chapterContent = ""
        try {
            if (chapterIndex in tableOfContents.indices) {
                val resource = tableOfContents[chapterIndex].resource
                val chapterText = resource.inputStream.bufferedReader().use { it.readText() }
                chapterContent = chapterText
            }
        } catch (e: Exception) {
            return null
        }
        return chapterContent
    }

    @Suppress("DEPRECATION")
    private fun saveImageToPrivateStorage(
        context: Context,
        imageResource: Resource,
        filename: String
    ): String {
        return try {
            val bitmap = BitmapFactory.decodeStream(imageResource.inputStream)

            val file = File(context.filesDir, "$filename.webp")
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.WEBP, 80, outputStream)
            }
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            "error when loading image"
        }
    }
    private fun parseChapterToList(
        html: String,
        book: Book,
        context: Context,
        bookID: Int,
        tocID: Int
    ): List<String> {
        val document = Jsoup.parse(html)
        val paragraphs = mutableListOf<String>()
        var shouldAddSpace = false
        var currentParagraph = StringBuilder()
        var i = 0

        document.body().traverse(object : NodeVisitor {
            override fun head(node: Node, depth: Int) {
                when (node) {
                    is TextNode -> {
                        val text = node.text().trim()
                        var trimmedText = ""
                        if (text.isNotEmpty()) {
                            if (shouldAddSpace) {
                                trimmedText = " $text "
                                shouldAddSpace = false
                            } else
                                trimmedText = text
                        }
                        if (trimmedText.isNotEmpty()) {
                            currentParagraph.append(trimmedText)
                        }
                    }
                    is Element -> {
                        when (node.tagName()) {
                            "p", "div" -> {
                                if (currentParagraph.isNotEmpty()) {
                                    paragraphs.add(currentParagraph.toString().trim())
                                    currentParagraph = StringBuilder()
                                }
                            }
                            "br" -> {
                                if (currentParagraph.isNotEmpty()) {
                                    paragraphs.add(currentParagraph.toString().trim())
                                    currentParagraph = StringBuilder()
                                }
                            }
                            "b", "strong" -> {
                                currentParagraph.append("<b>")
                                shouldAddSpace = true
                            }
                            "i", "em" -> {
                                currentParagraph.append("<i>")
                                shouldAddSpace = true
                            }
                            "u" -> {
                                currentParagraph.append("<u>")
                                shouldAddSpace = true
                            }
                            "h1" -> {
                                if (currentParagraph.isNotEmpty()) {
                                    paragraphs.add(currentParagraph.toString().trim())
                                }
                                currentParagraph = StringBuilder()
                                currentParagraph.append("<h1>")
                            }
                            "h2" -> {
                                if (currentParagraph.isNotEmpty()) {
                                    paragraphs.add(currentParagraph.toString().trim())
                                }
                                currentParagraph = StringBuilder()
                                currentParagraph.append("<h2>")
                            }
                            "h3" -> {
                                if (currentParagraph.isNotEmpty()) {
                                    paragraphs.add(currentParagraph.toString().trim())
                                }
                                currentParagraph = StringBuilder()
                                currentParagraph.append("<h3>")
                            }
                            "h4" -> {
                                if (currentParagraph.isNotEmpty()) {
                                    paragraphs.add(currentParagraph.toString().trim())
                                }
                                currentParagraph = StringBuilder()
                                currentParagraph.append("<h4>")
                            }
                            "h5" -> {
                                if (currentParagraph.isNotEmpty()) {
                                    paragraphs.add(currentParagraph.toString().trim())
                                }
                                currentParagraph = StringBuilder()
                                currentParagraph.append("<h5>")
                            }
                            "h6" -> {
                                if (currentParagraph.isNotEmpty()) {
                                    paragraphs.add(currentParagraph.toString().trim())
                                }
                                currentParagraph = StringBuilder()
                                currentParagraph.append("<h6>")
                            }
                            "img" -> {
                                try {
                                    val src = node.attr("src")
                                    val actualSrc = src.replace("../", "")
                                    val resource = getImageResourceFromBook(actualSrc, book)
                                    resource?.let {
                                        val imagePath = saveImageToPrivateStorage(
                                            context = context,
                                            imageResource = it,
                                            filename = "image_${bookID}_${tocID}_${i}"
                                        )
                                        if (imagePath.isNotEmpty()) {
                                            if (currentParagraph.isNotEmpty()) {
                                                paragraphs.add(currentParagraph.toString().trim())
                                                currentParagraph = StringBuilder()
                                            }
                                            paragraphs.add(imagePath)
                                            i++
                                        }
                                    }
                                } catch (e: Exception) {
                                    currentParagraph.append("Error when loading image")
                                    paragraphs.add(currentParagraph.toString().trim())
                                    currentParagraph = StringBuilder()
                                }
                            }
                        }
                    }
                }
            }

            override fun tail(node: Node, depth: Int) {
                if (node is Element) {
                    when (node.tagName()) {
                        "b", "strong" -> currentParagraph.append("</b>")
                        "i", "em" -> currentParagraph.append("</i>")
                        "u" -> currentParagraph.append("</u>")
                        "h1", "h2", "h3", "h4", "h5", "h6" -> {
                            currentParagraph.append("</h${node.tagName().last()}>")
                            paragraphs.add(currentParagraph.toString().trim())
                            currentParagraph = StringBuilder()
                        }
                    }
                }
            }
        })

        if (currentParagraph.isNotEmpty()) {
            paragraphs.add(currentParagraph.toString().trim())
        }

        return paragraphs
    }
    private fun getImageResourceFromBook(
        src: String,
        book: Book
    ): Resource? {
        return if (book.resources.containsByHref(src))
            book.resources.getByHref(src)
        else if (book.resources.containsByHref("OEBPS/$src"))
            book.resources.getByHref("OEBPS/$src")
        else if (book.resources.containsByHref("epub/$src"))
            book.resources.getByHref("epub/$src")
        else if (book.resources.containsByHref("images/$src"))
            book.resources.getByHref("images/$src")
        else if (book.resources.containsByHref("epub/images/$src"))
            book.resources.getByHref("epub/images/$src")
        else if (book.resources.containsByHref("OEBPS/images/$src"))
            book.resources.getByHref("OEBPS/images/$src")
        else
            null
    }
    private fun deleteCacheFile(context: Context, filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }
    }
}

