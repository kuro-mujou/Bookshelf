package com.capstone.bookshelf.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import android.widget.Toast
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
        const val FILE_NAME_KEY = "file_name"
    }

    private val bookRepository: BookRepository by inject()
    private val tableOfContentsRepository: TableOfContentRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val imagePathRepository: ImagePathRepository by inject()
    private val md = MessageDigest.getInstance("MD5")

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                setForeground(createForegroundInfo(context))
                val pdfPath = inputData.getString(BOOK_CACHE_PATH_KEY) ?: return@withContext Result.failure()
                val fileName = inputData.getString(FILE_NAME_KEY) ?: return@withContext Result.failure()
                processPDFtoBook(context, pdfPath,fileName)
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            } finally {
                context.cacheDir.deleteRecursively()
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(1234)
            }
        }
    }
    private suspend fun processPDFtoBook(context: Context, pdfUriString: String, fileName: String) {
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
                            bookTitle = info.title?: fileName.substring(0, fileName.length - 4)
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
                            authors.add(info.author?:"Unnamed Author")
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
                                                    imagePathRepository.saveImagePath(bookID, listOf(imagePath)) // Still save to general image path, consider chapter specific storage if needed
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
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to import book", Toast.LENGTH_SHORT).show()
            } finally {
                pfd?.close()
                Toast.makeText(context, "Book imported successfully", Toast.LENGTH_SHORT).show()
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

    private fun createForegroundInfo(context : Context): ForegroundInfo {
        val channelId = "book_import_channel"
        val channelName = "Book Import"
        val notificationId = 1234
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(context,
            channelId
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Bookshelf")
            .setContentText("Importing book...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }
}