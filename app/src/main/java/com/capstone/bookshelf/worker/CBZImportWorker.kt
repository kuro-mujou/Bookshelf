package com.capstone.bookshelf.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.OpenableColumns
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.core.uri.Uri
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
import com.capstone.bookshelf.util.NaturalOrderComparator
import com.capstone.bookshelf.util.calculateInSampleSize
import com.capstone.bookshelf.util.saveBitmapToPrivateStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.util.zip.ZipFile

class CBZImportWorker(
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

    companion object {
        const val INPUT_URI_KEY = "input_uri"
        const val ORIGINAL_FILENAME_KEY = "original_filename"
        private const val TAG = "CBZImportWorker"

        private const val PROGRESS_CHANNEL_ID = "book_import_progress_channel"
        private const val COMPLETION_CHANNEL_ID = "book_import_completion_channel"

        private const val MAX_BITMAP_DIMENSION = 2048
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif", "avif", "bmp")
    }

    init {
        createNotificationChannelIfNeeded(
            PROGRESS_CHANNEL_ID,
            "Book Import Progress"
        )
        createNotificationChannelIfNeeded(
            COMPLETION_CHANNEL_ID,
            "Book Import Completion"
        )
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val cbzUriString = inputData.getString(INPUT_URI_KEY)
        val originalFileName = inputData.getString(ORIGINAL_FILENAME_KEY) ?: getDisplayNameFromUri(
            appContext,
            cbzUriString?.toUri()
        ) ?: "Unknown CBZ"

        if (cbzUriString == null) {
            return@withContext Result.failure()
        }
        val cbzUri = cbzUriString.toUri()
        val initialNotification = createProgressNotificationBuilder(
            originalFileName, "Starting import..."
        ).build()
        try {
            setForeground(getForegroundInfoCompat(initialNotification))
        } catch (e: Exception) {
            // Non-fatal error if setForeground fails (e.g., app in background on older APIs)
        }
        val processingResult = processCbzAndSaveData(
            appContext,
            cbzUri,
            originalFileName,
            onProgress = { currentChapter, totalChapters, chapterName ->
                val progressPercent =
                    ((currentChapter.toFloat() / totalChapters.toFloat()) * 100).toInt()
                updateProgressNotification(
                    originalFileName,
                    "Processing: $chapterName ($currentChapter/$totalChapters)",
                    progressPercent
                )
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
     * The core processing logic. Copies to cache, extracts info, saves images, updates DB.
     * Returns Result.success(bookTitle) or Result.failure(exception).
     */
    private suspend fun processCbzAndSaveData(
        context: Context,
        zipFileUri: Uri,
        originalDisplayName: String,
        onProgress: suspend (currentChapter: Int, totalChapters: Int, chapterName: String) -> Unit
    ): kotlin.Result<String> {
        var tempZipFile: File? = null
        val actualFileName = originalDisplayName
        val bookTitle = actualFileName.substringBeforeLast('.')
        var bookId: String? = null

        try {
            bookId = BigInteger(1, md.digest(actualFileName.toByteArray())).toString(16)
                .padStart(32, '0')
            if (bookRepository.isBookExist(bookTitle)) {
                return kotlin.Result.failure(IOException("Book already imported"))
            }
            updateProgressNotification(originalDisplayName, "Copying file...", null)
            tempZipFile = File.createTempFile("cbz_import_", ".zip", context.cacheDir)
            context.contentResolver.openInputStream(zipFileUri)?.use { inputStream ->
                FileOutputStream(tempZipFile).use { outputStream ->
                    inputStream.copyTo(outputStream, 8192) // 8KB buffer
                }
            } ?: run {
                tempZipFile?.delete() // Clean up temp file
                return kotlin.Result.failure(IOException("Failed to open InputStream"))
            }
            updateProgressNotification(originalDisplayName, "Analyzing archive...", null)
            val groupedImageEntries = mutableMapOf<String, MutableList<String>>()
            val allSecondLevelDirs = mutableSetOf<String>()
            val naturalComparator = NaturalOrderComparator()
            ZipFile(tempZipFile).use { zipFile ->
                val entries = zipFile.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val entryName = entry.name
                    if (entry.isDirectory || entryName.startsWith("__MACOSX/") || entryName.contains(
                            "/.DS_Store"
                        )
                    ) continue
                    val pathSegments = entryName.split('/').filter { it.isNotEmpty() }
                    if (pathSegments.size >= 2) {
                        val secondLevelName = pathSegments[1]
                        allSecondLevelDirs.add(secondLevelName)

                        val fileName = pathSegments.last()
                        val extension = fileName.substringAfterLast('.', "").lowercase()
                        if (extension in IMAGE_EXTENSIONS) {
                            val imageList =
                                groupedImageEntries.getOrPut(secondLevelName) { mutableListOf() }
                            imageList.add(entryName)
                        }
                    }
                }
            }

            val finalDirNames = allSecondLevelDirs.filter { groupedImageEntries.containsKey(it) }
            val sortedDirNames = finalDirNames.toList().sortedWith(naturalComparator)
            val totalChapters = sortedDirNames.size

            if (totalChapters == 0) {
                return kotlin.Result.failure(IOException("No valid image entries found in file"))
            }
            updateProgressNotification(originalDisplayName, "Extracting cover image...", null)
            var coverImagePath = "no_cover_extracted"
            val firstChapterName = sortedDirNames.first()
            val imagePathsInFirstChapter = groupedImageEntries[firstChapterName]
                ?.sortedWith(naturalComparator)
            if (!imagePathsInFirstChapter.isNullOrEmpty()) {
                val firstImageEntryPath = imagePathsInFirstChapter.first()
                try {
                    ZipFile(tempZipFile).use { coverZipFile ->
                        val coverEntry = coverZipFile.getEntry(firstImageEntryPath)
                        if (coverEntry != null) {
                            coverZipFile.getInputStream(coverEntry).use { imageStream ->
                                val bitmap = decodeSampledBitmapFromStream(imageStream)
                                if (bitmap != null) {
                                    val coverFilename = "${bookId}_cover"
                                    coverImagePath =
                                        saveBitmapToPrivateStorage(context, bitmap, coverFilename)
                                    bitmap.recycle()
                                } else {
                                    coverImagePath = "decode_error"
                                }
                            }
                        } else {
                            coverImagePath = "entry_not_found"
                        }
                    }
                } catch (e: Exception) {
                    coverImagePath = "extraction_error"
                }
            } else {
                coverImagePath = "no_images_in_first_chapter"
            }
            val finalCoverPathForDb =
                if (coverImagePath.startsWith("error_") || coverImagePath.startsWith("no_")) {
                    null
                } else {
                    coverImagePath
                }
            saveBookInfo(
                bookID = bookId,
                title = bookTitle,
                coverImagePath = finalCoverPathForDb!!,
                totalChapters = totalChapters,
                cacheFilePath = tempZipFile.absolutePath
            ).run {
                bookRepository.updateRecentRead(bookId)
            }
            var chapterIndex = 0
            ZipFile(tempZipFile).use { chapterZipFile ->
                for (chapterName in sortedDirNames) {
                    chapterIndex++
                    onProgress(chapterIndex, totalChapters, chapterName)

                    val imageEntryPaths = groupedImageEntries[chapterName] ?: continue
                    val sortedImageEntryPaths = imageEntryPaths.sortedWith(naturalComparator)
                    val savedImagePathsInChapter = mutableListOf<String>()

                    for (imageEntryPath in sortedImageEntryPaths) {
                        val entry = chapterZipFile.getEntry(imageEntryPath) ?: continue
                        val originalImageName = entry.name.substringAfterLast('/')

                        try {
                            chapterZipFile.getInputStream(entry).use { imageStream ->
                                val bitmap = decodeSampledBitmapFromStream(imageStream)
                                if (bitmap != null) {
                                    val safeChapterName =
                                        chapterName.replace(Regex("[^A-Za-z0-9_-]"), "_")
                                    val safeImageName = originalImageName.substringBeforeLast('.')
                                        .replace(Regex("[^A-Za-z0-9_-]"), "_")
                                    val chapterImageFilename =
                                        "${bookId}_${safeChapterName}_${safeImageName}"
                                    val savedPath = saveBitmapToPrivateStorage(
                                        context,
                                        bitmap,
                                        chapterImageFilename
                                    )
                                    if (!savedPath.startsWith("error")) {
                                        savedImagePathsInChapter.add(savedPath)
                                    }
                                    bitmap.recycle()
                                }
                            }
                        } catch (oom: OutOfMemoryError) {
                        } catch (e: Exception) {
                        }
                    }
                    if (savedImagePathsInChapter.isNotEmpty()) {
                        saveChapterInfo(
                            bookId,
                            chapterName,
                            chapterIndex - 1,
                            savedImagePathsInChapter
                        )
                    }
                }
            }
            return kotlin.Result.success(bookTitle)

        } catch (e: Exception) {
            return kotlin.Result.failure(e)
        } finally {
            if (tempZipFile != null && tempZipFile.exists()) {
                tempZipFile.delete()
            }
        }
    }

    private suspend fun saveBookInfo(
        bookID: String,
        title: String,
        coverImagePath: String,
        totalChapters: Int,
        cacheFilePath: String
    ): Long {
        val bookEntity = BookEntity(
            bookId = bookID,
            title = title,
            coverImagePath = coverImagePath,
            authors = listOf("Unknown"),
            categories = emptyList(),
            description = null,
            totalChapter = totalChapters,
            currentChapter = 0,
            currentParagraph = 0,
            storagePath = cacheFilePath,
            isEditable = false,
            fileType = "cbz"
        )
        return bookRepository.insertBook(bookEntity)
    }

    private suspend fun saveChapterInfo(
        bookId: String,
        chapterName: String,
        chapterIndex: Int,
        savedImagePaths: List<String>
    ) {
        val tocEntity = TableOfContentEntity(
            bookId = bookId,
            title = chapterName,
            index = chapterIndex
        )
        tableOfContentsRepository.saveTableOfContent(tocEntity)
        val chapterEntity = ChapterContentEntity(
            tocId = chapterIndex,
            bookId = bookId,
            chapterTitle = chapterName,
            content = savedImagePaths,
        )
        chapterRepository.saveChapterContent(chapterEntity)
    }

    private fun createNotificationChannelIfNeeded(channelId: String, channelName: String) {
        if (notificationManager.getNotificationChannel(channelId) == null) {
            val importance =
                if (channelId == PROGRESS_CHANNEL_ID) NotificationManager.IMPORTANCE_LOW else NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                if (channelId == PROGRESS_CHANNEL_ID) {
                    setSound(null, null)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /** Creates the base builder for progress notifications. */
    private fun createProgressNotificationBuilder(
        fileName: String,
        message: String
    ): NotificationCompat.Builder {
        val displayFileName = fileName.substringBeforeLast(".")
        return NotificationCompat.Builder(appContext, PROGRESS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Importing: ${displayFileName.take(40)}${if (displayFileName.length > 40) "..." else ""}")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
    }

    /** Updates the ongoing progress notification. */
    private suspend fun updateProgressNotification(
        fileName: String,
        message: String,
        progress: Int?
    ) {
        val builder = createProgressNotificationBuilder(fileName, message)
        if (progress != null) {
            builder.setProgress(100, progress.coerceIn(0, 100), false) // Determinate
        } else {
            builder.setProgress(0, 0, true) // Indeterminate
        }
        try {
            setForeground(getForegroundInfoCompat(builder.build()))
        } catch (e: Exception) {
            notificationManager.notify(notificationId, builder.build())
        }
    }


    /** Sends the final completion notification. */
    private fun sendCompletionNotification(
        isSuccess: Boolean,
        bookTitle: String?,
        failureReason: String? = null
    ) {
        val title = if (isSuccess) "Import Successful" else "Import Failed"
        val defaultTitle = bookTitle ?: "File"
        val userFriendlyReason = when {
            failureReason == null -> null
            failureReason.contains("Book already imported") -> "This book is already in your library."
            failureReason.contains("No valid image entries found") -> "No images could be found in this file."
            failureReason.contains("Failed to open InputStream") -> "Could not read the selected file."
            failureReason.contains("OutOfMemoryError") -> "Ran out of memory processing images. Try importing smaller files."
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

    /** Decodes bitmap with sampling to prevent OOM errors. */
    private fun decodeSampledBitmapFromStream(
        stream: InputStream,
        reqWidth: Int = MAX_BITMAP_DIMENSION,
        reqHeight: Int = MAX_BITMAP_DIMENSION
    ): Bitmap? {
        try {
            if (!stream.markSupported()) {
                return BitmapFactory.decodeStream(stream)
            }
            stream.mark(1024 * 1024)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(stream, null, options)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            stream.reset()
            return BitmapFactory.decodeStream(stream, null, options)
        } catch (e: IOException) {
            return null
        } catch (oom: OutOfMemoryError) {
            return null
        } catch (e: Exception) {
            return null
        }
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
}