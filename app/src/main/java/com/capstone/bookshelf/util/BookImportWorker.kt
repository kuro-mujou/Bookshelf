package com.capstone.bookshelf.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.capstone.bookshelf.R
import com.capstone.bookshelf.data.database.entity.BookEntity
import com.capstone.bookshelf.data.database.entity.ChapterContentEntity
import com.capstone.bookshelf.data.database.entity.TableOfContentEntity
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.domain.repository.ChapterRepository
import com.capstone.bookshelf.domain.repository.ImagePathRepository
import com.capstone.bookshelf.domain.repository.TableOfContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.domain.TOCReference
import nl.siegmann.epublib.epub.EpubReader
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
        const val BOOK_CACHE_PATH_KEY = "book_cache_path"
    }

    private val bookRepository: BookRepository by inject()
    private val tableOfContentsRepository: TableOfContentRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val imagePathRepository: ImagePathRepository by inject()
    private val md = MessageDigest.getInstance("MD5")
    private var tableOfContents = mutableListOf<TOCReference>()
    private var totalChapters = 0

    override suspend fun doWork(): Result {
        val cacheFilePath = inputData.getString(BOOK_CACHE_PATH_KEY)
        val inputStream = context.contentResolver.openInputStream(cacheFilePath!!.toUri()) ?: return Result.failure()
        val book = EpubReader().readEpub(inputStream)
        val notificationId = 1234
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return try {
            val bookTitle = book.title
            val isAlreadyImported = bookRepository.isBookExist(bookTitle)
            if (isAlreadyImported) {
                sendCompletionNotification(context, notificationManager, isSuccess = false, specialMessage = "Book already imported")
                return Result.failure()
            }
            val bookID = BigInteger(1, md.digest(bookTitle.toByteArray())).toString(16)
                .padStart(32, '0')
            val initialNotification = createNotificationBuilder(context,bookTitle).build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ForegroundInfo(notificationId, initialNotification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                ForegroundInfo(notificationId, initialNotification)
            }
            saveBookInfo(book, context, bookID, cacheFilePath, notificationManager, notificationId, bookTitle)
            saveBookContent(bookID, book, context, notificationManager, notificationId, bookTitle)
            sendCompletionNotification(context, notificationManager)
            Result.success()
        } catch (e: Exception) {
            sendCompletionNotification(context, notificationManager, isSuccess = false)
            Result.failure()
        } finally {
            withContext(Dispatchers.IO) {
                inputStream.close()
            }
            notificationManager.cancel(notificationId)
        }
    }

    private suspend fun saveBookInfo(
        book: Book,
        context: Context,
        bookID: String,
        cacheFilePath: String,
        notificationManager: NotificationManager,
        notificationId: Int,
        fileName: String
    ): Long {
        tableOfContents = flattenTocReferences(book.tableOfContents.tocReferences)
        totalChapters = tableOfContents.size
        updateNotification(
            context = context,
            notificationManager = notificationManager,
            notificationId = notificationId,
            fileName = fileName,
            message = "Saving book info",
        )
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
    private fun updateNotification(
        context: Context,
        notificationManager: NotificationManager,
        notificationId: Int,
        fileName: String,
        message: String,
    ) {
        val updatedNotification = createNotificationBuilder(context, fileName)
            .setContentText(message)
            .setProgress(0, 0, true)
            .build()
        notificationManager.notify(notificationId, updatedNotification)
    }
    private suspend fun saveBookContent(
        bookID: String,
        book: Book,
        context: Context,
        notificationManager: NotificationManager,
        notificationId: Int,
        fileName: String
    ) {
        tableOfContents.forEachIndexed { index, tocReference ->
            updateNotification(
                context = context,
                notificationManager = notificationManager,
                notificationId = notificationId,
                fileName = fileName,
                message = "Saving chapter $index",
            )
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
            normalizedName.add((author.firstname + " " + author.lastname).trim())
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
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 80, outputStream)
                }
            } else {
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, outputStream)
                }
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
                                val parentNode = node.parentNode()
                                if (parentNode is Element && parentNode.tagName() in listOf("h1", "h2", "h3", "h4", "h5", "h6")) {
                                    if (currentParagraph.isNotEmpty()) {
                                        currentParagraph.append(" ")
                                    }
                                } else {
                                    if (currentParagraph.isNotEmpty()) {
                                        paragraphs.add(currentParagraph.toString().trim())
                                        currentParagraph = StringBuilder()
                                    }
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

                            "img","svg" -> {
                                try {
                                    if(node.tagName() == "img"){
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
                                    } else {
                                        var imageElement = node.selectFirst("image")
                                        if (imageElement != null) {
                                            val src = imageElement.attr("xlink:href")
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
                                        } else {
                                            imageElement = node.selectFirst("img")
                                            val src = imageElement?.attr("src")
                                            val actualSrc = src?.replace("../", "")
                                            val resource =
                                                actualSrc?.let { getImageResourceFromBook(it, book) }
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
        if (book.resources.resourceMap.containsKey(src)) {
            return book.resources.resourceMap[src]
        }
        val possiblePrefixes = listOf(
            "OEBPS/images/",
            "OEBPS/IMAGES/",
            "epub/images/",
            "epub/IMAGES/",
            "OEBPS/",
            "epub/",
            "images/",
        )
        possiblePrefixes.forEach{prefix->
            val prefixedHref = prefix + src
            if (book.resources.resourceMap.containsKey(prefixedHref)) {
                return book.resources.resourceMap[prefixedHref]
            }
        }
        book.resources.resourceMap.keys.forEach{key->
            if(key.endsWith(src))
                return book.resources.resourceMap[key]
        }
        return null
    }

    private fun sendCompletionNotification(
        context: Context,
        notificationManager: NotificationManager,
        isSuccess: Boolean = true,
        specialMessage: String? = ""
    ) {
        val completionNotification = createCompletionNotificationBuilder(context, isSuccess, specialMessage).build()
        val completionNotificationId = 1235
        notificationManager.notify(completionNotificationId, completionNotification)
    }
    private fun createNotificationBuilder(context : Context,fileName: String): NotificationCompat.Builder {
        val channelId = "book_import_channel"
        val channelName = "Book Import"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
        return NotificationCompat.Builder(context,channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Importing $fileName")
            .setContentText("Loading EPUB file") // Initial text
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setSilent(true)
    }
    private fun createCompletionNotificationBuilder(
        context: Context,
        isSuccess: Boolean,
        specialMessage: String? = ""
    ): NotificationCompat.Builder {
        val channelId = "book_import_completion_channel"
        val channelName = "Book Import Completion"
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Book Import")
            .setContentText(if (isSuccess) "Book import completed successfully!" else "Book import failed. $specialMessage")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    }
}