package com.capstone.bookshelf.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
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
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest

class EpubImportWorker(
    private val appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {

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
        private const val TAG = "EpubImportWorker"

        private const val PROGRESS_CHANNEL_ID = "book_import_progress_channel"
        private const val COMPLETION_CHANNEL_ID = "book_import_completion_channel"

        private const val MAX_BITMAP_DIMENSION = 2048
    }

    init {
        createNotificationChannelIfNeeded(PROGRESS_CHANNEL_ID, "Book Import Progress")
        createNotificationChannelIfNeeded(COMPLETION_CHANNEL_ID, "Book Import Completion")
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val epubUriString = inputData.getString(INPUT_URI_KEY)
        val originalFileName = inputData.getString(ORIGINAL_FILENAME_KEY)
            ?: getDisplayNameFromUri(appContext, epubUriString?.toUri()) ?: "Unknown EPUB"

        if (epubUriString == null) {
            sendCompletionNotification(false, originalFileName, "Input data missing")
            return@withContext Result.failure()
        }
        val epubUri = epubUriString.toUri()
        val initialNotification = createProgressNotificationBuilder(
            originalFileName, "Starting import..."
        ).build()
        try {
            setForeground(getForegroundInfoCompat(initialNotification))
        } catch (e: Exception) {
            // Proceed even if foreground fails, notification will still work
        }

        val processingResult = processEpubViaCache(
            appContext,
            epubUri,
            originalFileName,
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
     * Processes EPUB by copying to cache, parsing, extracting, and saving data.
     * Returns Result.success(bookTitle) or Result.failure(exception).
     */
    private suspend fun processEpubViaCache(
        context: Context,
        epubUri: Uri,
        originalFileName: String,
        onProgress: suspend (progress: Int?, message: String) -> Unit
    ): kotlin.Result<String> {
        var tempEpubFile: File? = null
        var book: Book? = null
        var bookId: String? = null
        var finalBookTitle = originalFileName.substringBeforeLast('.')

        try {
            onProgress(null, "Copying file...")
            tempEpubFile = File.createTempFile("epub_import_", ".epub", context.cacheDir)
            context.contentResolver.openInputStream(epubUri)?.use { inputStream ->
                FileOutputStream(tempEpubFile).use { outputStream ->
                    inputStream.copyTo(outputStream, 8192)
                }
            } ?: run {
                tempEpubFile?.delete()
                return kotlin.Result.failure(IOException("Failed to open InputStream for EPUB"))
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
            finalBookTitle = book.title?.takeIf { it.isNotBlank() } ?: finalBookTitle
            bookId = BigInteger(1, md.digest(originalFileName.toByteArray()))
                .toString(16).padStart(32, '0')
            if (bookRepository.isBookExist(finalBookTitle)) {
                return kotlin.Result.failure(IOException("Book already imported"))
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
                        coverImagePath = saveBitmapToPrivateStorage(context, bitmap, coverFilename)
                        bitmap.recycle()
                    } else {
                        coverImagePath = "error_decode_cover"
                    }
                }
            } catch (e: Exception) {
                coverImagePath = "error_processing_cover"
            }
            val finalCoverPathForDb =
                if (coverImagePath?.startsWith("error_") == true) "error" else coverImagePath
            onProgress(null, "Processing table of contents...")
            val flattenedToc = flattenTocReferences(book.tableOfContents?.tocReferences)
            val totalChapters = flattenedToc.size

            if (totalChapters == 0) {
                return kotlin.Result.failure(IOException("EPUB Table of Contents is empty or missing."))
            }
            onProgress(null, "Saving book information...")
            saveBookInfo(
                bookId, finalBookTitle, finalCoverPathForDb, book.metadata.authors,
                book.metadata.types, book.metadata.descriptions.firstOrNull(),
                totalChapters, tempEpubFile.absolutePath
            )
            if (finalCoverPathForDb != null) {
                imagePathRepository.saveImagePath(bookId, listOf(finalCoverPathForDb))
            }
            processAndSaveChapters(bookId, book, flattenedToc, context, onProgress)
            return kotlin.Result.success(finalBookTitle)
        } catch (e: Exception) {
            return kotlin.Result.failure(e)
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
            var document: org.jsoup.nodes.Document? = null
            try {
                resource.inputStream.use { stream ->
                    chapterHtml = stream.bufferedReader().readText()
                }
                document = Jsoup.parse(chapterHtml!!, resourceHref)
            } catch (e: Exception) {

            }

            val needsSplitting =
                tocEntriesForResource.any { it.fragmentId != null }

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
                        "Processing ${chapterTitle.take(30)}... (Segment ${i + 1})"
                    )
                    saveTableOfContentEntry(bookId, chapterTitle, overallChapterIndex)
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

    /** Parses HTML segment, attempts to keep inline tags within the same paragraph */
    private fun parseChapterHtmlSegment(
        document: org.jsoup.nodes.Document,
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
                    flushParagraphWithFormatting(
                        currentParagraph,
                        contentList
                    )
                    return
                }
                var isStartAnchorNode = false
                if (!passedStartAnchor && startElement != null && node == startElement) {
                    passedStartAnchor = true
                    processingActive = true
                    isStartAnchorNode = true
                }
                when (node) {
                    is TextNode -> {
                        val text = node.text()
                        val normalizedText = text.replace(Regex("[ \t\n\r]{2,}"), " ")
                        if (normalizedText.isNotEmpty()) {
                            if (currentParagraph.isNotEmpty() &&
                                !currentParagraph.endsWith(' ') &&
                                !normalizedText.first().isWhitespace()
                            ) {
                                currentParagraph.append(" ")
                            }
                            currentParagraph.append(normalizedText)
                        }
                    }

                    is Element -> {
                        val tagName = node.tagName().lowercase()
                        when (tagName) {
                            "p", "div", "ul", "ol", "li", "table", "blockquote", "hr", "h1", "h2", "h3", "h4", "h5", "h6", "tr" -> {
                                flushParagraphWithFormatting(currentParagraph, contentList)
                                if (tagName.startsWith("h")) currentParagraph.append("<$tagName>")
                            }
                            "br" -> {
                                val parent = node.parentNode()
                                if (parent is Element && parent.tagName().lowercase().startsWith("h")) {
                                    if (!currentParagraph.endsWith(' '))
                                        currentParagraph.append(" ")
                                } else {
                                    flushParagraphWithFormatting(currentParagraph, contentList)
                                }
                            }

                            "b", "strong", "i", "em", "u" -> {
                                if (currentParagraph.isNotEmpty() && !currentParagraph.endsWith(' ')) {
                                    currentParagraph.append(" ")
                                }
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
                                                    savedImagePath =
                                                        saveBitmapToPrivateStorage(
                                                            context = context,
                                                            bitmap = bitmap,
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
                                            imagePaths.add(savedImagePath!!)
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
                    hitEndAnchor =true
                    flushParagraphWithFormatting(currentParagraph, contentList)
                    return
                }
                if (startElement != null && node == startElement) {
                    if (!passedStartAnchor) {
                        passedStartAnchor = true
                        processingActive = true
                    }
                }
                if (!processingActive) return

                if (node is Element) {
                    val tagName = node.tagName().lowercase()
                    when (tagName) {
                        "b", "strong", "i", "em", "u" -> {
                            if (currentParagraph.isNotEmpty() && !currentParagraph.endsWith(' ')) {
                                currentParagraph.append(" ")
                            }
                            if (currentParagraph.isNotEmpty()) {
                                currentParagraph.append("</$tagName>")
                            }
                        }
                        "h1", "h2", "h3", "h4", "h5", "h6" -> {
                            if (currentParagraph.isNotEmpty()) {
                                if (currentParagraph.toString().endsWith("<$tagName>")) {
                                    currentParagraph.setLength(currentParagraph.length - "<$tagName>".length)
                                }
                                else {
                                    currentParagraph.append("</$tagName>")
                                    flushParagraphWithFormatting(
                                        currentParagraph,
                                        contentList
                                    )
                                }
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
        flushParagraphWithFormatting(currentParagraph, contentList)
        return Pair(contentList, imagePaths)
    }

    /** Helper to add buffered paragraph text (with formatting) to the list */
    private fun flushParagraphWithFormatting(buffer: StringBuilder, list: MutableList<String>) {
        val paragraphText = buffer.toString().trim()
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
        categories: List<String>?,
        description: String?,
        totalChapters: Int,
        storagePath: String
    ): Long {
        val cleanedCategories =
            categories?.mapNotNull { it.trim().takeIf(String::isNotBlank) } ?: emptyList()
        val normalizedAuthors = normalizeAuthorNames(authors)
        val bookEntity = BookEntity(
            bookId = bookID,
            title = title,
            coverImagePath = coverImagePath!!,
            authors = normalizedAuthors,
            categories = cleanedCategories,
            description = description?.trim()?.takeIf(String::isNotBlank),
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
        val tocEntity = TableOfContentEntity(bookId = bookId, title = title, index = index)
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

    private fun createNotificationChannelIfNeeded(
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
        val displayFileName = fileName.substringBeforeLast(".")
        return NotificationCompat.Builder(appContext, PROGRESS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Importing EPUB: ${displayFileName.take(35)}${if (displayFileName.length > 35) "..." else ""}")
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
            else -> "An unexpected error occurred."
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