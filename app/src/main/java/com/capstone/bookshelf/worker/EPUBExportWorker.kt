package com.capstone.bookshelf.worker

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.domain.repository.ChapterRepository
import com.capstone.bookshelf.domain.repository.ImagePathRepository
import com.capstone.bookshelf.domain.repository.TableOfContentRepository
import com.capstone.bookshelf.util.calculateHeaderSizes
import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubWriter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EPUBExportWorker(
    private val appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val bookRepository: BookRepository by inject()
    private val tableOfContentsRepository: TableOfContentRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val imagePathRepository: ImagePathRepository by inject()

    companion object {
        const val SAVE_URI = "saveUri"
        const val BOOK_ID = "bookId"
        const val FONT_SIZE = "fontSize"
    }

    override suspend fun doWork(): Result {
        try {
            val uriString = inputData.getString(SAVE_URI) ?: return Result.failure()
            val bookId = inputData.getString(BOOK_ID) ?: return Result.failure()
            val book = bookRepository.getBook(bookId) ?: return Result.failure()
            val fontSize = inputData.getFloat(FONT_SIZE, 16f)
            val tableOfContents = tableOfContentsRepository.getTableOfContents(bookId)
            val uri = uriString.toUri()
            appContext.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val book = Book().apply {
                    metadata.addTitle(book.title.replace(Regex("\\s*\\(Draft\\)$"),""))
                    book.authors.forEach {
                        metadata.addAuthor(Author(it,""))
                    }
                    tableOfContents.forEachIndexed { index, toc ->
                        val chapter = chapterRepository.getChapterContent(bookId, toc.index)
                        val html = buildChapterHtml(chapter?.content!!, fontSize)
                        addSection(
                            toc.title,
                            Resource(html.toByteArray(), "chapter${index + 1}.html")
                        )
                    }
                }
                EpubWriter().write(book, outputStream)
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }

    private fun buildChapterHtml(paragraphs: List<String>, fontSize: Float): String {
        if (paragraphs.isEmpty()) return ""

        val headingSizes = calculateHeaderSizes(fontSize)

        val firstParagraph = paragraphs.first()
        val otherParagraphs = paragraphs.drop(1)

        val headerConverted = convertParagraphToHeader(firstParagraph, headingSizes)

        return buildString {
            append(headerConverted)
            otherParagraphs.forEach { paragraphHtml ->
                append(paragraphHtml)
            }
        }
    }

    private fun convertParagraphToHeader(paragraphHtml: String, headingSizes: Array<Float>): String {
        val regex = Regex("font-size:\\s*(\\d+(?:\\.\\d+)?)px", RegexOption.IGNORE_CASE)
        val match = regex.find(paragraphHtml)
        val fontSizeInParagraph = match?.groupValues?.get(1)?.toFloatOrNull()
        if (fontSizeInParagraph != null) {
            val headerLevel = headingSizes.indexOfFirst { size ->
                kotlin.math.abs(size - fontSizeInParagraph) < 1.0f
            }
            if (headerLevel != -1) {
                val headingTag = "h${headerLevel + 1}"
                val innerText = paragraphHtml
                    .replace(Regex("<p[^>]*>", RegexOption.IGNORE_CASE), "")
                    .replace("</p>", "")
                    .trim()
                return "<$headingTag>$innerText</$headingTag>"
            }
        }
        return paragraphHtml
    }
}