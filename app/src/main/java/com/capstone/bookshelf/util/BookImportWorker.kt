package com.capstone.bookshelf.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.capstone.bookshelf.R
import com.capstone.bookshelf.data.database.entity.BookEntity
import com.capstone.bookshelf.data.database.entity.ChapterContentEntity
import com.capstone.bookshelf.data.database.entity.TableOfContentEntity
import com.capstone.bookshelf.domain.book.BookRepository
import com.capstone.bookshelf.domain.book.ChapterRepository
import com.capstone.bookshelf.domain.book.ImagePathRepository
import com.capstone.bookshelf.domain.book.TableOfContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.domain.TOCReference
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest

class BookImportWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    companion object {
        const val BOOK_TITLE_KEY = "book_title"
        const val BOOK_CACHE_PATH_KEY = "book_cache_path"
        const val NOTIFICATION_CHANNEL_ID = "book_import_channel"
        const val NOTIFICATION_ID = 1234
    }

    private val bookRepository: BookRepository by inject()
    private val tableOfContentsRepository: TableOfContentRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val imagePathRepository: ImagePathRepository by inject()
    private val md = MessageDigest.getInstance("MD5")
    private var tableOfContents = mutableListOf<TOCReference>()
    private var totalChapters = 0


    override suspend fun doWork(): Result {
        val bookTitle = inputData.getString(BOOK_TITLE_KEY)
            ?: return Result.failure()
        setForeground(createForegroundInfo("Importing $bookTitle..."))
        val cacheFilePath = inputData.getString(BOOK_CACHE_PATH_KEY)
        val bookID = BigInteger(1, md.digest(bookTitle.toByteArray())).toString(16)
            .padStart(32, '0')
        val book = withContext(Dispatchers.IO) {
            loadBookFromFile(cacheFilePath!!)
        } ?: return Result.failure()

        return try {
            saveBookInfo(book, context, bookID, cacheFilePath!!)
            saveBookContent(bookID, book, context)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        } finally {
            deleteCacheFile(cacheFilePath!!)
        }
    }

    private suspend fun saveBookInfo(book: Book, context: Context, bookID: String, cacheFilePath: String): Long {
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
        imagePathRepository.saveImagePath(bookID, listOf(coverImagePath))
        tableOfContents = flattenTocReferences(book.tableOfContents.tocReferences)
        totalChapters = tableOfContents.size
        val bookEntity = BookEntity(
            bookId = bookID,
            title = book.title,
            coverImagePath = coverImagePath,
            authors = normalizeAuthorName(book.metadata.authors),
            categories = book.metadata.types,
            description = null,
            totalChapter = totalChapters,
            storagePath = cacheFilePath,
            ratingsAverage = 0.0,
            ratingsCount = 0
        )
        return bookRepository.insertBook(bookEntity)
    }

    private suspend fun saveBookContent(
        bookID: String,
        book: Book,
        context: Context
    ) {
        tableOfContents.forEachIndexed { index, tocReference ->
            val tocEntity = TableOfContentEntity(
                bookId = bookID,
                title = tocReference.title,
                index = index
            )
            tableOfContentsRepository.saveTableOfContent(tocEntity)
            val chapterContent = loadChapterContent(tableOfContents, index)
            var paragraphs: List<String>
            var imagePath: List<String>
            chapterContent?.let {
                parseChapterToList(
                    html = it,
                    book = book,
                    context = context,
                    bookID = bookID,
                    tocID = index
                )
            }.also {
                paragraphs = it?.first!!
                imagePath = it.second
            }
            imagePathRepository.saveImagePath(bookID, imagePath)
            val chapterEntity = ChapterContentEntity(
                tocId = index,
                bookId = bookID,
                chapterTitle = tableOfContents[index].title,
                content = paragraphs,
            )
            chapterRepository.saveChapterContent(chapterEntity)
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

    private fun normalizeAuthorName(authors: List<Author>): List<String> {
        val normalizedName = mutableListOf<String>()
        authors.forEach { author ->
            normalizedName.add(author.firstname + " " + author.lastname)
        }
        return normalizedName
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
        bookID: String,
        tocID: Int,
    ): Pair<List<String>, List<String>> {
        val document = Jsoup.parse(html)
        val paragraphs = mutableListOf<String>()
        val pathList = mutableListOf<String>()
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
                                        pathList.add(imagePath)
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

        return Pair(paragraphs, pathList)
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
    private fun loadBookFromFile(filePath: String): Book? {
        return try {
            val file = File(filePath)
            nl.siegmann.epublib.epub.EpubReader().readEpub(file.inputStream())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun deleteCacheFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun createForegroundInfo(message: String): ForegroundInfo {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Book Import",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Importing Book")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }
}