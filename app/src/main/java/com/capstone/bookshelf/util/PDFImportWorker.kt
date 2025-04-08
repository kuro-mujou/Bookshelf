//package com.capstone.bookshelf.util
//
//import android.annotation.SuppressLint
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.pdf.PdfRenderer
//import android.os.Build
//import android.os.ParcelFileDescriptor
//import androidx.annotation.WorkerThread
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
//import com.capstone.bookshelf.domain.repository.BookRepository
//import com.capstone.bookshelf.domain.repository.ChapterRepository
//import com.capstone.bookshelf.domain.repository.ImagePathRepository
//import com.capstone.bookshelf.domain.repository.TableOfContentRepository
//import com.tom_roush.pdfbox.contentstream.operator.Operator
//import com.tom_roush.pdfbox.cos.COSBase
//import com.tom_roush.pdfbox.cos.COSName
//import com.tom_roush.pdfbox.io.MemoryUsageSetting
//import com.tom_roush.pdfbox.io.RandomAccessBufferedFileInputStream
//import com.tom_roush.pdfbox.pdmodel.PDDocument
//import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
//import com.tom_roush.pdfbox.pdmodel.PDPage
//import com.tom_roush.pdfbox.pdmodel.graphics.PDXObject
//import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
//import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionGoTo
//import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination
//import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination
//import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
//import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
//import com.tom_roush.pdfbox.text.PDFTextStripper
//import com.tom_roush.pdfbox.text.TextPosition
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import org.koin.core.component.KoinComponent
//import org.koin.core.component.inject
//import java.io.FileInputStream
//import java.io.FileOutputStream
//import java.io.IOException
//import java.math.BigInteger
//import java.security.MessageDigest
//import java.util.UUID
//
//sealed class PageContentElement {
//    data class Text(val text: String) : PageContentElement()
//    data class Image(val path: String) : PageContentElement()
//}
//class PDFImportWorker(
//    private val context: Context,
//    workerParams: WorkerParameters
//) : CoroutineWorker(context, workerParams), KoinComponent {
//    private enum class ProcessingMode {
//        TOC_BASED, IMAGE_ONLY
//    }
//    companion object {
//        const val BOOK_PATH_KEY = "book_cache_path"
//        const val FILE_NAME_KEY = "file_name"
//        private const val TAG = "PDFImportWorker"
//    }
//    private val bookRepository: BookRepository by inject()
//    private val tableOfContentsRepository: TableOfContentRepository by inject()
//    private val chapterRepository: ChapterRepository by inject()
//    private val imagePathRepository: ImagePathRepository by inject()
//    private val md = MessageDigest.getInstance("MD5")
//
//    private val progressNotificationId = System.currentTimeMillis().toInt()
//    private val completionNotificationId = progressNotificationId + 1
//
//    @SuppressLint("RestrictedApi")
//    override suspend fun doWork(): Result {
//        val pdfPath = inputData.getString(BOOK_PATH_KEY)
//        val fileName = inputData.getString(FILE_NAME_KEY) ?: "Unknown File"
//        if (pdfPath == null) {
//            return Result.failure()
//        }
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        createNotificationChannel(context, notificationManager, "book_import_channel", "Book Import Progress") // Ensure channel exists
//        val initialNotification = createNotificationBuilder(context, fileName, "Starting import...", progressNotificationId).build()
//        try {
//            val foregroundInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                ForegroundInfo(progressNotificationId, initialNotification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
//            } else {
//                ForegroundInfo(progressNotificationId, initialNotification)
//            }
//            setForeground(foregroundInfo)
//        } catch (e: Exception) {
//             return Result.failure()
//        }
//        var processingResult: kotlin.Result<Unit> = kotlin.Result.failure(IllegalStateException("Processing did not run"))
//        var bookTitleForNotification: String? = null
//        try {
//            processingResult = processPDFtoBook(context, pdfPath, fileName, notificationManager, progressNotificationId)
//            bookTitleForNotification = fileName.substringBeforeLast(".")
//        } catch (e: Exception) {
//            processingResult = kotlin.Result.failure(e)
//        } finally {
//            val isSuccess = processingResult.isSuccess
//            var failureMessage: String? = null
//            if (!isSuccess) {
//                failureMessage = processingResult.exceptionOrNull()?.message ?: "Unknown error during import"
//                if (failureMessage.contains("Book already imported")) {
//                    failureMessage = "Book already imported"
//                }
//            }
//            sendCompletionNotification(context, notificationManager, completionNotificationId, isSuccess, bookTitleForNotification, failureMessage)
//            notificationManager.cancel(progressNotificationId)
//        }
//        return if (processingResult.isSuccess) Result.success() else Result.failure()
//    }
//    private fun updateNotification(
//        context: Context,
//        notificationManager: NotificationManager,
//        notificationId: Int,
//        fileName: String,
//        message: String,
//        progress: Int? = null
//    ) {
//        val builder = createNotificationBuilder(context, fileName, message, notificationId)
//        if (progress != null) {
//            builder.setProgress(100, progress, false)
//        } else {
//            builder.setProgress(0, 0, true)
//        }
//        notificationManager.notify(notificationId, builder.build())
//    }
//    private suspend fun processPDFtoBook(
//        context: Context,
//        pdfUriString: String,
//        fileName: String,
//        notificationManager: NotificationManager,
//        notificationId: Int,
//    ): kotlin.Result<Unit> {
//        var bookTitle: String? = null
//        var bookID: String? = null
//        var pfd: ParcelFileDescriptor? = null
//        val uri = pdfUriString.toUri()
//        try {
//            return withContext(Dispatchers.IO) {
//                updateNotification(context, notificationManager, notificationId, fileName, "Loading $fileName...")
//                pfd = context.contentResolver.openFileDescriptor(uri, "r")
//                if (pfd == null) throw IOException("Could not open PDF file.")
//                pfd.use { ownedPfd ->
//                    val inputStream = FileInputStream(ownedPfd.fileDescriptor)
//                    inputStream.use { stream ->
//                        PDDocument.load(
//                            RandomAccessBufferedFileInputStream(stream),
//                            "", MemoryUsageSetting.setupTempFileOnly()
//                        ).use { document ->
//                            if (document.numberOfPages == 0) throw IOException("PDF document has no pages.")
//                            val info: PDDocumentInformation = document.documentInformation
//                            bookTitle = info.title?.takeIf { it.isNotBlank() } ?: fileName.substringBeforeLast(".")
//                            if (bookRepository.isBookExist(bookTitle)) {
//                                return@withContext kotlin.Result.failure(IOException("Book already imported"))
//                            }
//                            bookID = BigInteger(1, md.digest(bookTitle.toByteArray())).toString(16).padStart(32, '0')
//                            val coverImagePath = generateAndSaveCoverImage(context, uri, bookID)
//                            updateNotification(context, notificationManager, notificationId, fileName, "Analyzing Table of Contents...")
//                            val tocList = extractToc(document)
//                            val authors = listOf(info.author?.takeIf { it.isNotBlank() } ?: "Unknown Author")
//                            val processingMode = if (tocList.isNotEmpty()) ProcessingMode.TOC_BASED else ProcessingMode.IMAGE_ONLY
//                            val totalChapters = when (processingMode) {
//                                ProcessingMode.TOC_BASED -> tocList.size
//                                ProcessingMode.IMAGE_ONLY -> document.numberOfPages
//                            }
//                            updateNotification(context, notificationManager, notificationId, fileName, "Saving book information...")
//                            val bookEntity = BookEntity(
//                                bookId = bookID, title = bookTitle, coverImagePath = coverImagePath!!,
//                                authors = authors, categories = emptyList(), description = info.subject ?: info.keywords,
//                                totalChapter = totalChapters, currentChapter = 0, currentParagraph = 0,
//                                storagePath = pdfUriString, isEditable = false, fileType = "pdf"
//                            )
//                            bookRepository.insertBook(bookEntity)
//                            imagePathRepository.saveImagePath(bookID, listOf(coverImagePath))
//                            when (processingMode) {
//                                ProcessingMode.TOC_BASED -> {
//                                    processChaptersWithToc(
//                                        document = document, bookId = bookID, tocList = tocList,
//                                        fileName = fileName, notificationManager = notificationManager,
//                                        notificationId = notificationId, context = context
//                                    )
//                                }
//                                ProcessingMode.IMAGE_ONLY -> {
//                                    processPagesAsImages(
//                                        pdfUri = uri,
//                                        document = document,
//                                        bookId = bookID,
//                                        fileName = fileName, notificationManager = notificationManager,
//                                        notificationId = notificationId, context = context
//                                    )
//                                }
//                            }
//                            return@withContext kotlin.Result.success(Unit)
//                        }
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            return kotlin.Result.failure(e)
//        }
//    }
//    @WorkerThread
//    private fun generateAndSaveCoverImage(context: Context, pdfUri: android.net.Uri, bookId: String): String? {
//        var coverFd: ParcelFileDescriptor? = null
//        try {
//            coverFd = context.contentResolver.openFileDescriptor(pdfUri, "r")
//            if (coverFd == null) {
//                return null
//            }
//            return coverFd.use { ownedFd ->
//                PdfRenderer(ownedFd).use { renderer ->
//                    if (renderer.pageCount > 0) {
//                        val page = renderer.openPage(0)
//                        val bitmap = createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
//                        val canvas = Canvas(bitmap)
//                        canvas.drawColor(Color.WHITE)
//                        canvas.drawBitmap(bitmap, 0f, 0f, null)
//                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//                        page.close()
//                        saveImageToPrivateStorage(context, bitmap, "cover_${bookId}")
//                    } else {
//                        null
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            return null
//        }
//    }
//    @WorkerThread
//    private fun extractToc(document: PDDocument): List<Pair<String, Int>> {
//        val tocList = mutableListOf<Pair<String, Int>>()
//        try {
//            val outline: PDDocumentOutline? = document.documentCatalog?.documentOutline
//            var bookmark: PDOutlineItem? = outline?.firstChild
//            while (bookmark != null) {
//                val title = bookmark.title?.trim()?.takeIf { it.isNotEmpty() } ?: "Untitled Chapter"
//                val pageNumber = resolveBookmarkPageNumber(document, bookmark)
//                if (pageNumber != -1) {
//                    tocList.add(title to (pageNumber + 1))
//                }
//                bookmark = bookmark.nextSibling
//            }
//            tocList.sortBy { it.second }
//        } catch (e: Exception) {
//            return emptyList()
//        }
//        return tocList
//    }
//    @WorkerThread
//    private suspend fun processChaptersWithToc(
//        document: PDDocument,
//        bookId: String,
//        tocList: List<Pair<String, Int>>,
//        fileName: String,
//        notificationManager: NotificationManager,
//        notificationId: Int,
//        context: Context
//    ) {
//        val totalChapters = tocList.size
//        tocList.forEachIndexed { index, tocReference ->
//            val progress = ((index + 1).toFloat() / totalChapters * 100).toInt()
//            updateNotification(
//                context, notificationManager, notificationId, fileName,
//                "Processing Chapter ${index + 1}/$totalChapters: ${tocReference.first.take(30)}...",
//                progress
//            )
//            val tocEntity = TableOfContentEntity(bookId = bookId, title = tocReference.first, index = index)
//            tableOfContentsRepository.saveTableOfContent(tocEntity)
//            val startChapterPage = tocReference.second
//            val endChapterPage = if (index < tocList.size - 1) {
//                maxOf(startChapterPage, tocList[index + 1].second - 1)
//            } else {
//                document.numberOfPages
//            }
//            if (startChapterPage > endChapterPage || startChapterPage < 1 || startChapterPage > document.numberOfPages) {
//                saveEmptyChapter(bookId, index, tocReference.first)
//                return@forEachIndexed
//            }
//            val orderedStripper = OrderedContentStripper(
//                context = context, bookId = bookId, chapterIndex = index,
//                saveImageFunc = { bitmap, baseFileName ->
//                    this@PDFImportWorker.saveImageToPrivateStorage(context, bitmap, baseFileName)
//                }
//            )
//            orderedStripper.startPage = startChapterPage
//            orderedStripper.endPage = endChapterPage
//
//            val orderedPageElements: List<PageContentElement> = try {
//                orderedStripper.getText(document)
//                orderedStripper.getOrderedContent()
//            } catch (e: Exception) {
//                saveErrorChapter(bookId, index, tocReference.first,"[Error processing chapter content: ${e.javaClass.simpleName}]")
//                return@forEachIndexed
//            }
//            val chapterContentList = orderedPageElements.mapNotNull {
//                when (it) {
//                    is PageContentElement.Text -> it.text.takeIf { txt -> txt.isNotBlank() }
//                    is PageContentElement.Image -> it.path
//                }
//            }
//            val chapterEntity = ChapterContentEntity(
//                tocId = index, bookId = bookId, chapterTitle = tocReference.first, content = chapterContentList,
//            )
//            chapterRepository.saveChapterContent(chapterEntity)
//            val imagePathsInChapter = chapterContentList.filter { it.contains("${bookId}_chapter${index}") && it.endsWith(".webp") }
//            if (imagePathsInChapter.isNotEmpty()) {
//                imagePathRepository.saveImagePath(bookId, imagePathsInChapter)
//            }
//        }
//    }
//    @WorkerThread
//    private suspend fun processPagesAsImages(
//        pdfUri: android.net.Uri,
//        document: PDDocument,
//        bookId: String,
//        fileName: String,
//        notificationManager: NotificationManager,
//        notificationId: Int,
//        context: Context
//    ) {
//        val totalPages = document.numberOfPages
//        var rendererFd: ParcelFileDescriptor? = null
//        try {
//            rendererFd = context.contentResolver.openFileDescriptor(pdfUri, "r")
//            if (rendererFd == null) throw IOException("processPagesAsImages: Could not open PFD for PdfRenderer.")
//            PdfRenderer(rendererFd).use { renderer ->
//                for (pageIndex in 0 until totalPages) {
//                    val pageNumber = pageIndex + 1
//                    val progress = (pageNumber.toFloat() / totalPages * 100).toInt()
//                    val tocTitle = "Page $pageNumber"
//                    updateNotification(
//                        context, notificationManager, notificationId, fileName,
//                        "Processing Page $pageNumber/$totalPages",
//                        progress
//                    )
//                    val tocEntity = TableOfContentEntity(bookId = bookId, title = tocTitle, index = pageIndex)
//                    tableOfContentsRepository.saveTableOfContent(tocEntity)
//                    var pageImagePath: String? = null
//                    try {
//                        val page = renderer.openPage(pageIndex)
//                        val bitmap = createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
//                        val canvas = Canvas(bitmap)
//                        canvas.drawColor(Color.WHITE)
//                        canvas.drawBitmap(bitmap, 0f, 0f, null)
//                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//                        page.close()
//                        pageImagePath = saveImageToPrivateStorage(context, bitmap, "${bookId}_page_${pageNumber}")
//                    } catch (e: Exception) {
//                        saveErrorChapter(bookId, pageIndex, tocTitle, "[Error processing page image]")
//                        continue
//                    }
//                    val chapterEntity = ChapterContentEntity(
//                        tocId = pageIndex,
//                        bookId = bookId,
//                        chapterTitle = tocTitle,
//                        content = listOf(pageImagePath)
//                    )
//                    chapterRepository.saveChapterContent(chapterEntity)
//                    imagePathRepository.saveImagePath(bookId, listOf(pageImagePath))
//                }
//            }
//        } catch (e: Exception) {
//            throw e
//        }
//    }
//    @WorkerThread
//    private suspend fun saveEmptyChapter(bookId: String, tocIndex: Int, title: String) {
//        val emptyChapter = ChapterContentEntity(
//            tocId = tocIndex, bookId = bookId, chapterTitle = title, content = emptyList()
//        )
//        chapterRepository.saveChapterContent(emptyChapter)
//    }
//    @WorkerThread
//    private suspend fun saveErrorChapter(bookId: String, tocIndex: Int, title: String, errorMessage: String) {
//        val errorChapter = ChapterContentEntity(
//            tocId = tocIndex, bookId = bookId, chapterTitle = title, content = listOf(errorMessage)
//        )
//        chapterRepository.saveChapterContent(errorChapter)
//    }
//    @WorkerThread
//    private fun resolveBookmarkPageNumber(doc: PDDocument, bookmark: PDOutlineItem): Int {
//        try {
//            val destination = bookmark.destination
//            if (destination is PDPageDestination) {
//                return destination.retrievePageNumber()
//            } else if (destination is PDNamedDestination) {
//                val nameTree = doc.documentCatalog?.names?.dests
//                val pageDest = nameTree?.getValue(destination.namedDestination)
//                return pageDest?.retrievePageNumber() ?: -1
//            } else {
//                val action = bookmark.action
//                if (action is PDActionGoTo) {
//                    val actionDest = action.destination
//                    if (actionDest is PDPageDestination) {
//                        return actionDest.retrievePageNumber()
//                    } else if (actionDest is PDNamedDestination) {
//                        val nameTree = doc.documentCatalog?.names?.dests
//                        val pageDest = nameTree?.getValue(actionDest.namedDestination)
//                        return pageDest?.retrievePageNumber() ?: -1
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            throw e
//        }
//        return -1
//    }
//    @WorkerThread
//    private fun saveImageToPrivateStorage(context: Context, bitmap: Bitmap, baseFileName: String): String {
//        val safeBaseFileName = baseFileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
//        val fileName = "$safeBaseFileName.webp"
//        val file = context.getFileStreamPath(fileName)
//        try {
//            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
//                FileOutputStream(file).use { outputStream ->
//                    bitmap.compress(Bitmap.CompressFormat.WEBP, 80, outputStream)
//                }
//            } else {
//                FileOutputStream(file).use { outputStream ->
//                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, outputStream)
//                }
//            }
//            return file.absolutePath
//        } catch (e: IOException) {
//            throw e
//        }
//    }
//    private fun createNotificationChannel(context: Context, notificationManager: NotificationManager, channelId: String, channelName: String) {
//        val channel = NotificationChannel(
//            channelId,
//            channelName,
//            NotificationManager.IMPORTANCE_LOW
//        ).apply {
//            description = "Notifications for book import process"
//            setSound(null, null)
//        }
//        notificationManager.createNotificationChannel(channel)
//    }
//    private fun createNotificationBuilder(context: Context, fileName: String, message: String, notificationId: Int): NotificationCompat.Builder {
//        val channelId = "book_import_channel"
//        return NotificationCompat.Builder(context, channelId)
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .setContentTitle("Importing: $fileName")
//            .setContentText(message)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .setOngoing(true)
//            .setOnlyAlertOnce(true)
//    }
//
//    private fun sendCompletionNotification(
//        context: Context,
//        notificationManager: NotificationManager,
//        notificationId: Int,
//        isSuccess: Boolean,
//        bookTitle: String?,
//        specialMessage: String? = null
//    ) {
//        val channelId = "book_import_completion_channel"
//        val channelName = "Book Import Completion"
//        createNotificationChannel(context, notificationManager, channelId, channelName)
//        val title = if (isSuccess) "Import Successful" else "Import Failed"
//        val text = when {
//            isSuccess && bookTitle != null -> "'$bookTitle' imported successfully!"
//            isSuccess -> "Book import completed successfully!"
//            specialMessage != null -> "Failed to import '${bookTitle ?: "book"}': $specialMessage"
//            else -> "Book import failed for '${bookTitle ?: "book"}'."
//        }
//        val builder = NotificationCompat.Builder(context, channelId)
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .setContentTitle(title)
//            .setContentText(text)
//            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setAutoCancel(true)
//        notificationManager.notify(notificationId, builder.build())
//    }
//    private inner class OrderedContentStripper(
//        private val context: Context,
//        private val bookId: String,
//        private val chapterIndex: Int,
//        private val saveImageFunc: (Bitmap, String) -> String
//    ) : PDFTextStripper() {
//        private val pageContentList = mutableListOf<PageContentElement>()
//        private var currentPageNumber: Int = 0
//        private var imageCounterOnPage: Int = 0
//        private val textBuffer = StringBuilder()
//        private var lastTextYPosition: Float? = null
//        init {
//            this.paragraphStart = "\n<PARAGRAPH_START>\n"
//            this.paragraphEnd = "\n<PARAGRAPH_END>\n"
//            this.lineSeparator = "\n"
//        }
//        fun getOrderedContent(): List<PageContentElement> {
//            flushTextBuffer()
//            return pageContentList.toList()
//        }
//        override fun startPage(page: PDPage?) {
//            super.startPage(page)
//            currentPageNumber = currentPageNo
//            imageCounterOnPage = 0
//            textBuffer.setLength(0)
//            lastTextYPosition = null
//        }
//        override fun writeString(text: String?, textPositions: MutableList<TextPosition>?) {
//            if (text == null || text.isBlank()) return
//            textBuffer.append(text)
//            lastTextYPosition = textPositions?.lastOrNull()?.y ?: lastTextYPosition
//            super.writeString(text, textPositions)
//        }
//        @Throws(IOException::class)
//        override fun processOperator(operator: Operator, operands: List<COSBase>) {
//            val operation: String = operator.name
//            if ("Do" == operation && operands.isNotEmpty()) {
//                val objectName = operands[0] as? COSName ?: run {
//                    super.processOperator(operator, operands)
//                    return
//                }
//                val xObject: PDXObject? = try { resources?.getXObject(objectName) } catch (e: Exception) { null }
//                if (xObject is PDImageXObject) {
//                    flushTextBuffer()
//                    try {
//                        val imageFileNameBase = "${bookId}_chapter${chapterIndex}"
//                        val imagePath = saveImageInternal(xObject.image, imageFileNameBase)
//                        pageContentList.add(PageContentElement.Image(imagePath))
//                        imageCounterOnPage++
//                    } catch (e: Exception) {
//                        pageContentList.add(PageContentElement.Text("[Error processing image: ${objectName.name}]"))
//                    }
//                    return
//                }
//                super.processOperator(operator, operands)
//
//            } else {
//                super.processOperator(operator, operands)
//            }
//        }
//        override fun writeLineSeparator() {
//            textBuffer.append(" ")
//            lastTextYPosition = null
//        }
//        override fun writeParagraphSeparator() {
//            flushTextBuffer()
//            lastTextYPosition = null
//        }
//        override fun endPage(page: PDPage?) {
//            flushTextBuffer()
//            super.endPage(page)
//        }
//        private fun saveImageInternal(bitmap: Bitmap, baseFileName: String): String {
//            val uniqueFileName = "${baseFileName}_p${currentPageNumber}_img${imageCounterOnPage}"
//            return try {
//                saveImageFunc(bitmap, uniqueFileName)
//            } catch (e: Exception) {
//                "error_saving_image_${UUID.randomUUID()}"
//            }
//        }
//        private fun flushTextBuffer() {
//            if (textBuffer.isNotEmpty()) {
//                var textToAdd = textBuffer.toString()
//                textBuffer.setLength(0)
//                textToAdd = textToAdd.replace("<PARAGRAPH_START>", "").replace("<PARAGRAPH_END>", "")
//                textToAdd = textToAdd.replace("\t", " ")
//                textToAdd = textToAdd.replace(Regex("\\s{2,}"), " ").trim()
//                if (textToAdd.isNotBlank()) {
//                    pageContentList.add(PageContentElement.Text(textToAdd))
//                }
//            }
//            lastTextYPosition = null
//        }
//    }
//}


package com.capstone.bookshelf.util

// Assuming helpers are in the same package or imported
// import com.capstone.bookshelf.util.NaturalOrderComparator
// import com.capstone.bookshelf.util.saveBitmapToPrivateStorage
// import com.capstone.bookshelf.util.calculateInSampleSize // Less applicable here but keep for consistency if desired
// import com.tom_roush.pdfbox.io.RandomAccessBufferedFileInputStream // Not needed with file path
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
import android.util.Log
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

// Define PageContentElement if not already defined elsewhere accessible
sealed class PageContentElement {
    data class Text(val text: String) : PageContentElement()
    data class Image(val path: String) : PageContentElement()
}


class PDFImportWorker(
    private val appContext: Context, // Use appContext
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    // Koin Injected Repositories
    private val bookRepository: BookRepository by inject()
    private val tableOfContentsRepository: TableOfContentRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val imagePathRepository: ImagePathRepository by inject()

    // Member variables
    private val md = MessageDigest.getInstance("MD5")
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationId = System.currentTimeMillis().toInt() // Base ID for progress
    private val completionNotificationId = notificationId + 1 // Separate ID for completion

    // Processing modes
    private enum class ProcessingMode { TOC_BASED, IMAGE_ONLY }

    companion object {
        // Use same keys as CBZ worker for consistency
        const val INPUT_URI_KEY = "input_uri"
        const val ORIGINAL_FILENAME_KEY = "original_filename"
        private const val TAG = "PDFImportWorker"

        // Notification Channels (reuse IDs from CBZ if desired or make PDF specific)
        private const val PROGRESS_CHANNEL_ID = "book_import_progress_channel"
        private const val COMPLETION_CHANNEL_ID = "book_import_completion_channel"

        // Conceptually keep max dimension, though sampling isn't applied directly in PdfRenderer
        private const val MAX_BITMAP_DIMENSION = 2048
    }

    init {
        // Create notification channels
        createNotificationChannelIfNeeded(PROGRESS_CHANNEL_ID, "Book Import Progress")
        createNotificationChannelIfNeeded(COMPLETION_CHANNEL_ID, "Book Import Completion")
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val pdfUriString = inputData.getString(INPUT_URI_KEY)
        val originalFileName = inputData.getString(ORIGINAL_FILENAME_KEY)
            ?: getDisplayNameFromUri(appContext, pdfUriString?.toUri()) ?: "Unknown PDF" // Fetch display name

        if (pdfUriString == null) {
            Log.e(TAG, "Input data missing: $INPUT_URI_KEY")
            return@withContext Result.failure()
        }
        val pdfUri = pdfUriString.toUri()
        Log.i(TAG, "Starting PDF import for URI: $pdfUriString, FileName: $originalFileName")

        // --- Setup Foreground Service ---
        val initialNotification = createProgressNotificationBuilder(
            originalFileName, "Starting import..."
        ).build()
        try {
            setForeground(getForegroundInfoCompat(initialNotification))
        } catch (e: Exception) {
            Log.w(TAG, "Could not set foreground service: ${e.message}")
        }

        // --- Process the PDF File via Cache ---
        val processingResult = processPdfViaCache(
            appContext,
            pdfUri,
            originalFileName,
            // Progress callback
            onProgress = { progress, message ->
                updateProgressNotification(originalFileName, message, progress)
            }
        )

        // --- Handle Result and Completion Notification ---
        val isSuccess = processingResult.isSuccess
        val failureReason = if (!isSuccess) processingResult.exceptionOrNull()?.message else null
        val displayTitle = processingResult.getOrNull() // Book title returned on success
            ?: originalFileName.substringBeforeLast('.') // Fallback title

        sendCompletionNotification(isSuccess, displayTitle, failureReason)

        Log.i(TAG, "PDF import finished. Success: $isSuccess")
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
        onProgress: suspend (progress: Int?, message: String) -> Unit
    ): kotlin.Result<String> { // Returns book title on success
        var tempPdfFile: File? = null
        val bookTitle = originalFileName.substringBeforeLast('.') // Use filename as initial title
        var finalBookTitle: String = bookTitle // May be updated from PDF metadata
        var bookId: String? = null

        try {
            // --- Step 1: Copy to Cache ---
            Log.d(TAG, "Phase 1: Copying $pdfUri to temporary cache file...")
            onProgress(null, "Copying file...") // Indeterminate progress

            tempPdfFile = File.createTempFile("pdf_import_", ".pdf", context.cacheDir)
            Log.d(TAG, "Temporary file created at: ${tempPdfFile.absolutePath}")

            val bytesCopied = context.contentResolver.openInputStream(pdfUri)?.use { inputStream ->
                FileOutputStream(tempPdfFile).use { outputStream ->
                    inputStream.copyTo(outputStream, 8192) // 8KB buffer
                }
            } ?: run {
                Log.e(TAG, "Failed to open InputStream for URI: $pdfUri")
                tempPdfFile?.delete() // Clean up
                return kotlin.Result.failure(IOException("Failed to open InputStream for PDF"))
            }
            Log.d(TAG, "Phase 1 finished. Copied $bytesCopied bytes.")

            // --- Step 2: Process Cached PDF File ---
            Log.d(TAG, "Phase 2: Processing cached PDF file: ${tempPdfFile.absolutePath}")
            onProgress(null, "Loading PDF document...")

            // Load PDDocument from the temporary file
            PDDocument.load(tempPdfFile, "", MemoryUsageSetting.setupTempFileOnly()).use { document ->
                if (document.numberOfPages == 0) {
                    throw IOException("PDF document has no pages.")
                }
                val info: PDDocumentInformation = document.documentInformation
                finalBookTitle = info.title?.takeIf { it.isNotBlank() } ?: bookTitle // Update title if available
                bookId = BigInteger(1, md.digest(originalFileName.toByteArray())) // Use original filename for stable ID
                    .toString(16).padStart(32, '0')
                Log.d(TAG, "Using Book Title: $finalBookTitle, Generated Book ID: $bookId")

                // --- Check if book already exists ---
                if (bookRepository.isBookExist(finalBookTitle)) {
                    Log.w(TAG, "Book '$finalBookTitle' (ID: $bookId) already imported.")
                    return kotlin.Result.failure(IOException("Book already imported"))
                }

                // --- Generate Cover Image (using temp file path) ---
                onProgress(null, "Generating cover image...")
                val coverImagePath = generateAndSaveCoverImage(context, tempPdfFile, bookId) // Pass temp file
                val finalCoverPathForDb = if (coverImagePath == null || coverImagePath.startsWith("error_")) {
                    Log.w(TAG, "Using null or error cover path: $coverImagePath")
                    null
                } else {
                    coverImagePath
                }

                // --- Extract TOC ---
                onProgress(null, "Analyzing table of contents...")
                val tocList = extractToc(document)
                val authors = listOf(info.author?.takeIf { it.isNotBlank() } ?: "Unknown Author")
                val processingMode = if (tocList.isNotEmpty()) ProcessingMode.TOC_BASED else ProcessingMode.IMAGE_ONLY
                val totalChaptersOrPages = when (processingMode) {
                    ProcessingMode.TOC_BASED -> tocList.size
                    ProcessingMode.IMAGE_ONLY -> document.numberOfPages
                }

                Log.d(TAG, "Processing mode: $processingMode, Items: $totalChaptersOrPages")
                if (totalChaptersOrPages == 0) {
                    return kotlin.Result.failure(IOException("No chapters or pages found to process."))
                }

                // --- Save Book Info ---
                onProgress(null, "Saving book information...")
                saveBookInfo(
                    bookId, finalBookTitle, finalCoverPathForDb, totalChaptersOrPages,
                    tempPdfFile.absolutePath // Store temp path initially if needed
                )
                // Save cover path separately if needed (often BookEntity is enough)
                if (finalCoverPathForDb != null) {
                    imagePathRepository.saveImagePath(bookId, listOf(finalCoverPathForDb))
                }

                // --- Process Content based on Mode ---
                when (processingMode) {
                    ProcessingMode.TOC_BASED -> {
                        processChaptersWithToc(
                            document = document,
                            bookId = bookId,
                            tocList = tocList,
                            originalFileName = originalFileName, // Pass original for notifications
                            context = context,
                            onProgress = onProgress // Pass callback down
                        )
                    }
                    ProcessingMode.IMAGE_ONLY -> {
                        processPagesAsImages(
                            tempPdfFile = tempPdfFile, // Use temp file for renderer
                            bookId = bookId,
                            totalPages = totalChaptersOrPages, // Use calculated total
                            originalFileName = originalFileName,
                            context = context,
                            onProgress = onProgress // Pass callback down
                        )
                    }
                }

                return@use kotlin.Result.success(finalBookTitle) // Return final title
            } // PDDocument closed

        } catch (e: Exception) {
            Log.e(TAG, "Error during PDF processing via cache: ${e.message}", e)
            return kotlin.Result.failure(e) // Propagate exception
        } finally {
            // --- Cleanup ---
            if (tempPdfFile != null && tempPdfFile.exists()) {
                if (tempPdfFile.delete()) {
                    Log.d(TAG, "Temporary PDF file deleted: ${tempPdfFile.absolutePath}")
                } else {
                    Log.w(TAG, "Failed to delete temporary PDF file: ${tempPdfFile.absolutePath}")
                }
            }
        }
        return kotlin.Result.success(finalBookTitle)
    }

    // --- PDF Processing Logic (Modified to use temp File where needed) ---

    /** Generates cover using PdfRenderer from the cached file */
    private fun generateAndSaveCoverImage(context: Context, tempPdfFile: File, bookId: String): String? {
        var pfd: ParcelFileDescriptor? = null
        try {
            // Open the temp file for reading
            pfd = ParcelFileDescriptor.open(tempPdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            return PdfRenderer(pfd).use { renderer -> // Use closes PFD too
                if (renderer.pageCount > 0) {
                    renderer.openPage(0).use { page -> // Use closes page
                        // Create bitmap - OOM risk if page dimensions are huge
                        val bitmap = try {
                            createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                        } catch (oom: OutOfMemoryError) {
                            Log.e(TAG, "OOM creating cover bitmap (${page.width}x${page.height}).", oom)
                            return@use "error_oom_creating_bitmap" // Specific error
                        }
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE) // Background
                        // Render page onto bitmap - OOM risk if complex page
                        try {
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        } catch (oom: OutOfMemoryError) {
                            Log.e(TAG, "OOM rendering cover page.", oom)
                            bitmap.recycle() // Clean up partially created bitmap
                            return@use "error_oom_rendering_cover"
                        }
                        // Save the rendered bitmap
                        val coverFilename = "cover_${bookId}"
                        saveBitmapToPrivateStorage(context, bitmap, coverFilename).also {
                            bitmap.recycle() // Recycle after saving
                        }
                    }
                } else {
                    Log.w(TAG, "PDF has no pages, cannot generate cover.")
                    null // No pages
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate/save cover image: ${e.message}", e)
            return "error_generating_cover" // General error string
        } finally {
            try { pfd?.close() } catch (e: IOException) { /* Ignore close error */ }
        }
    }

    /** Extracts TOC using PdfBox from the already loaded document */
    private fun extractToc(document: PDDocument): List<Pair<String, Int>> { // Pair<Title, PageIndex+1>
        val tocList = mutableListOf<Pair<String, Int>>()
        try {
            val outline: PDDocumentOutline? = document.documentCatalog?.documentOutline
            var currentOutlineItem: PDOutlineItem? = outline?.firstChild
            while (currentOutlineItem != null) {
                val title = currentOutlineItem.title?.trim()?.takeIf { it.isNotEmpty() } ?: "Untitled Chapter"
                val pageNumber = resolveBookmarkPageNumber(document, currentOutlineItem) // 0-based index
                if (pageNumber != -1) {
                    tocList.add(title to (pageNumber + 1)) // Store 1-based page number
                }
                currentOutlineItem = currentOutlineItem.nextSibling
            }
            tocList.sortBy { it.second } // Sort by page number
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting TOC: ${e.message}", e)
            return emptyList() // Return empty list on error
        }
        return tocList
    }

    /** Resolves bookmark destination to a 0-based page index */
    private fun resolveBookmarkPageNumber(doc: PDDocument, bookmark: PDOutlineItem): Int {
        try {
            val destination = bookmark.destination
            val page = when (destination) {
                is PDPageDestination -> destination.page ?: doc.getPage(destination.pageNumber) // Prefer page object
                is PDNamedDestination -> {
                    val nameTree = doc.documentCatalog?.names?.dests
                    val pageDest = nameTree?.getValue(destination.namedDestination) as? PDPageDestination
                    pageDest?.page ?: if(pageDest != null) doc.getPage(pageDest.pageNumber) else null
                }
                else -> {
                    val action = bookmark.action
                    if (action is PDActionGoTo) {
                        val actionDest = action.destination
                        when(actionDest) {
                            is PDPageDestination -> actionDest.page ?: doc.getPage(actionDest.pageNumber)
                            is PDNamedDestination -> {
                                val nameTree = doc.documentCatalog?.names?.dests
                                val pageDest = nameTree?.getValue(actionDest.namedDestination) as? PDPageDestination
                                pageDest?.page ?: if(pageDest != null) doc.getPage(pageDest.pageNumber) else null
                            }
                            else -> null
                        }
                    } else null
                }
            }
            // Find the index of the resolved page
            return if (page != null) doc.pages.indexOf(page) else -1
        } catch (e: Exception) {
            Log.e(TAG,"Error resolving bookmark page for '${bookmark.title}': ${e.message}")
            return -1
        }
    }


    /** Processes PDF content chapter by chapter based on TOC */
    private suspend fun processChaptersWithToc(
        document: PDDocument,
        bookId: String,
        tocList: List<Pair<String, Int>>, // Pair<Title, PageNumber (1-based)>
        originalFileName: String, // For notifications
        context: Context,
        onProgress: suspend (progress: Int?, message: String) -> Unit
    ) {
        val totalChapters = tocList.size
        tocList.forEachIndexed { index, tocEntry ->
            val chapterTitle = tocEntry.first
            val startPageNumber = tocEntry.second // 1-based
            val endPageNumber = if (index < tocList.size - 1) {
                maxOf(startPageNumber, tocList[index + 1].second - 1) // End before next chapter starts
            } else {
                document.numberOfPages // Last chapter goes to the end
            }

            val progressPercent = ((index + 1).toFloat() / totalChapters * 100).toInt()
            onProgress(
                progressPercent,
                "Processing Chapter ${index + 1}/$totalChapters: ${chapterTitle.take(30)}..."
            )

            // --- Save TOC Entry ---
            // Assuming TableOfContentEntity uses 0-based index
            saveTableOfContentEntry(bookId, chapterTitle, index)

            // --- Validate Page Range ---
            if (startPageNumber > endPageNumber || startPageNumber < 1 || startPageNumber > document.numberOfPages) {
                Log.w(TAG, "Invalid page range for chapter '$chapterTitle' ($startPageNumber-$endPageNumber). Saving empty chapter.")
                saveEmptyChapterContent(bookId, chapterTitle, index) // Use index
                return@forEachIndexed // Skip to next chapter
            }

            // --- Extract Content using Stripper ---
            val orderedStripper = OrderedContentStripper(
                context = context,
                bookId = bookId,
                chapterIndex = index, // Pass 0-based index
                saveImageFunc = { bitmap, baseFileName ->
                    // Use the helper save function directly
                    saveBitmapToPrivateStorage(context, bitmap, baseFileName)
                }
            )
            orderedStripper.startPage = startPageNumber // Set 1-based start page for stripper
            orderedStripper.endPage = endPageNumber   // Set 1-based end page

            val orderedPageElements: List<PageContentElement> = try {
                // Run stripper on the document within the specified page range
                orderedStripper.getText(document) // This extracts text AND processes images via processOperator
                orderedStripper.getOrderedContent() // Retrieve collected elements
            } catch (e: Exception) {
                Log.e(TAG, "Error stripping content for chapter '$chapterTitle': ${e.message}", e)
                saveErrorChapterContent(bookId, chapterTitle, index, "[Error processing chapter content]")
                return@forEachIndexed // Skip to next chapter
            }

            // --- Prepare and Save Chapter Content ---
            val chapterContentList = orderedPageElements.mapNotNull {
                when (it) {
                    // Ensure text isn't just whitespace before saving
                    is PageContentElement.Text -> it.text.takeIf { txt -> txt.isNotBlank() }
                    is PageContentElement.Image -> it.path.takeIf { path -> !path.startsWith("error_") } // Only store valid paths
                }
            }

            if (chapterContentList.isNotEmpty()) {
                saveChapterContent(bookId, chapterTitle, index, chapterContentList)
                // Save image paths separately if needed (seems redundant)
                val imagePathsInChapter = chapterContentList.filter { it.contains("${bookId}_chapter${index}") && it.endsWith(".webp") }
                if (imagePathsInChapter.isNotEmpty()) {
                    imagePathRepository.saveImagePath(bookId, imagePathsInChapter)
                }
            } else {
                Log.w(TAG, "Chapter '$chapterTitle' resulted in no content after processing.")
                saveEmptyChapterContent(bookId, chapterTitle, index) // Save as empty if no content extracted
            }
        }
    }

    /** Processes PDF page by page, saving each as an image */
    private suspend fun processPagesAsImages(
        tempPdfFile: File, // Use cached file
        bookId: String,
        totalPages: Int,
        originalFileName: String, // For notifications
        context: Context,
        onProgress: suspend (progress: Int?, message: String) -> Unit
    ) {
        var pfd: ParcelFileDescriptor? = null
        try {
            pfd = ParcelFileDescriptor.open(tempPdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            PdfRenderer(pfd).use { renderer -> // Use closes PFD
                if (totalPages != renderer.pageCount) {
                    Log.w(TAG, "Mismatch between PDDocument page count ($totalPages) and PdfRenderer page count (${renderer.pageCount}). Using renderer count.")
                }
                val actualTotalPages = renderer.pageCount // Use renderer's count

                for (pageIndex in 0 until actualTotalPages) {
                    val pageNumber = pageIndex + 1
                    val progressPercent = (pageNumber.toFloat() / actualTotalPages * 100).toInt()
                    val tocTitle = "Page $pageNumber"

                    onProgress(progressPercent, "Processing Page $pageNumber/$actualTotalPages")

                    // --- Save TOC Entry for Page ---
                    saveTableOfContentEntry(bookId, tocTitle, pageIndex)

                    // --- Render and Save Page Image ---
                    var pageImagePath: String? = null
                    try {
                        renderer.openPage(pageIndex).use { page -> // Use closes page
                            // Create bitmap - OOM Risk
                            val bitmap = try {
                                createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                            } catch (oom: OutOfMemoryError) {
                                Log.e(TAG, "OOM creating bitmap for page $pageNumber (${page.width}x${page.height}).", oom)
                                throw oom // Re-throw to be caught below, saves error chapter
                            }
                            val canvas = Canvas(bitmap)
                            canvas.drawColor(Color.WHITE)
                            // Render - OOM Risk
                            try {
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            } catch (oom: OutOfMemoryError) {
                                Log.e(TAG, "OOM rendering page $pageNumber.", oom)
                                bitmap.recycle() // Clean up
                                throw oom // Re-throw
                            }
                            // Save
                            pageImagePath = saveBitmapToPrivateStorage(context, bitmap, "${bookId}_page_${pageNumber}")
                                .also { bitmap.recycle() } // Recycle after saving
                        }
                    } catch (e: Exception) { // Catch OOM or other rendering errors
                        Log.e(TAG, "Error rendering or saving page $pageNumber: ${e.message}", e)
                        saveErrorChapterContent(bookId, tocTitle, pageIndex, "[Error processing page image]")
                        continue // Skip to next page
                    }

                    // --- Save Chapter (Page) Content ---
                    if (pageImagePath != null && !pageImagePath!!.startsWith("error_")) {
                        saveChapterContent(bookId, tocTitle, pageIndex, listOf(pageImagePath!!))
                        // Save image path separately if needed
                        imagePathRepository.saveImagePath(bookId, listOf(pageImagePath!!))
                    } else {
                        Log.w(TAG, "Failed to get valid image path for page $pageNumber. Saving error chapter.")
                        saveErrorChapterContent(bookId, tocTitle, pageIndex, "[Failed to save page image]")
                    }
                } // End page loop
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during page-by-page image processing: ${e.message}", e)
            // This likely means PdfRenderer failed to initialize, re-throw to fail the worker
            throw e
        } finally {
            try { pfd?.close() } catch (e: IOException) { /* Ignore */ }
        }
    }


    // --- Database Interaction Helpers ---

    private suspend fun saveBookInfo(
        bookID: String, title: String, coverImagePath: String?,
        totalChapters: Int, storagePath: String // Can be temp path or original URI string
    ): Long {
        val bookEntity = BookEntity(
            bookId = bookID, title = title, coverImagePath = coverImagePath!!,
            authors = listOf("Unknown"), // Keep default or enhance later
            categories = emptyList(), description = null, // Add description if available
            totalChapter = totalChapters, currentChapter = 0, currentParagraph = 0,
            storagePath = storagePath, // Decide whether to store temp or original path
            isEditable = false, fileType = "pdf"
        )
        return bookRepository.insertBook(bookEntity)
    }

    private suspend fun saveTableOfContentEntry(bookId: String, title: String, index: Int): Long {
        val tocEntity = TableOfContentEntity(bookId = bookId, title = title, index = index)
        // Assuming saveTableOfContent returns the inserted row ID or generated primary key
        return tableOfContentsRepository.saveTableOfContent(tocEntity)
    }

    private suspend fun saveChapterContent(bookId: String, title: String, index: Int, content: List<String>) {
        // Assuming ChapterContentEntity links via bookId and index, or a TOC ID from saveTableOfContentEntry
        val chapterEntity = ChapterContentEntity(
            tocId = index, // Assuming tocId is same as index for simplicity, adjust if needed
            bookId = bookId,
            chapterTitle = title,
            content = content,
        )
        chapterRepository.saveChapterContent(chapterEntity)
    }

    private suspend fun saveEmptyChapterContent(bookId: String, title: String, index: Int) {
        saveChapterContent(bookId, title, index, emptyList())
        Log.d(TAG, "Saved empty chapter entry for '$title' (Index: $index)")
    }

    private suspend fun saveErrorChapterContent(bookId: String, title: String, index: Int, errorMessage: String) {
        saveChapterContent(bookId, title, index, listOf(errorMessage))
        Log.w(TAG, "Saved error chapter entry for '$title' (Index: $index): $errorMessage")
    }

    // --- Notification Helpers (Reused from CBZ Worker, ensure channel IDs match) ---

    private fun createNotificationChannelIfNeeded(channelId: String, channelName: String) {
        if (notificationManager.getNotificationChannel(channelId) == null) {
            val importance = if (channelId == PROGRESS_CHANNEL_ID) NotificationManager.IMPORTANCE_LOW else NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Notifications for book import process" // Generic description
                if (channelId == PROGRESS_CHANNEL_ID) setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createProgressNotificationBuilder(fileName: String, message: String): NotificationCompat.Builder {
        val displayFileName = fileName.substringBeforeLast(".")
        return NotificationCompat.Builder(appContext, PROGRESS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
            .setContentTitle("Importing PDF: ${displayFileName.take(35)}${if (displayFileName.length > 35) "..." else ""}")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
    }

    private suspend fun updateProgressNotification(fileName: String, message: String, progress: Int?) {
        // Ensure updates happen on the correct thread if necessary (usually IO is fine for notify)
        // withContext(Dispatchers.Main) { ... }
        val builder = createProgressNotificationBuilder(fileName, message)
        if (progress != null) {
            builder.setProgress(100, progress.coerceIn(0, 100), false) // Determinate
        } else {
            builder.setProgress(0, 0, true) // Indeterminate
        }
        try {
            setForeground(getForegroundInfoCompat(builder.build()))
        } catch (e: Exception) {
            Log.w(TAG, "Error updating foreground notification: ${e.message}")
            notificationManager.notify(notificationId, builder.build()) // Fallback
        }
    }

    private fun sendCompletionNotification(
        isSuccess: Boolean, bookTitle: String?, failureReason: String? = null
    ) {
        val title = if (isSuccess) "Import Successful" else "Import Failed"
        val defaultTitle = bookTitle ?: "PDF File"

        // Refine failure messages
        val userFriendlyReason = when {
            failureReason == null -> null
            failureReason.contains("Book already imported") -> "This book is already in your library."
            failureReason.contains("No valid image entries found") -> "No content could be extracted." // Adjusted for PDF
            failureReason.contains("Failed to open InputStream") -> "Could not read the selected file."
            failureReason.contains("OutOfMemoryError") -> "Ran out of memory processing the PDF. It might be too large or complex."
            failureReason.contains("PDF document has no pages") -> "The selected PDF file is empty."
            // Add more specific error mappings
            else -> "An unexpected error occurred." // Generic fallback
        }

        val text = when {
            isSuccess -> "'$defaultTitle' added to your library."
            userFriendlyReason != null -> "Failed to import '$defaultTitle': $userFriendlyReason"
            else -> "Import failed for '$defaultTitle'."
        }

        val builder = NotificationCompat.Builder(appContext, COMPLETION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace icon
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(completionNotificationId, builder.build())
        notificationManager.cancel(notificationId) // Ensure progress is dismissed
    }

    // --- Utility Helpers ---

    /** Gets display name from content URI. */
    private fun getDisplayNameFromUri(context: Context, uri: Uri?): String? {
        if (uri == null) return null
        // Implementation same as in CBZ worker...
        var displayName: String? = null
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        displayName = cursor.getString(nameIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not query display name for URI $uri: ${e.message}")
        }
        return displayName
    }

    /** Provides ForegroundInfo, handling platform differences. */
    private fun getForegroundInfoCompat(notification: Notification): ForegroundInfo {
        // Implementation same as in CBZ worker...
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    // --- Inner Class: OrderedContentStripper (Modified for context/saving) ---
    // Note: This class still uses PdfBox library specific features
    private inner class OrderedContentStripper(
        private val context: Context, // Needs context for saving
        private val bookId: String,
        private val chapterIndex: Int, // Use 0-based index
        // Function to save the bitmap, returns the path or error string
        private val saveImageFunc: (bitmap: Bitmap, baseFileName: String) -> String
    ) : PDFTextStripper() {

        private val pageContentList = mutableListOf<PageContentElement>()
        private var currentPageNumberForImages: Int = 0 // Track page for image naming
        private var imageCounterOnPage: Int = 0
        private val currentTextParagraph = StringBuilder() // Buffer for current paragraph

        init {
            // Configure PdfBox stripper behavior if needed (defaults are often ok)
            // paragraphStart = "\n" // Customize paragraph detection if necessary
            // paragraphEnd = "\n"
            lineSeparator = " " // Treat newlines within PDF as spaces unless paragraph break
            sortByPosition = true // Crucial for reading order
        }

        /** Returns the collected content, ensuring final text is flushed. */
        fun getOrderedContent(): List<PageContentElement> {
            flushTextBuffer() // Make sure any trailing text is added
            return pageContentList.toList()
        }

        override fun startPage(page: PDPage?) {
            super.startPage(page)
            flushTextBuffer() // Flush text from previous page before starting new one
            currentPageNumberForImages = currentPageNo // Update page number (1-based from stripper)
            imageCounterOnPage = 0 // Reset image counter for the new page
        }

        // This method is called for each chunk of text PdfBox processes.
        override fun writeString(text: String?, textPositions: MutableList<TextPosition>?) {
            currentTextParagraph.append(text ?: "")
            // We don't call super.writeString as we handle text buffering ourselves
        }

        // Process image operators ('Do')
        @Throws(IOException::class)
        override fun processOperator(operator: Operator, operands: List<COSBase>) {
            val operation: String = operator.name
            if ("Do" == operation && operands.isNotEmpty()) {
                val objectName = operands.firstOrNull() as? COSName
                if (objectName != null) {
                    val xObject: PDXObject? = try {
                        resources?.getXObject(objectName) // Get resource by name
                    } catch (e: Exception) {
                        Log.w(TAG, "Error getting XObject ${objectName.name}: ${e.message}")
                        null
                    }

                    if (xObject is PDImageXObject) {
                        // Found an image! Flush any preceding text first.
                        flushTextBuffer()
                        // Try to extract and save the image
                        try {
                            // Get bitmap - OOM Risk!
                            val bitmap = try { xObject.image } catch (oom: OutOfMemoryError) {
                                Log.e(TAG, "OOM getting image from PDF object ${objectName.name}", oom)
                                null // Handle OOM
                            }

                            if (bitmap != null) {
                                val imageFileNameBase = "${bookId}_chapter${chapterIndex}" // Base name for this chapter's images
                                val imagePath = saveImageInternal(bitmap, imageFileNameBase) // Save & get path
                                pageContentList.add(PageContentElement.Image(imagePath)) // Add image path to content
                                imageCounterOnPage++
                                // Don't recycle here if saveImageInternal needs it, assume save func handles it or pass copy
                            } else {
                                Log.w(TAG, "Could not get bitmap for image object ${objectName.name}")
                                pageContentList.add(PageContentElement.Text("[Image not loaded: ${objectName.name}]"))
                            }

                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing/saving image ${objectName.name}: ${e.message}", e)
                            pageContentList.add(PageContentElement.Text("[Error processing image: ${objectName.name}]"))
                        }
                        // We processed the 'Do' operator for the image, don't call super
                        return
                    }
                }
            }
            // If it wasn't an image 'Do' operator we handled, let the superclass process it
            super.processOperator(operator, operands)
        }

        // Called by PdfBox at the end of a line -> Treat as space in paragraph
        override fun writeLineSeparator() {
            currentTextParagraph.append(" ")
            // Don't call super.writeLineSeparator()
        }

        // Called by PdfBox between paragraphs -> Flush buffer
        override fun writeParagraphSeparator() {
            flushTextBuffer() // This signifies end of a paragraph
            // Don't call super.writeParagraphSeparator()
        }

        override fun endPage(page: PDPage?) {
            flushTextBuffer() // Flush any remaining text at end of page
            super.endPage(page)
        }

        /** Saves the image using the provided function and handles potential errors. */
        private fun saveImageInternal(bitmap: Bitmap, baseFileName: String): String {
            // Create a unique name incorporating page and image count
            val uniqueFileName = "${baseFileName}_p${currentPageNumberForImages}_img${imageCounterOnPage}"
            return try {
                saveImageFunc(bitmap, uniqueFileName) // Call the injected save function
            } catch (e: Exception) {
                Log.e(TAG, "Exception in provided saveImageFunc for $uniqueFileName", e)
                "error_saving_image_${UUID.randomUUID()}" // Unique error marker
            }
            // Note: Bitmap recycling should ideally happen *after* saveImageFunc completes.
            // The current structure assumes saveImageFunc uses the bitmap immediately.
            // If saveImageFunc operates async, care must be taken with recycling.
        }

        /** Adds the buffered text (if any) to the content list as a Text element. */
        private fun flushTextBuffer() {
            if (currentTextParagraph.isNotEmpty()) {
                // Basic cleanup: trim whitespace, normalize multiple spaces
                val cleanedText = currentTextParagraph.toString()
                    .replace("\t", " ") // Replace tabs with spaces
                    .replace(Regex("\\s{2,}"), " ") // Replace multiple spaces with one
                    .trim()

                if (cleanedText.isNotBlank()) {
                    pageContentList.add(PageContentElement.Text(cleanedText))
                }
                currentTextParagraph.setLength(0) // Clear the buffer
            }
        }
    } // End OrderedContentStripper
}