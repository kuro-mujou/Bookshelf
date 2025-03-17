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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class CBZImportWorker(
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
            val notificationId = 1234
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            try {
                val cbzPath = inputData.getString(BOOK_CACHE_PATH_KEY) ?: return@withContext Result.failure()
                val fileName = inputData.getString(FILE_NAME_KEY) ?: return@withContext Result.failure()
                val isAlreadyImported = bookRepository.isBookExist(fileName.substringBeforeLast("."))
                if (isAlreadyImported) {
                    sendCompletionNotification(context, notificationManager, isSuccess = false, specialMessage = "Book already imported")
                    return@withContext Result.failure()
                }
                val initialNotification = createNotificationBuilder(context,fileName).build()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ForegroundInfo(notificationId, initialNotification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } else {
                    ForegroundInfo(notificationId, initialNotification)
                }
                processCBZtoBook(
                    context = context,
                    cbzPath = cbzPath,
                    fileName = fileName,
                    notificationManager = notificationManager,
                    notificationId = notificationId
                )
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
    private suspend fun processCBZtoBook(
        context: Context,
        cbzPath: String,
        fileName: String,
        notificationManager: NotificationManager,
        notificationId: Int
    ) {
        return withContext(Dispatchers.IO) {
            updateNotification(
                context = context,
                notificationManager = notificationManager,
                notificationId = notificationId,
                fileName = fileName,
                message = "Loading $fileName",
            )
            val uri = cbzPath.toUri()
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            val bookID = BigInteger(1, md.digest(fileName.toByteArray())).toString(16)
                .padStart(32, '0')
            val chapterImagesMap = mutableMapOf<String, List<String>>()
            val allImagePaths = mutableListOf<String>()
            pfd?.use { fd ->
                val inputStream = FileInputStream(fd.fileDescriptor)
                ZipInputStream(inputStream).use { zipInputStream ->
                    var entry: ZipEntry? = zipInputStream.nextEntry
                    while (entry != null) {
                        val entryName = entry.name
                        if (isImageFile(entryName)) {
                            val folderName = entryName.substringBeforeLast('/').trim('/')
                            val bitmap = BitmapFactory.decodeStream(zipInputStream)
                            if (bitmap != null) {
                                val savedPath = saveImageToPrivateStorage(
                                    context = context,
                                    bitmap = bitmap,
                                    filename = "${bookID}_${folderName.substringAfterLast('/')}_${entryName.substringAfterLast('/').substringBefore(".")}"
                                )
                                allImagePaths.add(savedPath)
                                updateNotification(
                                    context = context,
                                    notificationManager = notificationManager,
                                    notificationId = notificationId,
                                    fileName = fileName,
                                    message = "Importing ${folderName.substringAfterLast('/')}",
                                )
                                if (folderName.isNotEmpty()) {
                                    val existingImages = chapterImagesMap[folderName]?.toMutableList() ?: mutableListOf()
                                    existingImages.add(savedPath)
                                    chapterImagesMap[folderName] = existingImages
                                }
                            }
                        }
                        entry = zipInputStream.nextEntry
                    }
                }
            }
            val sortedChapters = chapterImagesMap.keys.sortedBy { extractChapterNumber(it) }
            val sortedChapterImagesMap = chapterImagesMap.toSortedMap(compareBy { extractChapterNumber(it) })
            val coverImagePath: String? = sortedChapters.firstOrNull()?.let { sortedChapterImagesMap[it]?.firstOrNull() }
            if (coverImagePath != null) {
                updateNotification(
                    context = context,
                    notificationManager = notificationManager,
                    notificationId = notificationId,
                    fileName = fileName,
                    message = "Saving book info",
                )
                saveBookInfo(
                    bookID = bookID,
                    fileName = fileName.substringBeforeLast("."),
                    coverImagePath = coverImagePath,
                    totalChapters = sortedChapterImagesMap.size,
                    cacheFilePath = cbzPath
                )
                updateNotification(
                    context = context,
                    notificationManager = notificationManager,
                    notificationId = notificationId,
                    fileName = fileName,
                    message = "Saving chapter info",
                )
                saveBookContent(
                    bookID = bookID,
                    chapterImagesMap = sortedChapterImagesMap,
                    allImagePaths = allImagePaths
                )
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
    private suspend fun saveBookInfo(
        bookID: String,
        fileName: String,
        coverImagePath: String,
        totalChapters: Int,
        cacheFilePath: String
    ): Long {
        val bookEntity = BookEntity(
            bookId = bookID,
            title = fileName,
            coverImagePath = coverImagePath,
            authors = listOf("Unnamed Author"),
            categories = emptyList(),
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
        chapterImagesMap: Map<String, List<String>>,
        allImagePaths: List<String>,
    ) {
        var counter = 0
        chapterImagesMap.forEach{ (chapterTitle, imagePaths) ->
            val tocEntity = TableOfContentEntity(
                bookId = bookID,
                title = chapterTitle.substringAfterLast('/'),
                index = counter
            )
            tableOfContentsRepository.saveTableOfContent(tocEntity)
            val chapterEntity = ChapterContentEntity(
                tocId = counter,
                bookId = bookID,
                chapterTitle = chapterTitle.substringAfterLast('/'),
                content = imagePaths,
            )
            counter++
            chapterRepository.saveChapterContent(chapterEntity)
        }
        imagePathRepository.saveImagePath(bookID, allImagePaths)
    }
    private fun extractChapterNumber(name: String): Int? {
        val match = Regex("\\d+").find(name)
        return match?.value?.toIntOrNull()
    }
    private fun isImageFile(fileName: String): Boolean {
        val lowerName = fileName.lowercase()
        return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
                lowerName.endsWith(".png") || lowerName.endsWith(".gif") || lowerName.endsWith(".webp")
    }
    private fun extractImageFromZip(context: Context, cbzPath: String, entry: ZipEntry): Bitmap? {
        val uri = cbzPath.toUri()
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
        val inputStream = FileInputStream(pfd.fileDescriptor)
        val zipInputStream = ZipInputStream(inputStream)

        zipInputStream.use { zis ->
            var currentEntry: ZipEntry? = zis.nextEntry
            while (currentEntry != null) {
                if (currentEntry.name == entry.name) {
                    return BitmapFactory.decodeStream(zis)
                }
                currentEntry = zis.nextEntry
            }
        }

        return null
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
                    bitmap?.compress(Bitmap.CompressFormat.WEBP, 80, outputStream)
                }
            } else {
                FileOutputStream(file).use { outputStream ->
                    bitmap?.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, outputStream)
                }
            }
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            "error when loading image"
        }
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
            .setContentText("Loading CBZ file")
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