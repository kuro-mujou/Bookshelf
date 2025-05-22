package com.capstone.bookshelf.worker

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
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.capstone.bookshelf.R
import com.capstone.bookshelf.data.database.entity.BookEntity
import com.capstone.bookshelf.data.database.entity.ChapterContentEntity
import com.capstone.bookshelf.data.database.entity.TableOfContentEntity
import com.capstone.bookshelf.data.network.mapHttpStatusToDataError
import com.capstone.bookshelf.domain.error.DataError
import com.capstone.bookshelf.domain.error.MyResult
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.domain.repository.ChapterRepository
import com.capstone.bookshelf.domain.repository.ImagePathRepository
import com.capstone.bookshelf.domain.repository.TableOfContentRepository
import com.capstone.bookshelf.util.NaturalOrderComparator
import com.capstone.bookshelf.util.decodeSampledBitmapFromStream
import com.capstone.bookshelf.util.saveBitmapToPrivateStorage
import io.ktor.client.HttpClient
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.domain.TOCReference
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.Result as ImportResult

class DriveEPUBImportWorker(
    private val appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

    private val httpClient: HttpClient by inject()
    private val bookRepository: BookRepository by inject()
    private val tableOfContentsRepository: TableOfContentRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val imagePathRepository: ImagePathRepository by inject()

    private val md = MessageDigest.getInstance("MD5")
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationId = System.currentTimeMillis().toInt()
    private val completionNotificationId = notificationId + 1
    private var finalBookTitle = "EPUB"

    companion object {
        const val INPUT_DRIVE_LINK_KEY = "input_drive_link"
        const val ORIGINAL_FILENAME_KEY = "original_filename"
        private const val TAG = "EpubImportWorker"

        private const val PROGRESS_CHANNEL_ID = "book_import_progress_channel"
        private const val COMPLETION_CHANNEL_ID = "book_import_completion_channel"

        private const val MAX_BITMAP_DIMENSION = 2048
    }

    init {
        createNotificationChannel(PROGRESS_CHANNEL_ID, "Book Import Progress")
        createNotificationChannel(COMPLETION_CHANNEL_ID, "Book Import Completion")
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val driveLink = inputData.getString(INPUT_DRIVE_LINK_KEY)

        val initialNotification = createProgressNotificationBuilder(
            fileName = "EPUB",
            message = "Starting import..."
        ).build()
        setForeground(getForegroundInfoCompat(initialNotification))

        val inputStreamResult: MyResult<InputStream, DataError.Remote> =
            if (driveLink == null) {
                MyResult.Error(DataError.Remote.NOT_FOUND)
            } else {
                val fileIdRegex = "[-\\w]{25,}".toRegex()
                val fileId = fileIdRegex.find(driveLink)?.value
                if (fileId == null) {
                    MyResult.Error(DataError.Remote.NOT_FOUND)
                } else {
                    val directUrl = "https://drive.google.com/uc?export=download&id=$fileId"
                    safeDownloadStream(httpClient, directUrl)
                }
            }

        val processingResult: ImportResult<String> = when (inputStreamResult) {
            is MyResult.Success -> {
                processEpubStream(
                    context = appContext,
                    inputStream = inputStreamResult.data,
                    filePath = driveLink.toString(),
                    onProgress = { progress, message ->
                        updateProgressNotification(finalBookTitle, message, progress)
                    }
                )
            }

            is MyResult.Error -> {
                val errorReason = mapDataErrorToUserMessage(inputStreamResult.error)
                ImportResult.failure(IOException("Failed to get input stream: $errorReason"))
            }
        }

        val isSuccess = processingResult.isSuccess
        val failureReason = if (!isSuccess) processingResult.exceptionOrNull()?.message else null

        sendCompletionNotification(isSuccess, finalBookTitle, failureReason)
        return@withContext if (isSuccess) Result.success() else Result.failure()
    }

    suspend fun safeDownloadStream(
        httpClient: HttpClient,
        initialUrl: String
    ): MyResult<InputStream, DataError.Remote> {
        try {
            val response1: HttpResponse = httpClient.get(initialUrl)
            if (!response1.status.isSuccess()) {
                return MyResult.Error(mapHttpStatusToDataError(response1.status))
            }
            val contentType1 = response1.contentType()?.withoutParameters()
            if (contentType1 == ContentType.Text.Html) {
                val htmlBody = response1.bodyAsText()
                val secondTryResult =
                    parseHtmlAndAttemptSecondDownload(httpClient, htmlBody, initialUrl)
                return secondTryResult ?: MyResult.Error(DataError.Remote.HTML_PARSING_FAILED)

            } else {
                val channel: ByteReadChannel = response1.bodyAsChannel()
                val inputStream = withContext(Dispatchers.IO) {
                    channel.toInputStream()
                }
                return MyResult.Success(inputStream)
            }

        } catch (e: SocketTimeoutException) {
            return MyResult.Error(DataError.Remote.REQUEST_TIMEOUT)
        } catch (e: UnresolvedAddressException) {
            return MyResult.Error(DataError.Remote.NO_INTERNET)
        } catch (e: Exception) {
            return MyResult.Error(DataError.Remote.UNKNOWN)
        }
    }

    private suspend fun parseHtmlAndAttemptSecondDownload(
        httpClient: HttpClient,
        htmlBody: String,
        originalUrl: String
    ): MyResult<InputStream, DataError.Remote>? {
        try {
            val document = Jsoup.parse(htmlBody)
            val form = document.selectFirst("#download-form")
            if (form == null) {
                return null
            }
            val actionUrl = form.attr("abs:action")
            if (actionUrl.isBlank()) {
                return null
            }

            val formParams = mutableMapOf<String, String>()
            val inputs = form.select("input[type=hidden]")
            for (input in inputs) {
                val name = input.attr("name")
                val value = input.attr("value")
                if (name.isNotBlank()) {
                    formParams[name] = value
                }
            }

            val response2: HttpResponse = httpClient.get(actionUrl) {
                formParams.forEach { (key, value) ->
                    parameter(key, value)
                }
            }

            if (!response2.status.isSuccess()) {
                return MyResult.Error(mapHttpStatusToDataError(response2.status))
            }

            val contentType2 = response2.contentType()?.withoutParameters()
            if (contentType2 == ContentType.Text.Html) {
                return MyResult.Error(DataError.Remote.DOWNLOAD_CONFIRMATION_FAILED)
            }
            val channel: ByteReadChannel = response2.bodyAsChannel()
            val inputStream = withContext(Dispatchers.IO) {
                channel.toInputStream()
            }
            return MyResult.Success(inputStream)

        } catch (e: SocketTimeoutException) {
            return MyResult.Error(DataError.Remote.REQUEST_TIMEOUT)
        } catch (e: UnresolvedAddressException) {
            return MyResult.Error(DataError.Remote.NO_INTERNET)
        } catch (e: IOException) {
            return MyResult.Error(DataError.Remote.HTML_PARSING_FAILED)
        } catch (e: Exception) {
            return MyResult.Error(DataError.Remote.UNKNOWN)
        }
    }

    private fun mapDataErrorToUserMessage(error: DataError.Remote): String {
        return when (error) {
            DataError.Remote.REQUEST_TIMEOUT -> "Network request timed out."
            DataError.Remote.NO_INTERNET -> "No internet connection or cannot reach server."
            DataError.Remote.SERVER -> "Server error during download."
            DataError.Remote.TOO_MANY_REQUESTS -> "Too many requests, please try again later."
            DataError.Remote.UNAUTHORIZED -> "Access denied to the file."
            DataError.Remote.NOT_FOUND -> "File not found at the source."
            DataError.Remote.UNKNOWN -> "Unknown error occurred during download."
            DataError.Remote.UNEXPECTED_CONTENT_TYPE_HTML -> "Download failed (possibly virus scan page)."
            DataError.Remote.SERIALIZATION -> "Serialization error occurred."
            DataError.Remote.HTML_PARSING_FAILED -> "HTML parsing failed."
            DataError.Remote.DOWNLOAD_CONFIRMATION_FAILED -> "Download confirmation failed."
        }
    }

    /**
     * Processes EPUB stream by copying to cache, parsing, extracting, and saving data.
     * Returns Result.success(bookTitle) or Result.failure(exception).
     */
    private suspend fun processEpubStream(
        context: Context,
        inputStream: InputStream,
        filePath: String,
        onProgress: suspend (progress: Int?, message: String) -> Unit
    ): ImportResult<String> {
        var tempEpubFile: File? = null
        var book: Book? = null
        var bookId: String? = null
        onProgress(null, "Copying file...")
        try {
            tempEpubFile = File.createTempFile("epub_import_", ".epub", context.cacheDir)
            inputStream.use { input ->
                FileOutputStream(tempEpubFile).use { outputStream ->
                    input.copyTo(outputStream, 8192)
                }
            }
            onProgress(null, "Parsing EPUB structure...")
            try {
                FileInputStream(tempEpubFile).use { fis ->
                    book = EpubReader().readEpub(fis)
                }
            } catch (e: Exception) {
                throw IOException("Could not parse EPUB file.", e)
            }

            if (book == null) throw IOException("EpubReader returned a null book object.")
            finalBookTitle = book.title
            bookId = BigInteger(1, md.digest(finalBookTitle.toByteArray()))
                .toString(16).padStart(32, '0')
            if (bookRepository.isBookExist(finalBookTitle)) {
                return ImportResult.failure(IOException("Book already imported"))
            }
            onProgress(null, "Extracting cover image...")
            var coverImagePath: String? = null
            try {
                book.coverImage?.inputStream?.use { coverStream ->
                    val bitmap = decodeSampledBitmapFromStream(
                        coverStream,
                        MAX_BITMAP_DIMENSION,
                        MAX_BITMAP_DIMENSION
                    )
                    if (bitmap != null) {
                        val coverFilename = "cover_${bookId}"
                        coverImagePath = saveBitmapToPrivateStorage(
                            context = context,
                            bitmap = bitmap,
                            compressType = Bitmap.CompressFormat.JPEG,
                            quality = 80,
                            filenameWithoutExtension = coverFilename
                        )
                        bitmap.recycle()
                    } else {
                        coverImagePath = "error_decode_cover"
                    }
                }
            } catch (e: Exception) {
                coverImagePath = "error_processing_cover"
            }
            val finalCoverPathForDb = coverImagePath?.takeIf { !it.startsWith("error_") } ?: "error"
            onProgress(null, "Processing table of contents...")
            val flattenedToc = flattenTocReferences(book.tableOfContents?.tocReferences)
            val totalChapters = flattenedToc.size
            if (totalChapters == 0) {
                return ImportResult.failure(IOException("EPUB Table of Contents is empty or missing. You might opened Epub3 format, please use file picker method"))
            }
            onProgress(null, "Saving book information...")
            saveBookInfo(
                bookID = bookId,
                title = finalBookTitle,
                coverImagePath = finalCoverPathForDb,
                authors = book.metadata.authors,
                description = book.metadata.descriptions,
                totalChapters = totalChapters,
                storagePath = filePath
            )
            imagePathRepository.saveImagePath(bookId, listOf(finalCoverPathForDb))
            bookRepository.updateRecentRead(bookId)
            processAndSaveChapters(bookId, book, flattenedToc, context, onProgress)
            return ImportResult.success(finalBookTitle)
        } catch (e: Exception) {
            try {
                inputStream.close()
            } catch (closeEx: Exception) {
            }
            return ImportResult.failure(e)
        } finally {
            if (tempEpubFile != null && tempEpubFile.exists()) {
                tempEpubFile.delete()
            }
        }
    }

    private fun flattenTocReferences(tocReferences: List<TOCReference>?): List<TOCReference> {
        if (tocReferences == null) return emptyList()
        val flattened = mutableListOf<TOCReference>()
        for (ref in tocReferences) {
            flattened.add(ref)
            flattened.addAll(flattenTocReferences(ref.children))
        }
        return flattened
    }

    private fun normalizeAuthorNames(authors: List<Author>?): List<String> {
        return authors?.mapNotNull { author ->
            val first = author.firstname?.trim()
            val last = author.lastname?.trim()
            when {
                !first.isNullOrBlank() && !last.isNullOrBlank() -> "$first $last"
                !first.isNullOrBlank() -> first
                !last.isNullOrBlank() -> last
                else -> null
            }
        } ?: listOf("Unknown Author")
    }

    /** Processes chapters, handling sub-chapters and the initial segment correctly */
    private suspend fun processAndSaveChapters(
        bookId: String,
        book: Book,
        flattenedToc: List<TOCReference>,
        context: Context,
        onProgress: suspend (progress: Int?, message: String) -> Unit
    ) {
        if (flattenedToc.isEmpty()) return
        val tocGroupedByResource = flattenedToc
            .filter { it.resource?.href != null }
            .groupBy { it.resource.href.substringBefore('#') }
            .filterKeys { it.isNotBlank() }

        var overallChapterIndex = 0
        val totalTocEntries = flattenedToc.size
        val naturalComparator = NaturalOrderComparator()

        for ((resourceHref, tocEntriesForResource) in tocGroupedByResource) {
            val resource = tocEntriesForResource.first().resource ?: continue

            var chapterHtml: String? = null
            var document: Document? = null
            try {
                resource.inputStream.use { stream ->
                    chapterHtml = stream.bufferedReader().readText()
                }
                document = Jsoup.parse(chapterHtml!!, resourceHref)
            } catch (e: Exception) {

            }

            val needsSplitting = tocEntriesForResource.any { it.fragmentId != null }

            if (document == null) {
                tocEntriesForResource.forEach {
                    val chapterTitle = it.title?.takeIf { t -> t.isNotBlank() }
                        ?: "Chapter ${overallChapterIndex + 1}"
                    onProgress(
                        ((overallChapterIndex + 1).toFloat() / totalTocEntries * 100).toInt(),
                        "Error loading ${chapterTitle.take(30)}..."
                    )
                    saveTableOfContentEntry(bookId, chapterTitle, overallChapterIndex)
                    saveErrorChapterContent(
                        bookId,
                        chapterTitle,
                        overallChapterIndex,
                        "[ERR: Load/Parse]"
                    )
                    overallChapterIndex++
                }
                continue
            }

            if (!needsSplitting) {
                val representativeTocRef = tocEntriesForResource.first()
                val chapterTitle = representativeTocRef.title?.takeIf { it.isNotBlank() }
                    ?: "Chapter ${overallChapterIndex + 1}"
                val progressPercent =
                    ((overallChapterIndex + 1).toFloat() / totalTocEntries * 100).toInt()
                onProgress(
                    progressPercent,
                    "Processing ${chapterTitle.take(30)}... (Full Resource)"
                )
                saveTableOfContentEntry(bookId, chapterTitle, overallChapterIndex)
                var parsedContent: Pair<List<String>, List<String>>? = null
                var segmentError: String? = null
                try {
                    parsedContent = parseChapterHtmlSegment(
                        document = document,
                        startAnchorId = null,
                        endAnchorId = null,
                        book = book,
                        context = context,
                        bookId = bookId,
                        chapterIndex = overallChapterIndex
                    )
                } catch (e: Exception) {
                    segmentError = "[ERR: Parse Full]"
                }
                val contentToSave = parsedContent?.first ?: (if (segmentError != null) listOf(
                    segmentError
                ) else emptyList())
                val imagePathsFound = parsedContent?.second ?: emptyList()
                if (contentToSave.isNotEmpty()) {
                    saveChapterContent(bookId, chapterTitle, overallChapterIndex, contentToSave)
                } else {
                    saveEmptyChapterContent(bookId, chapterTitle, overallChapterIndex)
                }
                val validImagePaths = imagePathsFound.filter { !it.startsWith("error_") }
                if (validImagePaths.isNotEmpty()) {
                    imagePathRepository.saveImagePath(bookId, validImagePaths)
                }
                overallChapterIndex++
                for (extraIndex in 1 until tocEntriesForResource.size) {
                    val extraTocRef = tocEntriesForResource[extraIndex]
                    val extraTitle = extraTocRef.title?.takeIf { it.isNotBlank() }
                        ?: "Chapter ${overallChapterIndex + 1}"
                    saveTableOfContentEntry(
                        bookId,
                        extraTitle,
                        overallChapterIndex
                    )
                    saveEmptyChapterContent(bookId, extraTitle, overallChapterIndex)
                    overallChapterIndex++
                }

            } else {
                val sortedTocEntries = tocEntriesForResource.sortedWith(
                    compareBy(nullsFirst<String>()) { ref: TOCReference ->
                        ref.fragmentId?.takeIf { it.isNotBlank() }
                    }.thenComparator { ref1: TOCReference, ref2: TOCReference ->
                        val frag1 = ref1.fragmentId?.takeIf { it.isNotBlank() }
                        val frag2 = ref2.fragmentId?.takeIf { it.isNotBlank() }
                        when {
                            frag1 == null && frag2 == null -> 0
                            frag1 == null -> -1
                            frag2 == null -> 1
                            else -> naturalComparator.compare(
                                frag1,
                                frag2
                            )
                        }
                    }
                )
                for (i in sortedTocEntries.indices) {
                    val currentTocRef = sortedTocEntries[i]
                    val chapterTitle = currentTocRef.title?.takeIf { it.isNotBlank() }
                        ?: "Chapter ${overallChapterIndex + 1}"
                    val progressPercent =
                        ((overallChapterIndex + 1).toFloat() / totalTocEntries * 100).toInt()
                    onProgress(
                        progressPercent,
                        "Processing $chapterTitle"
                    )
                    saveTableOfContentEntry(
                        bookId = bookId,
                        title = chapterTitle,
                        index = overallChapterIndex
                    )
                    var startAnchorId: String?
                    var endAnchorId: String?
                    if (i == 0) {
                        startAnchorId = null
                        endAnchorId = sortedTocEntries.getOrNull(1)?.fragmentId
                            ?.takeIf { it.isNotBlank() }
                    } else {
                        startAnchorId = currentTocRef.fragmentId?.takeIf { it.isNotBlank() }
                        endAnchorId = sortedTocEntries.getOrNull(i + 1)?.fragmentId
                            ?.takeIf { it.isNotBlank() }
                    }
                    var parsedContent: Pair<List<String>, List<String>>? = null
                    try {
                        parsedContent = parseChapterHtmlSegment(
                            document = document,
                            startAnchorId = startAnchorId,
                            endAnchorId = endAnchorId,
                            book = book,
                            context = context,
                            bookId = bookId,
                            chapterIndex = overallChapterIndex
                        )
                    } catch (e: Exception) {

                    }
                    val contentToSave = parsedContent?.first ?: emptyList()
                    val imagePathsFound = parsedContent?.second ?: emptyList()
                    if (contentToSave.isNotEmpty()) {
                        saveChapterContent(bookId, chapterTitle, overallChapterIndex, contentToSave)
                    } else {
                        saveEmptyChapterContent(bookId, chapterTitle, overallChapterIndex)
                    }
                    val validImagePaths = imagePathsFound.filter { !it.startsWith("error_") }
                    if (validImagePaths.isNotEmpty()) {
                        imagePathRepository.saveImagePath(bookId, validImagePaths)
                    }
                    overallChapterIndex++
                }
            }
        }
    }

    /** Parses HTML segment between anchors, includes content within start anchor */
    private fun parseChapterHtmlSegment(
        document: Document,
        startAnchorId: String?,
        endAnchorId: String?,
        book: Book,
        context: Context,
        bookId: String,
        chapterIndex: Int
    ): Pair<List<String>, List<String>> {
        val contentList = mutableListOf<String>()
        val imagePaths = mutableListOf<String>()
        var currentParagraph = StringBuilder()
        var imageCounter = 0
        val startElement = if (!startAnchorId.isNullOrBlank()) {
            try {
                document.selectFirst("[id=$startAnchorId], [name=$startAnchorId]")
            } catch (e: Exception) {
                null
            }
        } else null
        val endElement = if (!endAnchorId.isNullOrBlank()) {
            try {
                document.selectFirst("[id=$endAnchorId], [name=$endAnchorId]")
            } catch (e: Exception) {
                null
            }
        } else null
        var processingActive = startAnchorId.isNullOrBlank() || startElement == null
        var passedStartAnchor = processingActive
        var hitEndAnchor = false
        document.body().traverse(object : NodeVisitor {
            override fun head(node: Node, depth: Int) {
                if (hitEndAnchor) return
                if (endElement != null && node == endElement) {
                    hitEndAnchor = true
                    flushParagraphWithFormatting(currentParagraph, contentList)
                    return
                }
                var isStartAnchorNode = false
                if (!passedStartAnchor && startElement != null && node == startElement) {
                    passedStartAnchor = true
                    processingActive = true
                    isStartAnchorNode = true
                }
                if (!processingActive || !passedStartAnchor) {
                    return
                }
                when (node) {
                    is TextNode -> {
                        val text = node.text()
                        var textToAppend = text.replace(Regex("\\s+"), " ")
                        if (textToAppend.isNotBlank()) {
                            currentParagraph.append(textToAppend)
                        }
                    }

                    is Element -> {
                        val tagName = node.tagName().lowercase()
                        when (tagName) {
                            "p", "div", "ul", "ol", "li", "table", "blockquote", "hr", "h1", "h2", "h3", "h4", "h5", "h6" -> {
                                flushParagraphWithFormatting(currentParagraph, contentList)
                                if (tagName.startsWith("h"))
                                    currentParagraph.append("<$tagName>")
                            }

                            "br" -> {
                                val parentNode = node.parentNode()
                                if (parentNode is Element && parentNode.tagName().lowercase()
                                        .startsWith("h")
                                ) {
                                    if (currentParagraph.isNotEmpty() && !currentParagraph.endsWith(
                                            " "
                                        )
                                    )
                                        currentParagraph.append(" ")
                                } else {
                                    flushParagraphWithFormatting(currentParagraph, contentList)
                                }
                            }

                            "b", "strong", "i", "em", "u" -> {
                                currentParagraph.append("<$tagName>")
                            }

                            "td", "th" -> {
                                if (currentParagraph.isNotEmpty() && !currentParagraph.endsWith(' ')) {
                                    currentParagraph.append(" ")
                                }
                            }

                            "img", "image" -> {
                                flushParagraphWithFormatting(currentParagraph, contentList)
                                val srcAttr = when (tagName) {
                                    "img" -> node.attr("src")
                                    "image" -> node.attr("xlink:href").ifEmpty { node.attr("href") }
                                    else -> ""
                                }
                                if (srcAttr.isNotBlank()) {
                                    val imageResource = getImageResourceFromBook(srcAttr, book)
                                    if (imageResource != null) {
                                        var savedImagePath: String? = null
                                        try {
                                            imageResource.inputStream.use { stream ->
                                                val bitmap = decodeSampledBitmapFromStream(
                                                    stream,
                                                    MAX_BITMAP_DIMENSION,
                                                    MAX_BITMAP_DIMENSION
                                                )
                                                if (bitmap != null) {
                                                    val name =
                                                        "image_${bookId}_${chapterIndex}_seg${imageCounter++}"
                                                    savedImagePath = saveBitmapToPrivateStorage(
                                                        context = context,
                                                        bitmap = bitmap,
                                                        compressType = Bitmap.CompressFormat.JPEG,
                                                        quality = 80,
                                                        filenameWithoutExtension = name
                                                    )
                                                    bitmap.recycle()
                                                } else {
                                                    savedImagePath = "error_decode"
                                                }
                                            }
                                        } catch (e: Exception) {
                                        }
                                        if (savedImagePath != null && !savedImagePath!!.startsWith("error_")) {
                                            contentList.add(savedImagePath!!)
                                            imagePaths.add(
                                                savedImagePath!!
                                            )
                                        }
                                    }
                                }
                                currentParagraph.setLength(0)
                            }

                            "tbody", "thead", "tfoot" -> {}
                        }
                    }
                }
            }

            override fun tail(node: Node, depth: Int) {
                if (hitEndAnchor) return
                if (endElement != null && node == endElement) {
                    hitEndAnchor = true
                    flushParagraphWithFormatting(currentParagraph, contentList)
                    return
                }
                if (!passedStartAnchor) return
                if (!processingActive) return
                if (node is Element) {
                    val tagName = node.tagName().lowercase()
                    when (tagName) {
                        "b", "strong", "i", "em", "u" -> {
                            if (currentParagraph.isNotEmpty()) currentParagraph.append("</$tagName>")
                        }

                        "h1", "h2", "h3", "h4", "h5", "h6" -> {
                            if (currentParagraph.isNotEmpty() && currentParagraph.toString()
                                    .endsWith("<$tagName>")
                            ) {
                                currentParagraph.setLength(currentParagraph.length - "<$tagName>".length)
                            } else if (currentParagraph.isNotEmpty()) {
                                currentParagraph.append("</$tagName>")
                                flushParagraphWithFormatting(currentParagraph, contentList)
                            }
                        }

                        "p", "div", "ul", "ol", "li", "table", "blockquote", "hr", "tr" -> {
                            flushParagraphWithFormatting(currentParagraph, contentList)
                        }

                        "td", "th" -> {
                            if (currentParagraph.isNotEmpty() && !currentParagraph.endsWith(' ')) {
                                currentParagraph.append(" ")
                            }
                        }

                        "tbody", "thead", "tfoot" -> {}
                    }
                }
            }
        })
        if (!hitEndAnchor) {
            flushParagraphWithFormatting(currentParagraph, contentList)
        }
        return Pair(contentList, imagePaths)
    }

    /** Helper to add buffered paragraph text (with formatting) to the list */
    private fun flushParagraphWithFormatting(buffer: StringBuilder, list: MutableList<String>) {
        val paragraphText = buffer.toString()
        if (paragraphText.isNotBlank()) {
            list.add(paragraphText)
        }
        buffer.setLength(0)
    }

    /** Finds an image resource in the EPUB, handling relative paths */
    private fun getImageResourceFromBook(href: String, book: Book): Resource? {
        if (href.isBlank()) return null
        var resource = book.resources.getByHref(href)
        if (resource != null) return resource
        val normalizedHref = href.replace("../", "")
        resource = book.resources.getByHref(normalizedHref)
        if (resource != null) return resource
        val commonPrefixes = listOf(
            "images/",
            "Images/",
            "img/",
            "IMG/",
            "OEBPS/images/",
            "OEBPS/Images/",
            "OPS/images/",
            "OPS/Images/",
            "OEBPS/",
            "OPS/"
        )
        for (prefix in commonPrefixes) {
            resource = book.resources.getByHref(prefix + normalizedHref)
            if (resource != null) return resource
            resource = book.resources.getByHref(prefix + href)
            if (resource != null) return resource
        }
        val filename = href.substringAfterLast('/')
        if (filename.isNotBlank() && filename != href) {
            resource = book.resources.all.find { it.href?.endsWith(filename) == true }
            if (resource != null) return resource
        }
        return null
    }

    private suspend fun saveBookInfo(
        bookID: String,
        title: String,
        coverImagePath: String?,
        authors: List<Author>?,
        description: List<String>?,
        totalChapters: Int,
        storagePath: String
    ): Long {
        val normalizedAuthors = normalizeAuthorNames(authors)
        val bookEntity = BookEntity(
            bookId = bookID,
            title = title,
            coverImagePath = coverImagePath!!,
            authors = normalizedAuthors,
            description = description?.joinToString("\n") ?: "",
            totalChapter = totalChapters,
            currentChapter = 0,
            currentParagraph = 0,
            storagePath = storagePath,
            isEditable = false,
            fileType = "epub"
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
            content = content
        )
        chapterRepository.saveChapterContent(chapterEntity)
    }

    private suspend fun saveEmptyChapterContent(
        bookId: String,
        title: String,
        index: Int
    ) {
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

    private fun createNotificationChannel(
        channelId: String,
        channelName: String
    ) {
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
        return NotificationCompat.Builder(appContext, PROGRESS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Importing EPUB: ${fileName.take(35)}${if (fileName.length > 35) "..." else ""}")
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
        if (progress != null) builder.setProgress(100, progress.coerceIn(0, 100), false)
        else builder.setProgress(0, 0, true)
        try {
            setForeground(getForegroundInfoCompat(builder.build()))
        } catch (e: Exception) {
            notificationManager.notify(notificationId, builder.build())
        }
    }

    private fun sendCompletionNotification(
        isSuccess: Boolean,
        bookTitle: String?,
        failureReason: String? = null
    ) {
        val title = if (isSuccess) "Import Successful" else "Import Failed"
        val defaultTitle = bookTitle ?: "EPUB File"
        val userFriendlyReason = when {
            failureReason == null -> null
            failureReason.contains("Book already imported") -> "This book is already in your library."
            failureReason.contains("Could not parse EPUB file") -> "The selected file is not a valid EPUB."
            failureReason.contains("Table of Contents is empty") -> "Could not find chapters in the EPUB."
            failureReason.contains("Failed to open InputStream") -> "Could not read the selected file."
            failureReason.contains("OutOfMemoryError") -> "Ran out of memory processing the EPUB."
            else -> failureReason
        }
        val text = when {
            isSuccess -> "'$defaultTitle' added to your library."
            userFriendlyReason != null -> "Failed to import '$defaultTitle': $userFriendlyReason"
            else -> "Import failed for '$defaultTitle'."
        }
        val builder = NotificationCompat.Builder(appContext, COMPLETION_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle(title)
            setContentText(text)
            setStyle(
                NotificationCompat.BigTextStyle().bigText(text)
            )
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
            setAutoCancel(true)
        }
        notificationManager.notify(completionNotificationId, builder.build())
        notificationManager.cancel(notificationId)
    }

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
                    if (nameIndex != -1) displayName = cursor.getString(nameIndex)
                }
            }
        } catch (e: Exception) {
        }
        return displayName
    }

    private fun getForegroundInfoCompat(notification: Notification): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }
}