package com.capstone.bookshelf.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
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
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.io.RandomAccessBufferedFileInputStream
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionGoTo
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest

class PDFImportWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) , KoinComponent {

    companion object {
        const val BOOK_CACHE_PATH_KEY = "book_cache_path"
        const val NOTIFICATION_CHANNEL_ID = "book_import_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Book Import"
        const val NOTIFICATION_ID = 1234
    }

    private val bookRepository: BookRepository by inject()
    private val tableOfContentsRepository: TableOfContentRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val imagePathRepository: ImagePathRepository by inject()
    private val md = MessageDigest.getInstance("MD5")

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                createForegroundInfo()
                val pdfPath = inputData.getString(BOOK_CACHE_PATH_KEY) ?: return@withContext Result.failure()
                processPDFtoBook(context, pdfPath)
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            } finally {
                context.cacheDir.deleteRecursively()
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(BookImportWorker.NOTIFICATION_ID)
            }
        }
    }
    private suspend fun processPDFtoBook(context: Context, pdfUriString: String) {
        return withContext(Dispatchers.IO) {
            var bookTitle: String?
            val authors = mutableListOf<String>()
            var coverImage: Bitmap
            var bookID: String
            val tocList = mutableListOf<Pair<String, Int>>()
            var pfd: ParcelFileDescriptor? = null
            try {
                val uri = pdfUriString.toUri()
                pfd = context.contentResolver.openFileDescriptor(uri, "r")
                pfd?.let { fd ->
                    val inputStream = FileInputStream(fd.fileDescriptor)
                    inputStream.use {stream->
                        PDDocument.load(
                            RandomAccessBufferedFileInputStream(stream),
                            "",
                            MemoryUsageSetting.setupTempFileOnly()
                        ).use { document ->
                            val info: PDDocumentInformation = document.documentInformation
                            bookTitle = info.title
                            bookID =
                                BigInteger(1, md.digest(bookTitle!!.toByteArray())).toString(16)
                                    .padStart(32, '0')
                            PdfRenderer(fd).use { renderer ->
                                val page = renderer.openPage(0)
                                coverImage = createBitmap(page.width, page.height)
                                page.render(
                                    coverImage,
                                    null,
                                    null,
                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                )
                                page.close()
                            }
                            val coverImagePath =
                                saveImageToPrivateStorage(context, coverImage, "cover_${bookTitle}")
                            authors.add(info.author)
                            val outline: PDDocumentOutline =
                                document.documentCatalog?.documentOutline ?: return@withContext
                            var bookmark: PDOutlineItem? = outline.firstChild ?: return@withContext
                            while (bookmark != null) {
                                val title = bookmark.title
                                val pageNumber = when (val destination = bookmark.destination) {
                                    is PDPageDestination -> destination.retrievePageNumber()
                                    is PDNamedDestination -> {
                                        val nameTree = document.documentCatalog?.names?.dests
                                        nameTree?.getValue(destination.namedDestination)
                                            ?.retrievePageNumber() ?: -1
                                    }

                                    else -> {
                                        val action = bookmark.action
                                        if (action is PDActionGoTo) {
                                            (action.destination as? PDPageDestination)?.retrievePageNumber()
                                                ?: -1
                                        } else {
                                            -1
                                        }
                                    }
                                }
                                if (pageNumber != -1) {
                                    tocList.add(title to pageNumber + 1)
                                }
                                bookmark = bookmark.nextSibling
                            }
                            val bookEntity = BookEntity(
                                bookId = bookID,
                                title = bookTitle!!,
                                coverImagePath = coverImagePath,
                                authors = authors,
                                categories = emptyList(),
                                description = null,
                                totalChapter = tocList.size,
                                storagePath = pdfUriString,
                                ratingsAverage = 0.0,
                                ratingsCount = 0
                            )
                            bookRepository.insertBook(bookEntity)
                            imagePathRepository.saveImagePath(bookID, listOf(coverImagePath))
                            val stripper = PDFTextStripper()
                            stripper.paragraphStart = "\n\n"
                            stripper.paragraphEnd = ""
                            stripper.lineSeparator = "\n"
                            tocList.forEachIndexed { index, tocReference ->
                                val tocEntity = TableOfContentEntity(
                                    bookId = bookID,
                                    title = tocReference.first,
                                    index = index
                                )
                                tableOfContentsRepository.saveTableOfContent(tocEntity)
                                val startChapterPage = tocList[index].second
                                val endChapterPage = if (index < tocList.size - 1) {
                                    tocList[index + 1].second - 1
                                } else {
                                    document.numberOfPages
                                }
                                stripper.startPage = startChapterPage
                                stripper.endPage = endChapterPage
                                val chapterContent = stripper.getText(document).trim()
                                val paragraphs = parseChapterContentToList(chapterContent)
                                val chapterEntity = ChapterContentEntity(
                                    tocId = index,
                                    bookId = bookID,
                                    chapterTitle = tocList[index].first,
                                    content = paragraphs,
                                )
                                chapterRepository.saveChapterContent(chapterEntity)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pfd?.close()
            }
        }
    }
    @Suppress("DEPRECATION")
    private fun saveImageToPrivateStorage(
        context: Context,
        bitmap: Bitmap?,
        filename: String
    ): String {
        return try {
            val file = File(context.filesDir, "$filename.webp")
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
                FileOutputStream(file).use { outputStream ->
                    bitmap?.compress(Bitmap.CompressFormat.WEBP, 100, outputStream)
                }
            } else {
                FileOutputStream(file).use { outputStream ->
                    bitmap?.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, outputStream)
                }
            }
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            "error when loading image"
        }
    }
    private fun parseChapterContentToList(text: String): List<String> {
        val paragraphs = text.split("\n\n")
            .map { it.replace("\t", " ").replace("\n", " ").trim() }
            .filter { it.isNotEmpty() }
        return paragraphs
    }
    private fun createForegroundInfo(): ForegroundInfo {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            BookImportWorker.NOTIFICATION_CHANNEL_ID,
            BookImportWorker.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(context,
            BookImportWorker.NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Importing Book")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(BookImportWorker.NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(BookImportWorker.NOTIFICATION_ID, notification)
        }
    }
}

//package com.capstone.bookshelf.util
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
//import android.graphics.Bitmap
//import android.graphics.pdf.PdfRenderer
//import android.os.Build
//import android.os.ParcelFileDescriptor
//import androidx.core.app.NotificationCompat
//import androidx.core.graphics.createBitmap
//import androidx.core.net.toUri
//import androidx.work.CoroutineWorker
//import androidx.work.ForegroundInfo
//import androidx.work.WorkerParameters
//import com.capstone.bookshelf.R
//import com.capstone.bookshelf.data.database.entity.BookEntity
//import com.capstone.bookshelf.data.database.entity.ChapterContentEntity
//import com.capstone.bookshelf.data.database.entity.TableOfContentEntity
//import com.capstone.bookshelf.domain.book.BookRepository
//import com.capstone.bookshelf.domain.book.ChapterRepository
//import com.capstone.bookshelf.domain.book.ImagePathRepository
//import com.capstone.bookshelf.domain.book.TableOfContentRepository
//import com.tom_roush.pdfbox.io.RandomAccessBuffer
//import com.tom_roush.pdfbox.pdfparser.PDFParser
//import com.tom_roush.pdfbox.pdmodel.PDDocument
//import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
//import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionGoTo
//import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination
//import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination
//import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
//import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
//import com.tom_roush.pdfbox.text.PDFTextStripper
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import org.koin.core.component.KoinComponent
//import org.koin.core.component.inject
//import java.io.File
//import java.io.FileOutputStream
//import java.io.IOException
//import java.io.InputStream
//import java.math.BigInteger
//import java.security.MessageDigest
//
//class PDFImportWorker(
//    private val context: Context,
//    workerParams: WorkerParameters
//) : CoroutineWorker(context, workerParams), KoinComponent {
//
//    companion object {
//        const val BOOK_CACHE_PATH_KEY = "book_cache_path"
//        const val NOTIFICATION_CHANNEL_ID = "book_import_channel"
//        const val NOTIFICATION_CHANNEL_NAME = "Book Import"
//        const val NOTIFICATION_ID = 1234
//    }
//
//    private val bookRepository: BookRepository by inject()
//    private val tableOfContentsRepository: TableOfContentRepository by inject()
//    private val chapterRepository: ChapterRepository by inject()
//    private val imagePathRepository: ImagePathRepository by inject()
//    private val md = MessageDigest.getInstance("MD5")
//
//    override suspend fun doWork(): Result {
//        return withContext(Dispatchers.IO) {
//            try {
//                createForegroundInfo()
//                val pdfUriString = inputData.getString(BOOK_CACHE_PATH_KEY) ?: return@withContext Result.failure()
//                processPDFtoBook(context, pdfUriString)
//                Result.success()
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Result.failure()
//            } finally {
//                context.cacheDir.deleteRecursively()
//                val notificationManager =
//                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                notificationManager.cancel(NOTIFICATION_ID)
//            }
//        }
//    }
//
//
//    private suspend fun processPDFtoBook(context: Context, pdfUriString: String) {
//        var inputStream: InputStream? = null
//        var pfd: ParcelFileDescriptor? = null
//
//        try {
//            val uri = pdfUriString.toUri()
//            inputStream = context.contentResolver.openInputStream(uri) ?: return // Early return if null
//
//            // --- Extract Cover Image (Efficiently, outside the main PDF parsing) ---
//            pfd = context.contentResolver.openFileDescriptor(uri, "r")
//            val coverImage = pfd?.let { extractCoverImage(it) } // Extract and close pfd promptly
//            val coverImagePath = coverImage?.let {
//                saveImageToPrivateStorage(context, it, "cover_${uri.lastPathSegment}")
//            } ?: ""
//
//            inputStream.let { stream ->
//                val parser = PDFParser( RandomAccessBuffer(stream)) // Efficient, incremental parsing
//                parser.parse()
//
//                PDDocument(parser.document).use { document ->
//                    // --- Extract Metadata (Title, Author) ---
//                    val info: PDDocumentInformation = document.documentInformation
//                    val bookTitle = info.title ?: uri.lastPathSegment ?: "Untitled"
//                    val bookID = BigInteger(1, md.digest(bookTitle.toByteArray())).toString(16).padStart(32, '0')
//                    val authors = listOf(info.author ?: "Unknown Author")
//
//
//                    // --- Extract Bookmarks (Table of Contents) ---
//                    val tocList = extractBookmarks(document)
//
//
//                    // --- Save Book Entity ---
//                    val bookEntity = BookEntity(
//                        bookId = bookID,
//                        title = bookTitle,
//                        coverImagePath = coverImagePath,
//                        authors = authors,
//                        categories = emptyList(),
//                        description = null,
//                        totalChapter = tocList.size,
//                        storagePath = pdfUriString,
//                        ratingsAverage = 0.0,
//                        ratingsCount = 0
//                    )
//                    bookRepository.insertBook(bookEntity)
//                    imagePathRepository.saveImagePath(bookID, listOf(coverImagePath))
//
//                    // --- Extract and Save Chapter Content ---
//                    tocList.forEachIndexed { index, (title, pageNumber) ->
//                        //Save table of content
//                        val tocEntity = TableOfContentEntity(bookId = bookID, title = title, index = index)
//                        tableOfContentsRepository.saveTableOfContent(tocEntity)
//
//                        val endPage = if (index < tocList.size - 1) tocList[index + 1].second - 1 else document.numberOfPages
//
//                        // Efficient, streaming text extraction:
//                        val paragraphs = extractChapterContent(document, pageNumber, endPage)
//
//                        val chapterEntity = ChapterContentEntity(
//                            tocId = index,
//                            bookId = bookID,
//                            chapterTitle = title,
//                            content = paragraphs
//                        )
//                        chapterRepository.saveChapterContent(chapterEntity)
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace() // Log the exception
//            // Consider re-throwing, or returning a specific error result
//        } finally {
//            inputStream?.close()
//            pfd?.close()
//
//        }
//    }
//    private suspend fun extractChapterContent(document: PDDocument, startPage: Int, endPage: Int): List<String> =
//        withContext(Dispatchers.IO) {
//            val paragraphs = mutableListOf<String>()
//            val stripper = object : PDFTextStripper() {
//                private var currentParagraph = StringBuilder()
//
//                // Override writeString to process text *incrementally*
//                override fun writeString(text: String?, textPositions: MutableList<com.tom_roush.pdfbox.text.TextPosition>?) {
//                    text?.let {
//                        currentParagraph.append(it)
//                        // Basic paragraph detection (double newline) you can improve if needed
//                        if (it.contains("\n\n")) {
//                            val parts = currentParagraph.toString().split("\n\n")
//                            for (i in 0 until parts.size - 1) {
//                                val trimmed = parts[i].trim()
//                                if (trimmed.isNotEmpty()) {
//                                    paragraphs.add(trimmed)
//                                }
//                            }
//                            // Keep the last part for the next chunk
//                            currentParagraph = StringBuilder(parts.last().trim())
//                        }
//                    }
//                }
//
//                // Make sure to add any remaining text after processing all pages
//                override fun endDocument(doc: PDDocument?) {
//                    super.endDocument(doc)
//                    if (currentParagraph.isNotEmpty()) {
//                        val trimmed = currentParagraph.toString().trim()
//                        if(trimmed.isNotEmpty()){
//                            paragraphs.add(trimmed)
//                        }
//                    }
//                }
//            }
//
//            stripper.startPage = startPage
//            stripper.endPage = endPage
//            stripper.paragraphStart = "\n\n" // Consistent with splitting logic
//            stripper.paragraphEnd = ""      // Don't add extra at the end
//            stripper.lineSeparator = "\n"
//            stripper.getText(document)  // This triggers the overridden writeString
//            paragraphs
//        }
//
//    // Extracts bookmarks and returns a List of (title, pageNumber) pairs
//    private fun extractBookmarks(document: PDDocument): List<Pair<String, Int>> {
//        val tocList = mutableListOf<Pair<String, Int>>()
//        val outline: PDDocumentOutline = document.documentCatalog?.documentOutline ?: return emptyList()
//        var bookmark: PDOutlineItem? = outline.firstChild ?: return emptyList()
//
//        while (bookmark != null) {
//            val title = bookmark.title
//            var pageNumber: Int
//            when (val destination = bookmark.destination) {
//                is PDPageDestination -> {
//                    pageNumber = destination.retrievePageNumber()
//                    if(pageNumber != -1){
//                        tocList.add(title to pageNumber + 1) //page number is 0 based.
//                    }
//                }
//                is PDNamedDestination -> {
//                    val nameTree = document.documentCatalog?.names?.dests
//                    val resolvedDest = nameTree?.getValue(destination.namedDestination)
//                    if (resolvedDest != null) {
//                        pageNumber = resolvedDest.retrievePageNumber()
//                        if(pageNumber != -1){
//                            tocList.add(title to pageNumber + 1)
//                        }
//                    }
//                }
//                else -> {
//                    val action = bookmark.action
//                    if (action is PDActionGoTo) {
//                        val goToDest = action.destination
//                        if (goToDest is PDPageDestination) {
//                            pageNumber = goToDest.retrievePageNumber()
//                            if(pageNumber != -1){
//                                tocList.add(title to pageNumber + 1)
//                            }
//                        }
//                    }
//                }
//            }
//            bookmark = bookmark.nextSibling
//        }
//        return tocList
//    }
//
//
//    private fun extractCoverImage(pfd: ParcelFileDescriptor): Bitmap? {
//        return try {
//            PdfRenderer(pfd).use { renderer ->
//                val page = renderer.openPage(0)
//                val bitmap = createBitmap(page.width, page.height)
//                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//                page.close()
//                bitmap
//            }
//        } catch (e: Exception) {
//            e.printStackTrace() // Log error
//            null
//        }
//    }
//
//
//    @Suppress("DEPRECATION")
//    private fun saveImageToPrivateStorage(context: Context, bitmap: Bitmap, filename: String): String {
//        return try {
//            val file = File(context.filesDir, "$filename.webp")
//            FileOutputStream(file).use { outputStream ->
//                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
//                    bitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream)
//                } else {
//                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, outputStream)
//                }
//            }
//            file.absolutePath
//        } catch (e: IOException) {
//            e.printStackTrace()
//            "error when loading image" // Return a default/error value
//        }
//    }
//    private fun createForegroundInfo(): ForegroundInfo {
//        val notificationManager =
//            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val channel = NotificationChannel(
//            NOTIFICATION_CHANNEL_ID,
//            NOTIFICATION_CHANNEL_NAME,
//            NotificationManager.IMPORTANCE_DEFAULT
//        )
//        notificationManager.createNotificationChannel(channel)
//        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .setContentTitle("Importing Book")
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .build()
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            ForegroundInfo(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
//        } else {
//            ForegroundInfo(NOTIFICATION_ID, notification)
//        }
//    }
//}