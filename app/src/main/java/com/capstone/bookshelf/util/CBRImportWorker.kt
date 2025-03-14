package com.capstone.bookshelf.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.capstone.bookshelf.R
import com.capstone.bookshelf.domain.book.BookRepository
import com.capstone.bookshelf.domain.book.ChapterRepository
import com.capstone.bookshelf.domain.book.ImagePathRepository
import com.capstone.bookshelf.domain.book.TableOfContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest

class CBRImportWorker(
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
                processCBRtoBook(context, pdfPath,fileName)
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
    private suspend fun processCBRtoBook(context: Context, pdfUriString: String, fileName: String) {
        return withContext(Dispatchers.IO) {
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