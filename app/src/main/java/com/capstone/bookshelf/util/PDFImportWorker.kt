package com.capstone.bookshelf.util

import android.annotation.SuppressLint
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
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.domain.repository.ChapterRepository
import com.capstone.bookshelf.domain.repository.ImagePathRepository
import com.capstone.bookshelf.domain.repository.TableOfContentRepository
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.io.RandomAccessBufferedFileInputStream
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
import com.tom_roush.pdfbox.pdmodel.PDResources
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
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
import java.io.FileInputStream
import java.math.BigInteger
import java.security.MessageDigest

class PDFImportWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) , KoinComponent {

    companion object {
        const val BOOK_CACHE_PATH_KEY = "book_cache_path"
        const val FILE_NAME_KEY = "file_name"
    }

    private val bookRepository: BookRepository by inject()
    private val tableOfContentsRepository: TableOfContentRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val imagePathRepository: ImagePathRepository by inject()
    private val md = MessageDigest.getInstance("MD5")

    @SuppressLint("RestrictedApi")
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val notificationId = 1234
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            try {
                val pdfPath = inputData.getString(BOOK_CACHE_PATH_KEY) ?: return@withContext Result.failure()
                val fileName = inputData.getString(FILE_NAME_KEY) ?: return@withContext Result.failure()
                val initialNotification = createNotificationBuilder(context,fileName).build()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ForegroundInfo(notificationId, initialNotification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } else {
                    ForegroundInfo(notificationId, initialNotification)
                }
                val result = processPDFtoBook(context, pdfPath,fileName, notificationManager, notificationId)
                when (result) {
                    is Result.Success -> {
                        sendCompletionNotification(context, notificationManager)
                        return@withContext Result.success()
                    }
                    is Result.Failure -> {
                        sendCompletionNotification(context, notificationManager, isSuccess = false, specialMessage = "Book already imported")
                        return@withContext Result.failure()
                    }
                }
                sendCompletionNotification(context, notificationManager)
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                sendCompletionNotification(context, notificationManager, isSuccess = false)
                Result.failure()
            } finally {
                context.cacheDir.deleteRecursively()
                notificationManager.cancel(notificationId)
            }
        }
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
    private suspend fun processPDFtoBook(
        context: Context,
        pdfUriString: String,
        fileName: String,
        notificationManager: NotificationManager,
        notificationId: Int,
    ): Result {
        var bookTitle: String?
        val authors = mutableListOf<String>()
        var coverImage: Bitmap
        var bookID: String
        val tocList = mutableListOf<Pair<String, Int>>()
        var pfd: ParcelFileDescriptor? = null
        try {
            updateNotification(
                context = context,
                notificationManager = notificationManager,
                notificationId = notificationId,
                fileName = fileName,
                message = "Loading $fileName",
            )
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
                        bookTitle = info.title?: fileName.substringBeforeLast(".")
                        val isAlreadyImported = bookRepository.isBookExist(bookTitle!!)
                        if (isAlreadyImported) {
                            sendCompletionNotification(context, notificationManager, isSuccess = false, specialMessage = "Book already imported")
                            return Result.failure()
                        }
                        bookID = BigInteger(1,md.digest(bookTitle!!.toByteArray())).toString(16).padStart(32, '0')
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
                        val coverImagePath = saveImageToPrivateStorage(context, coverImage, "cover_${bookTitle}")
                        authors.add(info.author?:"Unnamed Author")
                        val outline: PDDocumentOutline =
                            document.documentCatalog?.documentOutline ?: return Result.failure()
                        var bookmark: PDOutlineItem? = outline.firstChild ?: return Result.failure()
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
                            } else {
                                tocList.add("Sample Bookmark" to 0)
                            }
                            bookmark = bookmark.nextSibling
                        }
                        updateNotification(
                            context = context,
                            notificationManager = notificationManager,
                            notificationId = notificationId,
                            fileName = fileName,
                            message = "Saving book info",
                        )
                        val bookEntity = BookEntity(
                            bookId = bookID,
                            title = bookTitle!!,
                            coverImagePath = coverImagePath,
                            authors = authors,
                            categories = emptyList(),
                            description = null,
                            totalChapter = tocList.size,
                            storagePath = pdfUriString,
                            isEditable = false
                        )
                        bookRepository.insertBook(bookEntity)
                        imagePathRepository.saveImagePath(bookID, listOf(coverImagePath))
                        val stripper = PDFTextStripper()
                        stripper.paragraphStart = "\n\n"
                        stripper.paragraphEnd = ""
                        stripper.lineSeparator = "\n"
                        tocList.forEachIndexed { index, tocReference ->
                            updateNotification(
                                context = context,
                                notificationManager = notificationManager,
                                notificationId = notificationId,
                                fileName = fileName,
                                message = "Saving chapter $index",
                            )
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
                                content = emptyList(),
                            )
                            val chapterContentWithImages = mutableListOf<String>()
                            var paragraphIndex = 0
                            for (pageNumber in startChapterPage..endChapterPage) {
                                val page = document.getPage(pageNumber - 1)
                                val resources: PDResources = page.resources
                                val xObjectNames = resources.xObjectNames
                                val xObjectSet: Set<COSName>? = xObjectNames?.toSet()

                                var imageIndex = 0
                                xObjectSet?.let {
                                    for (xObjectName in it) {
                                        val xObject = resources.getXObject(xObjectName)
                                        if (xObject is PDImageXObject) {
                                            try {
                                                val imageFileName = "${bookID}_chapter${index}_image${imageIndex}_page${pageNumber}"
                                                val imagePath = saveImageToPrivateStorage(context, xObject.image, imageFileName)
                                                if (paragraphIndex < paragraphs.size) {
                                                    chapterContentWithImages.add(paragraphs[paragraphIndex])
                                                    paragraphIndex++
                                                }
                                                chapterContentWithImages.add(imagePath)
                                                imagePathRepository.saveImagePath(bookID, listOf(imagePath))
                                                imageIndex++
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                            }
                            while (paragraphIndex < paragraphs.size) {
                                chapterContentWithImages.add(paragraphs[paragraphIndex])
                                paragraphIndex++
                            }
                            val updatedChapterEntity = chapterEntity.copy(content = chapterContentWithImages)
                            chapterRepository.saveChapterContent(updatedChapterEntity)
                        }
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        } finally {
            pfd?.close()
        }
    }
    private fun parseChapterContentToList(text: String): List<String> {
        val paragraphs = text.split("\n\n")
            .map { it.replace("\t", " ").replace("\n", " ").trim() }
            .filter { it.isNotEmpty() }
        return paragraphs
    }
    private fun sendCompletionNotification(
        context: Context,
        notificationManager: NotificationManager,
        isSuccess: Boolean = true,
        specialMessage: String? = ""
    ) {
        val completionNotification = createCompletionNotificationBuilder(context, isSuccess,specialMessage).build()
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
            .setContentText("Loading PDF file")
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