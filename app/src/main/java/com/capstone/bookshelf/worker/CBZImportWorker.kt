package com.capstone.bookshelf.worker

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
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
import com.capstone.bookshelf.util.decodeSampledBitmapFromStream
import com.capstone.bookshelf.util.saveBitmapToPrivateStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.InternalReadiumApi
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.cover
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.resource.buffered
import org.readium.r2.shared.util.toAbsoluteUrl
import org.readium.r2.shared.util.use
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser
import org.readium.r2.streamer.parser.audio.AudioParser
import org.readium.r2.streamer.parser.epub.EpubParser
import org.readium.r2.streamer.parser.image.ImageParser
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.Result as ImportResult

@SuppressLint("NewApi")
class CBZImportWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val bookRepository: BookRepository by inject()
    private val tableOfContentsRepository: TableOfContentRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val imagePathRepository: ImagePathRepository by inject()

    private val md = MessageDigest.getInstance("MD5")
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationId = System.currentTimeMillis().toInt()
    private val completionNotificationId = notificationId + 1
    private var finalBookTitle: String = ""
    private var finalBookId: String = ""

    companion object {
        const val INPUT_URI_KEY = "input_uri"
        private const val PROGRESS_CHANNEL_ID = "book_import_progress_channel"
        private const val COMPLETION_CHANNEL_ID = "book_import_completion_channel"
        private const val TAG = "CBZImportWorker"
        private const val MAX_BITMAP_DIMENSION = 2048
        private const val COMIC_INFO_FILE_NAME = "ComicInfo.xml"
    }

    init {
        createNotificationChannel(
            PROGRESS_CHANNEL_ID,
            "Book Import Progress"
        )
        createNotificationChannel(
            COMPLETION_CHANNEL_ID,
            "Book Import Completion"
        )
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val initialNotification = createProgressNotificationBuilder(
            fileName = finalBookTitle,
            message = "Starting import..."
        ).build()
        setForeground(getForegroundInfoCompat(initialNotification))

        val processingResult = processEPUB(
            onProgress = { progress, chapterName ->
                updateProgressNotification(
                    fileName = finalBookTitle,
                    message = "Processing: $chapterName",
                    progress = progress
                )
            }
        )

        val isSuccess = processingResult.isSuccess
        val failureReason = if (!isSuccess) processingResult.exceptionOrNull()?.message else null

        sendCompletionNotification(isSuccess, finalBookTitle, failureReason)
        Result.success()
    }

    private suspend fun processEPUB(
        onProgress: suspend (progress: Int?, chapterName: String) -> Unit
    ): ImportResult<String> {
        val httpClient = DefaultHttpClient()
        val assetRetriever = AssetRetriever(
            contentResolver = context.contentResolver,
            httpClient = httpClient
        )
        val publicationOpener = PublicationOpener(
            publicationParser = DefaultPublicationParser(
                context = context,
                httpClient = httpClient,
                assetRetriever = assetRetriever,
                pdfFactory = null,
                additionalParsers = listOf(
                    EpubParser(),
                    AudioParser(
                        assetSniffer = assetRetriever
                    ),
                    ImageParser(
                        assetRetriever = assetRetriever
                    ),
                )
            )
        )
        val epubUriString = inputData.getString(EPUBImportWorker.Companion.INPUT_URI_KEY) ?: return ImportResult.failure(
            IOException("Fail to get input uri")
        )
        val uri = epubUriString.toUri()
        val url = uri.toAbsoluteUrl()
        val asset = assetRetriever.retrieve(url!!).getOrNull()
        if (asset == null) {
            return ImportResult.failure(IOException("Fail to load ebook"))
        }
        val publication = publicationOpener.open(asset, allowUserInteraction = true).getOrNull()
        if (publication == null) {
            return ImportResult.failure(IOException("Fail to open ebook"))
        }
        onProgress(null, "Processing Book info...")

        val (title,authors) = readComicInfoXml(publication)
        finalBookTitle = title ?: publication.metadata.title ?: getDisplayNameFromUri(context, uri) ?: "Untitled"

        var currentChapter = ""
        val chapterMap = mutableMapOf<String, MutableList<String>>()
        val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp")
        publication.readingOrder.forEach { link ->
            val href = link.href.toString().trimStart('/')
            if (imageExtensions.any { href.endsWith(it, ignoreCase = true) }) {
                val parts = href.split("/")
                val chapterName = if (parts.size < 2) {
                    finalBookTitle
                } else {
                    parts[parts.size - 2]
                }
                if (chapterName != currentChapter) {
                    currentChapter = chapterName.replace("%20", " ")
                }
                val list = chapterMap.getOrPut(currentChapter) { mutableListOf() }
                list.add(href)
            }
        }
        val sortedChapterMap = chapterMap.entries
            .sortedBy { entry ->
                Regex("(\\d+)(?!.*\\d)").find(entry.key)?.value?.toIntOrNull() ?: Int.MAX_VALUE
            }
            .associate { it.toPair() }
        val processBookResult = processAndSaveBookInfo(
            epubUriString = epubUriString,
            publication = publication,
            tableOfContentSize = sortedChapterMap.size,
            author = authors,
        )
        if (processBookResult.isFailure) {
            return processBookResult
        }

        onProgress(null, "Processing Book content...")
        val processContentResult = processAndSaveBookContent(
            publication = publication,
            chapterMap = sortedChapterMap,
            onProgress = onProgress
        )
        if (processContentResult.isFailure) {
            return processContentResult
        }

        return ImportResult.success(finalBookTitle)
    }

    private suspend fun processAndSaveBookContent(
        publication: Publication,
        chapterMap: Map<String, List<String>>,
        onProgress: suspend (progress: Int?, chapterName: String) -> Unit
    ): ImportResult<String> {
        var overallChapterIndex = 0
        val contentList = mutableListOf<String>()
        chapterMap.forEach { (chapterName, imagePaths) ->
            val progress = ((overallChapterIndex + 1).toFloat() / chapterMap.size * 100).toInt()
            onProgress(progress, chapterName)

            imagePaths.forEach { href->
                val imagePath = saveImageFromPublication(
                    imageElementHref = href,
                    publication = publication,
                    tocIndex = overallChapterIndex,
                    paragraphIndex = imagePaths.indexOf(href)
                )
                if (!imagePath.startsWith("error")) {
                    contentList.add(imagePath)
                }
            }
            imagePathRepository.saveImagePath(finalBookId, contentList)
            saveChapterInfo(finalBookId, chapterName, overallChapterIndex, contentList)
            contentList.clear()
            overallChapterIndex++
        }
        return ImportResult.success(finalBookTitle)
    }

    @OptIn(ExperimentalReadiumApi::class)
    private suspend fun saveImageFromPublication(
        imageElementHref: String,
        publication: Publication,
        tocIndex: Int,
        paragraphIndex: Int,
    ): String {
        var imagePath = "error"
        publication.get(Url(imageElementHref)!!)?.buffered()?.use { buffering ->
            buffering.read().onSuccess { byteArray ->
                byteArray.inputStream().use {
                    val bitmap = decodeSampledBitmapFromStream(
                        it,
                        MAX_BITMAP_DIMENSION,
                        MAX_BITMAP_DIMENSION
                    )
                    if (bitmap != null) {
                        imagePath = saveBitmapToPrivateStorage(
                            context = context,
                            bitmap = bitmap,
                            compressType = Bitmap.CompressFormat.WEBP_LOSSY,
                            quality = 80,
                            filenameWithoutExtension = "image_${finalBookId}_${tocIndex}_${paragraphIndex}"
                        )
                        bitmap.recycle()
                    }
                }
            }
        }
        return imagePath
    }

    private suspend fun processAndSaveBookInfo(
        epubUriString: String,
        publication: Publication,
        tableOfContentSize: Int,
        author: String?,
    ): ImportResult<String> {
        finalBookId = BigInteger(1, md.digest(finalBookTitle.toByteArray())).toString(16)
            .padStart(32, '0')
        if (bookRepository.isBookExist(finalBookTitle)) {
            return ImportResult.failure(IOException("Book already imported"))
        }
        var coverImagePath = ""
        val coverImageBitmap = publication.cover()
        coverImagePath = coverImageBitmap?.let { bitmap ->
            saveBitmapToPrivateStorage(
                context = context,
                bitmap = bitmap,
                compressType = Bitmap.CompressFormat.WEBP_LOSSY,
                quality = 80,
                filenameWithoutExtension = "cover_$finalBookId"
            )
        } ?: "error"
        val authors = author?.split(",") ?: publication.metadata.authors.map { it.name.toString() }
        bookRepository.insertBook(
            BookEntity(
                bookId = finalBookId,
                title = finalBookTitle,
                coverImagePath = coverImagePath,
                authors = authors,
                description = publication.metadata.description,
                totalChapter = tableOfContentSize,
                currentChapter = 0,
                currentParagraph = 0,
                isFavorite = false,
                storagePath = epubUriString,
                isEditable = false,
                fileType = "cbz"
            )
        ).run {
            bookRepository.updateRecentRead(finalBookId)
        }
        imagePathRepository.saveImagePath(finalBookId, listOf(coverImagePath))
        return ImportResult.success(finalBookTitle)
    }

    private suspend fun saveBookInfo(
        bookID: String,
        title: String,
        authors: List<String>,
        coverImagePath: String,
        totalChapters: Int,
        cacheFilePath: String
    ): Long {
        val bookEntity = BookEntity(
            bookId = bookID,
            title = title,
            coverImagePath = coverImagePath,
            authors = authors,
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

    private fun createNotificationChannel(channelId: String, channelName: String) {
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
        return NotificationCompat.Builder(context, PROGRESS_CHANNEL_ID)
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
            else -> failureReason
        }

        val text = when {
            isSuccess -> "'$defaultTitle' added to your library."
            userFriendlyReason != null -> "Failed to import '$defaultTitle': $userFriendlyReason"
            else -> "Import failed for '$defaultTitle'."
        }
        val builder = NotificationCompat.Builder(context, COMPLETION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        notificationManager.notify(completionNotificationId, builder.build())
        notificationManager.cancel(notificationId)
    }

    /** Provides ForegroundInfo, handling platform differences. */
    private fun getForegroundInfoCompat(notification: Notification): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }


    @OptIn(InternalReadiumApi::class)
    suspend fun readComicInfoXml(publication: Publication?):Pair<String?,String?> {
        val comicInfoHref = "ComicInfo.xml"
        val url = Url(comicInfoHref)

        val resource = publication?.container?.get(url!!)
        if (resource == null) {
            return Pair(null,null)
        }

        val result = resource.read()
        result.onSuccess { byteArray ->
            val parserFactory = XmlPullParserFactory.newInstance()
            parserFactory.isNamespaceAware = true
            val parser = parserFactory.newPullParser()

            parser.setInput(ByteArrayInputStream(byteArray), null)

            var eventType = parser.eventType
            var series: String? = null
            var writer: String? = null
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "Series","series" -> {
                                series = parser.nextText()?.trim()
                            }

                            "writer","Writer" -> {
                                writer = parser.nextText()?.trim()
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
            return Pair(series,writer)
        }.onFailure { error ->
            return Pair(null,null)
        }
        return Pair(null,null)
    }
}