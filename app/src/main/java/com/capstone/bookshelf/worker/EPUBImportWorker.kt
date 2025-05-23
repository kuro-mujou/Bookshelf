package com.capstone.bookshelf.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
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
import com.capstone.bookshelf.util.cleanHtmlForJsoup
import com.capstone.bookshelf.util.decodeSampledBitmapFromStream
import com.capstone.bookshelf.util.saveBitmapToPrivateStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.InternalReadiumApi
import org.readium.r2.shared.publication.Link
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
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.Result as ImportResult

@OptIn(InternalReadiumApi::class, ExperimentalReadiumApi::class)
class EPUBImportWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    companion object {
        const val INPUT_URI_KEY = "input_uri"
        private const val PROGRESS_CHANNEL_ID = "book_import_progress_channel"
        private const val COMPLETION_CHANNEL_ID = "book_import_completion_channel"
        private const val TAG = "EPUBImportWorker"
        private const val MAX_BITMAP_DIMENSION = 2048
    }

    private val md = MessageDigest.getInstance("MD5")
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notificationId = System.currentTimeMillis().toInt()
    private val completionNotificationId = notificationId + 1
    private val bookRepository: BookRepository by inject()
    private val tableOfContentsRepository: TableOfContentRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val imagePathRepository: ImagePathRepository by inject()
    private var finalBookTitle: String = ""
    private var finalBookId: String = ""

    init {
        createNotificationChannel(PROGRESS_CHANNEL_ID, "Book Import Progress")
        createNotificationChannel(COMPLETION_CHANNEL_ID, "Book Import Completion")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
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

    suspend fun processEPUB(
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
        val epubUriString = inputData.getString(INPUT_URI_KEY) ?: return ImportResult.failure(
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

        val actualTocToProcess = mergeLinks(publication.readingOrder, publication.tableOfContents)

        if (actualTocToProcess.isEmpty()) {
            return ImportResult.failure(throw Exception("Table of Content is empty"))
        }

        val processBookResult = processAndSaveBookInfo(
            epubUriString = epubUriString,
            publication = publication,
            tableOfContentSize = actualTocToProcess.size
        )
        if (processBookResult.isFailure) {
            return processBookResult
        }

        onProgress(null, "Processing Book content...")
        val processContentResult = processAndSaveBookContent(
            publication = publication,
            actualTocToProcess = actualTocToProcess,
            onProgress = onProgress
        )
        if (processContentResult.isFailure) {
            return processContentResult
        }

        return ImportResult.success(finalBookTitle)
    }

    private suspend fun processAndSaveBookContent(
        publication: Publication,
        actualTocToProcess: List<Link>,
        onProgress: suspend (progress: Int?, chapterName: String) -> Unit
    ): ImportResult<String> {
        val tocGroupedByResource =
            actualTocToProcess.groupBy { it.href.toString().substringBefore('#') }

        var overallChapterIndex = 0
        val totalTocEntriesForProgress = actualTocToProcess.size
        for ((resourceBaseHref, tocLinksInResource) in tocGroupedByResource) {
            if (resourceBaseHref.isBlank()) {
                continue
            }
            val mainResourceLink = Url(resourceBaseHref)
            var resourceFetchError: String? = null
            var rawHtml = ""
            try {
                publication.get(mainResourceLink!!)?.buffered().use {
                    it?.read()?.onSuccess { byteArray ->
                        rawHtml = cleanHtmlForJsoup(byteArray.decodeToString())
                    }
                }
            } catch (e: Exception) {
                resourceFetchError = "[ERR: Load/Parse Resource Exception]"
            }

            val needsSplitting = tocLinksInResource.any {
                it.href.toString().contains('#')
            } && tocLinksInResource.size > 1

            val realDoc = Jsoup.parse(rawHtml)
            if (!needsSplitting) {
                val representativeTocLink = tocLinksInResource.first()
                val chapterTitle = representativeTocLink.title
                    ?: realDoc
                        .selectFirst("h1, h2, h3, h4, h5, h6")
                        ?.text()
                    ?: realDoc.head()
                        .selectFirst("title")
                        ?.text()?.let { "$it - Chapter ${overallChapterIndex + 1}" }
                    ?: "Chapter ${overallChapterIndex + 1}"
                val progress =
                    ((overallChapterIndex + 1).toFloat() / totalTocEntriesForProgress * 100).toInt()
                onProgress(progress, chapterTitle)

                saveTableOfContentEntry(finalBookId, chapterTitle, overallChapterIndex)
                var parsedContent: Pair<List<String>, List<String>>? = null
                var segmentError: String? = null
                try {
                    parsedContent = parseChapterHtmlSegment(
                        document = realDoc,
                        startAnchorId = null,
                        endAnchorId = null,
                        publication = publication,
                        context = context,
                        bookId = finalBookId,
                        chapterIndex = overallChapterIndex,
                        baseChapterResourceHref = resourceBaseHref
                    )
                } catch (e: Exception) {
                    segmentError = "[ERR: Parse Full Segment]"
                }

                val contentToSave = parsedContent?.first ?: (if (segmentError != null) listOf(
                    segmentError
                ) else emptyList())
                val imagePathsFound = parsedContent?.second ?: emptyList()

                if (contentToSave.isNotEmpty()) {
                    saveChapterContent(
                        finalBookId,
                        chapterTitle,
                        overallChapterIndex,
                        contentToSave
                    )
                } else {
                    saveEmptyChapterContent(finalBookId, chapterTitle, overallChapterIndex)
                }
                val validImagePaths = imagePathsFound.filter { !it.startsWith("error_") }
                if (validImagePaths.isNotEmpty()) {
                    imagePathRepository.saveImagePath(finalBookId, validImagePaths)
                }
                overallChapterIndex++

                for (extraIdx in 1 until tocLinksInResource.size) {
                    val extraLink = tocLinksInResource[extraIdx]
                    val extraTitle = extraLink.title ?: "Chapter ${overallChapterIndex + 1}"
                    saveTableOfContentEntry(finalBookId, extraTitle, overallChapterIndex)
                    saveEmptyChapterContent(finalBookId, extraTitle, overallChapterIndex)
                    overallChapterIndex++
                }

            } else {
                for (idxInResource in tocLinksInResource.indices) {
                    val currentLink = tocLinksInResource[idxInResource]
                    val chapterTitle = currentLink.title
                        ?: realDoc
                            .selectFirst("h1, h2, h3, h4, h5, h6")
                            ?.text()
                        ?: realDoc.head()
                            .selectFirst("title")
                            ?.text()?.let { "$it - Chapter ${overallChapterIndex + 1}" }
                        ?: "Chapter ${overallChapterIndex + 1}"
                    val progress =
                        ((overallChapterIndex + 1).toFloat() / totalTocEntriesForProgress * 100).toInt()
                    onProgress(progress, chapterTitle)

                    saveTableOfContentEntry(finalBookId, chapterTitle, overallChapterIndex)

                    val actualStartAnchorForParsing: String?
                    val actualEndAnchorForParsing: String?

                    if (idxInResource == 0) {
                        actualStartAnchorForParsing = null
                        actualEndAnchorForParsing =
                            tocLinksInResource.getOrNull(1)?.href?.toString()
                                ?.substringAfterLast('#', "")?.takeIf {
                                it.isNotBlank() && tocLinksInResource[1].href.toString()
                                    .contains('#')
                            }
                    } else {
                        actualStartAnchorForParsing =
                            currentLink.href.toString().substringAfterLast('#', "").takeIf {
                                it.isNotBlank() && currentLink.href.toString().contains('#')
                            }
                        actualEndAnchorForParsing =
                            tocLinksInResource.getOrNull(idxInResource + 1)?.href?.toString()
                                ?.substringAfterLast('#', "")?.takeIf {
                                it.isNotBlank() && tocLinksInResource[idxInResource + 1].href.toString()
                                    .contains('#')
                            }
                    }
                    var parsedContent: Pair<List<String>, List<String>>? = null
                    var segmentError: String? = null
                    try {
                        parsedContent = parseChapterHtmlSegment(
                            document = realDoc,
                            startAnchorId = actualStartAnchorForParsing,
                            endAnchorId = actualEndAnchorForParsing,
                            publication = publication,
                            context = context,
                            bookId = finalBookId,
                            chapterIndex = overallChapterIndex,
                            baseChapterResourceHref = resourceBaseHref
                        )
                    } catch (e: Exception) {
                        segmentError = "[ERR: Parse Segment]"
                    }

                    val contentToSave = parsedContent?.first ?: (if (segmentError != null) listOf(
                        segmentError
                    ) else emptyList())
                    val imagePathsFound = parsedContent?.second ?: emptyList()

                    if (contentToSave.isNotEmpty()) {
                        saveChapterContent(
                            finalBookId,
                            chapterTitle,
                            overallChapterIndex,
                            contentToSave
                        )
                    } else {
                        saveEmptyChapterContent(finalBookId, chapterTitle, overallChapterIndex)
                    }
                    val validImagePaths = imagePathsFound.filter { !it.startsWith("error_") }
                    if (validImagePaths.isNotEmpty()) {
                        imagePathRepository.saveImagePath(finalBookId, validImagePaths)
                    }
                    overallChapterIndex++
                }
            }
        }
        return ImportResult.success(finalBookTitle)
    }

    @OptIn(ExperimentalReadiumApi::class)
    private suspend fun parseChapterHtmlSegment(
        document: Document,
        startAnchorId: String?,
        endAnchorId: String?,
        publication: Publication,
        context: Context,
        bookId: String,
        chapterIndex: Int,
        baseChapterResourceHref: String,
    ): Pair<List<String>, List<String>> {
        val contentList = mutableListOf<String>()
        val imagePaths = mutableListOf<String>()
        var currentParagraph = StringBuilder()
        var insideHeading = false
        var spaceAddedInHeading = false
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
                    if (!insideHeading) {
                        flushParagraphWithFormatting(currentParagraph, contentList)
                    }
                    return
                }

                if (!passedStartAnchor && startElement != null && node == startElement) {
                    passedStartAnchor = true
                    processingActive = true
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

                            "p", "div", "ul", "ol", "li", "table", "blockquote", "hr",
                            "figure", "figcaption", "details", "summary", "main", "header",
                            "footer", "nav", "aside", "article", "section" -> {
                                if (!insideHeading) {
                                    flushParagraphWithFormatting(currentParagraph, contentList)
                                }
                            }

                            "h1", "h2", "h3", "h4", "h5", "h6" -> {
                                currentParagraph.append("<$tagName>")
                                insideHeading = true
                                spaceAddedInHeading = false
                            }

                            "br" -> {
                                if (insideHeading && !spaceAddedInHeading) {
                                    currentParagraph.append(" ")
                                    spaceAddedInHeading = true
                                } else if (!insideHeading) {
                                    flushParagraphWithFormatting(currentParagraph, contentList)
                                }
                            }

                            "b", "strong", "i", "em", "u", "s", "strike", "del", "sup", "sub", "code" -> {
                                currentParagraph.append("<$tagName>")
                            }

                            "td", "th" -> {
                                if (currentParagraph.isNotEmpty() && !currentParagraph.endsWith(' ')) {
                                    currentParagraph.append(" ")
                                }
                            }

                            "img", "image" -> {
                                if (!insideHeading) {
                                    flushParagraphWithFormatting(currentParagraph, contentList)
                                }
                                val srcAttr = when (tagName) {
                                    "img" -> node.attr("src").ifEmpty { node.attr("data-src") }
                                    "image" -> node.attr("xlink:href").ifEmpty { node.attr("href") }
                                    else -> ""
                                }
                                if (srcAttr.isNotBlank()) {
                                    val cleanHref = srcAttr.replace(Regex("""^(\.\./)+"""), "")
                                    contentList.add(cleanHref)
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }

            override fun tail(node: Node, depth: Int) {
                if (hitEndAnchor && node != endElement) return
                if (!passedStartAnchor || !processingActive) return

                if (node is Element) {
                    val tagName = node.tagName().lowercase()
                    when (tagName) {
                        "b", "strong", "i", "em", "u", "s", "strike", "del", "sup", "sub", "code" -> {
                            if (currentParagraph.isNotEmpty())
                                currentParagraph.append("</$tagName>")
                        }

                        "h1", "h2", "h3", "h4", "h5", "h6" -> {
                            if (currentParagraph.isNotEmpty()) {
                                currentParagraph.append("</$tagName>")
                                if (!insideHeading) {
                                    flushParagraphWithFormatting(currentParagraph, contentList)
                                }
                            }
                            insideHeading = false
                            spaceAddedInHeading = false
                        }

                        "p", "div", "ul", "ol", "li", "table", "blockquote", "hr", "tr",
                        "figure", "figcaption", "details", "summary", "main", "header",
                        "footer", "nav", "aside", "article", "section" -> {
                            if (!insideHeading) {
                                flushParagraphWithFormatting(currentParagraph, contentList)
                            }
                        }

                        "td", "th" -> {
                            if (currentParagraph.isNotEmpty() && !currentParagraph.endsWith(' ')) {
                                currentParagraph.append(" ")
                            }
                        }

                        else -> {}
                    }
                }
                if (endElement != null && node == endElement) {
                    if (!insideHeading) {
                        flushParagraphWithFormatting(currentParagraph, contentList)
                    }
                    hitEndAnchor = true
                }
            }
        })

        if (processingActive && !hitEndAnchor) {
            if (!insideHeading) {
                flushParagraphWithFormatting(currentParagraph, contentList)
            }
        }

        val imageRegex =
            Regex("""(?i)\b[\w./-]+?\.(jpg|jpeg|png|gif|webp|bmp|svg)\b""", RegexOption.IGNORE_CASE)
        val fixedContentList = contentList.mapNotNull {
            if (imageRegex.containsMatchIn(it)) {
                val imagePath =
                    saveImageFromPublication(it, publication, chapterIndex, contentList.indexOf(it))
                if (imagePath.startsWith("error")) {
                    null
                } else {
                    imagePaths.add(imagePath)
                    imagePath
                }
            } else {
                it
            }
        }
        return Pair(fixedContentList, imagePaths)
    }

    /** Helper to add buffered paragraph text (with formatting) to the list */
    private fun flushParagraphWithFormatting(buffer: StringBuilder, list: MutableList<String>) {
        val paragraphText = buffer.toString()
        if (paragraphText.isNotBlank()) {
            list.add(paragraphText)
        }
        buffer.setLength(0)
    }

    private fun flattenLinks(links: List<Link>): List<Link> {
        val result = mutableListOf<Link>()
        for (link in links) {
            result.add(link)
            if (link.children.isNotEmpty()) {
                result.addAll(flattenLinks(link.children))
            }
        }
        return result
    }

    private fun mergeLinks(readingOrder: List<Link>, tableOfContents: List<Link>): List<Link> {
        val flatReadingOrder = flattenLinks(readingOrder)
        val flatToc = flattenLinks(tableOfContents)
        val (baseList, overrideList) = if (flatReadingOrder.size >= flatToc.size) {
            flatReadingOrder to flatToc
        } else {
            flatToc to flatReadingOrder
        }

        val overrideMap = overrideList.associateBy { it.href }

        return baseList.map { link ->
            if (link.title.isNullOrBlank()) {
                val match = overrideMap[link.href]
                if (match != null && !match.title.isNullOrBlank()) {
                    link.copy(title = match.title)
                } else {
                    link
                }
            } else {
                link
            }
        }
    }

    @OptIn(ExperimentalReadiumApi::class)
    private suspend fun saveImageFromPublication(
        imageElementHref: String,
        publication: Publication,
        tocIndex: Int,
        paragraphIndex: Int,
    ): String {
        val match =
            publication.resources.firstOrNull { it.href.toString().endsWith(imageElementHref) }
        var imagePath = "error"
        publication.get(match!!)?.buffered()?.use { buffering ->
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
                            compressType = Bitmap.CompressFormat.JPEG,
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
    ): ImportResult<String> {

        finalBookTitle = publication.metadata.title.toString()
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
                compressType = Bitmap.CompressFormat.JPEG,
                quality = 80,
                filenameWithoutExtension = "cover_$finalBookId"
            )
        } ?: "error"
        val authors = publication.metadata.authors.map { it.name.toString() }
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
                fileType = "epub"
            )
        ).run {
            bookRepository.updateRecentRead(finalBookId)
        }
        imagePathRepository.saveImagePath(finalBookId, listOf(coverImagePath))
        return ImportResult.success(finalBookTitle)
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
        return NotificationCompat.Builder(context, PROGRESS_CHANNEL_ID)
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
        val builder = NotificationCompat.Builder(context, COMPLETION_CHANNEL_ID).apply {
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

    private fun getForegroundInfoCompat(notification: Notification): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

}