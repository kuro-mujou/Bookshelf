package com.capstone.bookshelf.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
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
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.domain.repository.ChapterRepository
import com.capstone.bookshelf.domain.repository.ImagePathRepository
import com.capstone.bookshelf.domain.repository.TableOfContentRepository
import com.capstone.bookshelf.util.saveBitmapToPrivateStorage
import com.tom_roush.pdfbox.contentstream.operator.Operator
import com.tom_roush.pdfbox.cos.COSBase
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.graphics.PDXObject
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionGoTo
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.util.UUID
import kotlin.math.ceil
import kotlin.Result as ImportResult

sealed class PageContentElement {
    data class Text(val text: String) : PageContentElement()
    data class Image(val path: String) : PageContentElement()
}

class PDFImportWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val bookRepository: BookRepository by inject()
    private val tableOfContentsRepository: TableOfContentRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val imagePathRepository: ImagePathRepository by inject()

    private val md = MessageDigest.getInstance("MD5")
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationId = System.currentTimeMillis().toInt()
    private val completionNotificationId = notificationId + 1

    private enum class ProcessingMode { TOC_BASED, IMAGE_ONLY }

    companion object {
        const val INPUT_URI_KEY = "input_uri"
        const val ORIGINAL_FILENAME_KEY = "original_filename"
        const val SPECIAL_INTENT_KEY = "special_intent"
        private const val TAG = "PDFImportWorker"

        private const val PROGRESS_CHANNEL_ID = "book_import_progress_channel"
        private const val COMPLETION_CHANNEL_ID = "book_import_completion_channel"

        private const val MAX_BITMAP_DIMENSION = 2048
    }

    init {
        createNotificationChannelIfNeeded(PROGRESS_CHANNEL_ID, "Book Import Progress")
        createNotificationChannelIfNeeded(COMPLETION_CHANNEL_ID, "Book Import Completion")
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val pdfUriString = inputData.getString(INPUT_URI_KEY)
        val originalFileName = inputData.getString(ORIGINAL_FILENAME_KEY)
            ?: getDisplayNameFromUri(appContext, pdfUriString?.toUri()) ?: "Unknown PDF"
        val specialIntent = inputData.getString(SPECIAL_INTENT_KEY) ?: "null"
        if (pdfUriString == null) {
            return@withContext Result.failure()
        }
        val pdfUri = pdfUriString.toUri()
        val initialNotification = createProgressNotificationBuilder(
            originalFileName, "Starting import..."
        ).build()
        try {
            setForeground(getForegroundInfoCompat(initialNotification))
        } catch (e: Exception) {

        }

        val processingResult = processPdfViaCache(
            context = appContext,
            pdfUri = pdfUri,
            originalFileName = originalFileName,
            specialIntent = specialIntent,
            onProgress = { progress, message ->
                updateProgressNotification(originalFileName, message, progress)
            }
        )

        val isSuccess = processingResult.isSuccess
        val failureReason = if (!isSuccess) processingResult.exceptionOrNull()?.message else null
        val displayTitle = processingResult.getOrNull()
            ?: originalFileName.substringBeforeLast('.')
        sendCompletionNotification(isSuccess, displayTitle, failureReason)
        return@withContext if (isSuccess) Result.success() else Result.failure()
    }

    /**
     * Processes PDF by first copying to cache, then analyzing and saving data.
     * Returns Result.success(bookTitle) or Result.failure(exception).
     */
    private suspend fun processPdfViaCache(
        context: Context,
        pdfUri: Uri,
        originalFileName: String,
        specialIntent: String,
        onProgress: suspend (progress: Int?, message: String) -> Unit
    ): ImportResult<String> {
        var tempPdfFile: File? = null
        val bookTitle = originalFileName.substringBeforeLast('.')
        var finalBookTitle: String = bookTitle
        var bookId: String? = null
        try {
            onProgress(null, "Copying file...")
            tempPdfFile = File.createTempFile("pdf_import_", ".pdf", context.cacheDir)
            context.contentResolver.openInputStream(pdfUri)?.use { inputStream ->
                FileOutputStream(tempPdfFile).use { outputStream ->
                    inputStream.copyTo(outputStream, 8192)
                }
            } ?: run {
                tempPdfFile?.delete()
                return ImportResult.failure(IOException("Failed to open InputStream for PDF"))
            }
            onProgress(null, "Loading PDF document...")
            PDDocument.load(tempPdfFile, "", MemoryUsageSetting.setupTempFileOnly())
                .use { document ->
                    if (document.numberOfPages == 0) {
                        throw IOException("PDF document has no pages.")
                    }
                    val info: PDDocumentInformation = document.documentInformation
                    finalBookTitle = info.title?.takeIf { it.isNotBlank() } ?: bookTitle
                    bookId = BigInteger(1, md.digest(originalFileName.toByteArray()))
                        .toString(16).padStart(32, '0')
                    if (bookRepository.isBookExist(finalBookTitle)) {
                        return ImportResult.failure(IOException("Book already imported"))
                    }
                    onProgress(null, "Generating cover image...")
                    val coverImagePath = generateAndSaveCoverImage(context, tempPdfFile, bookId)
                    val finalCoverPathForDb =
                        if (coverImagePath == null || coverImagePath.startsWith("error_")) {
                            null
                        } else {
                            coverImagePath
                        }
                    onProgress(null, "Analyzing table of contents...")
                    val tocList = extractToc(document)
                    val authors =
                        listOf(info.author?.takeIf { it.isNotBlank() } ?: "Unknown Author")
                    val processingMode =
                        if (specialIntent != "null" && specialIntent == "TEXT" && tocList.isNotEmpty()) {
                            ProcessingMode.TOC_BASED
                        } else
                            ProcessingMode.IMAGE_ONLY
                    val totalChaptersOrPages = when (processingMode) {
                        ProcessingMode.TOC_BASED -> tocList.size
                        ProcessingMode.IMAGE_ONLY -> if (document.numberOfPages > 0) {
                            ceil(document.numberOfPages / 15.0).toInt()
                        } else {
                            1
                        }
                    }
                    if (totalChaptersOrPages == 0) {
                        return ImportResult.failure(IOException("No chapters or pages found to process."))
                    }
                    onProgress(null, "Saving book information...")
                    saveBookInfo(
                        bookID = bookId,
                        title = finalBookTitle,
                        authors = authors,
                        coverImagePath = finalCoverPathForDb,
                        totalChapters = totalChaptersOrPages,
                        storagePath = tempPdfFile.absolutePath,
                        fileType = when (processingMode) {
                            ProcessingMode.TOC_BASED -> "pdf/normal"
                            ProcessingMode.IMAGE_ONLY -> "pdf/images"
                        }
                    ).run {
                        bookRepository.updateRecentRead(bookId)
                    }
                    if (finalCoverPathForDb != null) {
                        imagePathRepository.saveImagePath(bookId, listOf(finalCoverPathForDb))
                    }
                    when (processingMode) {
                        ProcessingMode.TOC_BASED -> {
                            processChaptersWithToc(
                                document = document,
                                bookId = bookId,
                                tocList = tocList,
                                originalFileName = originalFileName,
                                context = context,
                                onProgress = onProgress
                            )
                        }

                        ProcessingMode.IMAGE_ONLY -> {
                            processPagesAsImages(
                                tempPdfFile = tempPdfFile,
                                bookId = bookId,
                                originalFileName = originalFileName,
                                context = context,
                                onProgress = onProgress
                            )
                        }
                    }

                    return@use ImportResult.success(finalBookTitle)
                }
        } catch (e: Exception) {
            return ImportResult.failure(e)
        } finally {
            if (tempPdfFile != null && tempPdfFile.exists()) {
                tempPdfFile.delete()
            }
        }
        return ImportResult.success(finalBookTitle)
    }

    /** Generates cover using PdfRenderer from the cached file */
    private fun generateAndSaveCoverImage(
        context: Context,
        tempPdfFile: File,
        bookId: String
    ): String? {
        var pfd: ParcelFileDescriptor? = null
        try {
            pfd = ParcelFileDescriptor.open(tempPdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            return PdfRenderer(pfd).use { renderer ->
                if (renderer.pageCount > 0) {
                    renderer.openPage(0).use { page ->
                        val bitmap = try {
                            createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                        } catch (oom: OutOfMemoryError) {
                            return@use "error_oom_creating_bitmap"
                        }
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)
                        try {
                            page.render(
                                bitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                            )
                        } catch (oom: OutOfMemoryError) {
                            bitmap.recycle()
                            return@use "error_oom_rendering_cover"
                        }
                        val coverFilename = "cover_${bookId}"
                        saveBitmapToPrivateStorage(
                            context = context,
                            bitmap = bitmap,
                            compressType = Bitmap.CompressFormat.JPEG,
                            quality = 80,
                            filenameWithoutExtension = coverFilename
                        ).also {
                            bitmap.recycle()
                        }
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            return "error_generating_cover"
        } finally {
            try {
                pfd?.close()
            } catch (e: IOException) {
            }
        }
    }

    /** Extracts TOC using PdfBox from the already loaded document */
    private fun extractToc(document: PDDocument): List<Pair<String, Int>> {
        val tocList = mutableListOf<Pair<String, Int>>()
        try {
            val outline: PDDocumentOutline? = document.documentCatalog?.documentOutline
            var currentOutlineItem: PDOutlineItem? = outline?.firstChild
            while (currentOutlineItem != null) {
                val title = currentOutlineItem.title?.trim()?.takeIf { it.isNotEmpty() }
                    ?: "Untitled Chapter"
                val pageNumber = resolveBookmarkPageNumber(document, currentOutlineItem)
                if (pageNumber != -1) {
                    tocList.add(title to (pageNumber + 1))
                }
                currentOutlineItem = currentOutlineItem.nextSibling
            }
            tocList.sortBy { it.second }
        } catch (e: Exception) {
            return emptyList()
        }
        return tocList
    }

    /** Resolves bookmark destination to a 0-based page index */
    private fun resolveBookmarkPageNumber(doc: PDDocument, bookmark: PDOutlineItem): Int {
        try {
            val destination = bookmark.destination
            val page = when (destination) {
                is PDPageDestination -> destination.page ?: doc.getPage(destination.pageNumber)
                is PDNamedDestination -> {
                    val nameTree = doc.documentCatalog?.names?.dests
                    val pageDest = nameTree?.getValue(destination.namedDestination)
                    pageDest?.page
                        ?: if (pageDest != null) doc.getPage(pageDest.pageNumber) else null
                }

                else -> {
                    val action = bookmark.action
                    if (action is PDActionGoTo) {
                        val actionDest = action.destination
                        when (actionDest) {
                            is PDPageDestination -> actionDest.page
                                ?: doc.getPage(actionDest.pageNumber)

                            is PDNamedDestination -> {
                                val nameTree = doc.documentCatalog?.names?.dests
                                val pageDest = nameTree?.getValue(actionDest.namedDestination)
                                pageDest?.page
                                    ?: if (pageDest != null) doc.getPage(pageDest.pageNumber) else null
                            }

                            else -> null
                        }
                    } else null
                }
            }
            return if (page != null) doc.pages.indexOf(page) else -1
        } catch (e: Exception) {
            return -1
        }
    }


    /** Processes PDF content chapter by chapter based on TOC */
    private suspend fun processChaptersWithToc(
        document: PDDocument,
        bookId: String,
        tocList: List<Pair<String, Int>>,
        originalFileName: String,
        context: Context,
        onProgress: suspend (progress: Int?, message: String) -> Unit
    ) {
        val totalChapters = tocList.size
        tocList.forEachIndexed { index, tocEntry ->
            val chapterTitle = tocEntry.first
            val startPageNumber = tocEntry.second
            val endPageNumber = if (index < tocList.size - 1) {
                maxOf(startPageNumber, tocList[index + 1].second - 1)
            } else {
                document.numberOfPages
            }

            val progressPercent = ((index + 1).toFloat() / totalChapters * 100).toInt()
            onProgress(
                progressPercent,
                "Processing Chapter ${index + 1}/$totalChapters: ${chapterTitle.take(30)}..."
            )
            saveTableOfContentEntry(bookId, chapterTitle, index)
            if (startPageNumber > endPageNumber || startPageNumber < 1 || startPageNumber > document.numberOfPages) {
                saveEmptyChapterContent(bookId, chapterTitle, index)
                return@forEachIndexed
            }
            val orderedStripper = OrderedContentStripper(
                context = context,
                bookId = bookId,
                chapterIndex = index,
                saveImageFunc = { bitmap, baseFileName ->
                    saveBitmapToPrivateStorage(
                        context = context,
                        bitmap = bitmap,
                        compressType = Bitmap.CompressFormat.JPEG,
                        quality = 80,
                        filenameWithoutExtension = baseFileName
                    )
                }
            )
            orderedStripper.startPage = startPageNumber
            orderedStripper.endPage = endPageNumber

            val orderedPageElements: List<PageContentElement> = try {
                orderedStripper.getText(document)
                orderedStripper.getOrderedContent()
            } catch (e: Exception) {
                saveErrorChapterContent(
                    bookId,
                    chapterTitle,
                    index,
                    "[Error processing chapter content]"
                )
                return@forEachIndexed
            }
            val chapterContentList = orderedPageElements.mapNotNull {
                when (it) {
                    is PageContentElement.Text -> it.text.takeIf { txt -> txt.isNotBlank() }
                    is PageContentElement.Image -> it.path.takeIf { path -> !path.startsWith("error_") }
                }
            }

            if (chapterContentList.isNotEmpty()) {
                saveChapterContent(bookId, chapterTitle, index, chapterContentList)
                val imagePathsInChapter = chapterContentList.filter {
                    it.contains("${bookId}_chapter${index}") && it.endsWith(".webp")
                }
                if (imagePathsInChapter.isNotEmpty()) {
                    imagePathRepository.saveImagePath(bookId, imagePathsInChapter)
                }
            } else {
                saveEmptyChapterContent(bookId, chapterTitle, index)
            }
        }
    }

    /** Processes PDF page by page, saving each as an image and grouping images into chapters */
    private suspend fun processPagesAsImages(
        tempPdfFile: File,
        bookId: String,
        originalFileName: String,
        context: Context,
        onProgress: suspend (progress: Int?, message: String) -> Unit
    ) {
        val pagesPerChapter = 15
        var pfd: ParcelFileDescriptor? = null
        val allSavedImagePaths = mutableListOf<String>()
        val currentChapterImagePaths = mutableListOf<String>()
        var chapterIndexCounter = 0
        var currentBatchStartPageNumber = 1
        try {
            pfd = ParcelFileDescriptor.open(tempPdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            PdfRenderer(pfd).use { renderer ->
                val actualTotalPages = renderer.pageCount

                for (pageIndex in 0 until actualTotalPages) {
                    val pageNumber = pageIndex + 1
                    val progressPercent = (pageNumber.toFloat() / actualTotalPages * 100).toInt()
                    onProgress(progressPercent, "Processing Page $pageNumber/$actualTotalPages")

                    var pageImagePath: String? = null
                    try {
                        renderer.openPage(pageIndex).use { page ->
                            val bitmap = try {
                                createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                            } catch (oom: OutOfMemoryError) {
                                throw oom
                            }
                            val canvas = Canvas(bitmap)
                            canvas.drawColor(Color.WHITE)
                            try {
                                page.render(
                                    bitmap,
                                    null,
                                    null,
                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                )
                            } catch (oom: OutOfMemoryError) {
                                bitmap.recycle()
                                throw oom
                            }
                            pageImagePath = saveBitmapToPrivateStorage(
                                context = context,
                                bitmap = bitmap,
                                compressType = Bitmap.CompressFormat.PNG,
                                quality = 100,
                                filenameWithoutExtension = "${bookId}_page_${pageNumber}"
                            ).also { bitmap.recycle() }
                        }
                    } catch (e: Exception) {
                        pageImagePath = null
                    }

                    if (pageImagePath != null && !pageImagePath!!.startsWith("error_")) {
                        currentChapterImagePaths.add(pageImagePath!!)
                        allSavedImagePaths.add(pageImagePath!!)
                    }
                    val isEndOfBatch = (pageNumber % pagesPerChapter == 0)
                    val isEndOfDocument = (pageNumber == actualTotalPages)
                    if (isEndOfBatch || isEndOfDocument) {
                        if (currentChapterImagePaths.isNotEmpty()) {
                            val currentBatchEndPageNumber = pageNumber
                            val chapterTitle =
                                if (currentBatchStartPageNumber == currentBatchEndPageNumber) {
                                    "Page $currentBatchStartPageNumber"
                                } else {
                                    "Pages $currentBatchStartPageNumber - $currentBatchEndPageNumber"
                                }
                            saveTableOfContentEntry(bookId, chapterTitle, chapterIndexCounter)
                            saveChapterContent(
                                bookId = bookId,
                                title = chapterTitle,
                                index = chapterIndexCounter,
                                content = currentChapterImagePaths.toList()
                            )
                            chapterIndexCounter++
                            currentChapterImagePaths.clear()
                            currentBatchStartPageNumber = pageNumber + 1

                        } else {
                            currentBatchStartPageNumber = pageNumber + 1
                        }
                    }
                }
                if (allSavedImagePaths.isNotEmpty()) {
                    imagePathRepository.saveImagePath(bookId, allSavedImagePaths)
                } else {
                    if (chapterIndexCounter == 0) {
                        saveErrorChapterContent(
                            bookId = bookId,
                            title = originalFileName.takeUnless { it.isBlank() }
                                ?: "Document Content",
                            index = 0,
                            errorMessage = "No pages could be processed into images for the entire document."
                        )
                    }
                }
            }
        } catch (e: Exception) {
            if (chapterIndexCounter == 0) {
                saveErrorChapterContent(
                    bookId = bookId,
                    title = originalFileName.takeUnless { it.isBlank() } ?: "Document Content",
                    index = 0,
                    errorMessage = "Error processing document: ${e.message ?: "Unknown error"}"
                )
            }
            throw e
        } finally {
            try {
                pfd?.close()
            } catch (e: IOException) {
            }
        }
    }

    private suspend fun saveBookInfo(
        bookID: String,
        title: String,
        authors: List<String>,
        coverImagePath: String?,
        totalChapters: Int,
        storagePath: String,
        fileType: String
    ): Long {
        val bookEntity = BookEntity(
            bookId = bookID,
            title = title,
            coverImagePath = coverImagePath!!,
            authors = authors,
            description = null,
            totalChapter = totalChapters,
            currentChapter = 0,
            currentParagraph = 0,
            storagePath = storagePath,
            isEditable = false,
            fileType = fileType
        )
        return bookRepository.insertBook(bookEntity)
    }

    private suspend fun saveTableOfContentEntry(
        bookId: String,
        title: String,
        index: Int
    ): Long {
        val tocEntity = TableOfContentEntity(
            bookId = bookId,
            title = title,
            index = index
        )
        return tableOfContentsRepository.saveTableOfContent(tocEntity)
    }

    private suspend fun saveChapterContent(
        bookId: String,
        title: String,
        index: Int,
        content: List<String>
    ) {
        val chapterEntity = ChapterContentEntity(
            tocId = index,
            bookId = bookId,
            chapterTitle = title,
            content = content,
        )
        chapterRepository.saveChapterContent(chapterEntity)
    }

    private suspend fun saveEmptyChapterContent(bookId: String, title: String, index: Int) {
        saveChapterContent(bookId, title, index, emptyList())
    }

    private suspend fun saveErrorChapterContent(
        bookId: String,
        title: String,
        index: Int,
        errorMessage: String
    ) {
        saveChapterContent(bookId, title, index, listOf(errorMessage))
    }

    private fun createNotificationChannelIfNeeded(channelId: String, channelName: String) {
        if (notificationManager.getNotificationChannel(channelId) == null) {
            val importance =
                if (channelId == PROGRESS_CHANNEL_ID) NotificationManager.IMPORTANCE_LOW else NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Notifications for book import process"
                if (channelId == PROGRESS_CHANNEL_ID) setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createProgressNotificationBuilder(
        fileName: String,
        message: String
    ): NotificationCompat.Builder {
        val displayFileName = fileName.substringBeforeLast(".")
        return NotificationCompat.Builder(appContext, PROGRESS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Importing PDF: ${displayFileName.take(35)}${if (displayFileName.length > 35) "..." else ""}")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
    }

    private suspend fun updateProgressNotification(
        fileName: String,
        message: String,
        progress: Int?
    ) {
        val builder = createProgressNotificationBuilder(fileName, message)
        if (progress != null) {
            builder.setProgress(100, progress.coerceIn(0, 100), false)
        } else {
            builder.setProgress(0, 0, true)
        }
        try {
            setForeground(getForegroundInfoCompat(builder.build()))
        } catch (e: Exception) {
            notificationManager.notify(notificationId, builder.build())
        }
    }

    private fun sendCompletionNotification(
        isSuccess: Boolean, bookTitle: String?, failureReason: String? = null
    ) {
        val title = if (isSuccess) "Import Successful" else "Import Failed"
        val defaultTitle = bookTitle ?: "PDF File"
        val userFriendlyReason = when {
            failureReason == null -> null
            failureReason.contains("Book already imported") -> "This book is already in your library."
            failureReason.contains("No valid image entries found") -> "No content could be extracted."
            failureReason.contains("Failed to open InputStream") -> "Could not read the selected file."
            failureReason.contains("OutOfMemoryError") -> "Ran out of memory processing the PDF. It might be too large or complex."
            failureReason.contains("PDF document has no pages") -> "The selected PDF file is empty."
            else -> "An unexpected error occurred."
        }

        val text = when {
            isSuccess -> "'$defaultTitle' added to your library."
            userFriendlyReason != null -> "Failed to import '$defaultTitle': $userFriendlyReason"
            else -> "Import failed for '$defaultTitle'."
        }
        val builder = NotificationCompat.Builder(appContext, COMPLETION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        notificationManager.notify(completionNotificationId, builder.build())
        notificationManager.cancel(notificationId)
    }

    /** Gets display name from content URI. */
    private fun getDisplayNameFromUri(context: Context, uri: Uri?): String? {
        if (uri == null) return null
        var displayName: String? = null
        try {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        displayName = cursor.getString(nameIndex)
                    }
                }
            }
        } catch (e: Exception) {
        }
        return displayName
    }

    /** Provides ForegroundInfo, handling platform differences. */
    private fun getForegroundInfoCompat(notification: Notification): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    private inner class OrderedContentStripper(
        private val context: Context,
        private val bookId: String,
        private val chapterIndex: Int,
        private val saveImageFunc: (bitmap: Bitmap, baseFileName: String) -> String
    ) : PDFTextStripper() {

        private val pageContentList = mutableListOf<PageContentElement>()
        private var currentPageNumberForImages: Int = 0
        private var imageCounterOnPage: Int = 0
        private val currentTextParagraph = StringBuilder()

        init {
            lineSeparator = " "
            sortByPosition = true
        }

        /** Returns the collected content, ensuring final text is flushed. */
        fun getOrderedContent(): List<PageContentElement> {
            flushTextBuffer()
            return pageContentList.toList()
        }

        override fun startPage(page: PDPage?) {
            super.startPage(page)
            flushTextBuffer()
            currentPageNumberForImages = currentPageNo
            imageCounterOnPage = 0
        }

        override fun writeString(text: String?, textPositions: MutableList<TextPosition>?) {
            currentTextParagraph.append(text ?: "")
        }

        @Throws(IOException::class)
        override fun processOperator(operator: Operator, operands: List<COSBase>) {
            val operation: String = operator.name
            if ("Do" == operation && operands.isNotEmpty()) {
                val objectName = operands.firstOrNull() as? COSName
                if (objectName != null) {
                    val xObject: PDXObject? = try {
                        resources?.getXObject(objectName)
                    } catch (e: Exception) {
                        null
                    }
                    if (xObject is PDImageXObject) {
                        flushTextBuffer()
                        try {
                            val bitmap = try {
                                xObject.image
                            } catch (oom: OutOfMemoryError) {
                                null
                            }

                            if (bitmap != null) {
                                val imageFileNameBase = "${bookId}_chapter${chapterIndex}"
                                val imagePath = saveImageInternal(bitmap, imageFileNameBase)
                                pageContentList.add(PageContentElement.Image(imagePath))
                                imageCounterOnPage++
                            } else {
                                pageContentList.add(PageContentElement.Text("[Image not loaded: ${objectName.name}]"))
                            }
                        } catch (e: Exception) {
                            pageContentList.add(PageContentElement.Text("[Error processing image: ${objectName.name}]"))
                        }
                        return
                    }
                }
            }
            super.processOperator(operator, operands)
        }

        override fun writeLineSeparator() {
            currentTextParagraph.append(" ")
        }

        override fun writeParagraphSeparator() {
            flushTextBuffer()
        }

        override fun endPage(page: PDPage?) {
            flushTextBuffer()
            super.endPage(page)
        }

        /** Saves the image using the provided function and handles potential errors. */
        private fun saveImageInternal(bitmap: Bitmap, baseFileName: String): String {
            val uniqueFileName =
                "${baseFileName}_p${currentPageNumberForImages}_img${imageCounterOnPage}"
            return try {
                saveImageFunc(bitmap, uniqueFileName)
            } catch (e: Exception) {
                "error_saving_image_${UUID.randomUUID()}"
            }
        }

        /** Adds the buffered text (if any) to the content list as a Text element. */
        private fun flushTextBuffer() {
            if (currentTextParagraph.isNotEmpty()) {
                val cleanedText = currentTextParagraph.toString()
                    .replace("\t", " ")
                    .replace(Regex("\\s{2,}"), " ")
                    .trim()
                if (cleanedText.isNotBlank()) {
                    pageContentList.add(PageContentElement.Text(cleanedText))
                }
                currentTextParagraph.setLength(0)
            }
        }
    }
}