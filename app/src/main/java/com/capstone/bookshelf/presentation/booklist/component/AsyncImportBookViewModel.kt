package com.capstone.bookshelf.presentation.booklist.component

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.capstone.bookshelf.domain.book.BookRepository
import com.capstone.bookshelf.util.BookImportWorker
import com.capstone.bookshelf.util.CBRImportWorker
import com.capstone.bookshelf.util.CBZImportWorker
import com.capstone.bookshelf.util.PDFImportWorker
import kotlinx.coroutines.launch
import nl.siegmann.epublib.domain.Book

class AsyncImportBookViewModel(
    private val bookRepository: BookRepository
) : ViewModel() {

    fun processAndSaveBook(
        book: Book,
        context: Context,
        filePath: String
    ) = viewModelScope.launch {
        try {
            Toast.makeText(context, "Importing...", Toast.LENGTH_SHORT).show()
            val title = book.title
            val isAlreadyImported = bookRepository.isBookExist(title)
            if (isAlreadyImported) {
                Toast.makeText(context, "Book already imported", Toast.LENGTH_SHORT).show()
            } else {
                val inputData = Data.Builder()
                    .putString(BookImportWorker.BOOK_TITLE_KEY, book.title)
                    .putString(BookImportWorker.BOOK_CACHE_PATH_KEY, filePath)
                    .build()

                val workRequest = OneTimeWorkRequest.Builder(BookImportWorker::class.java)
                    .setInputData(inputData)
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)
            }
        }catch (e: Exception){
            e.printStackTrace()
            Toast.makeText(context, "Can't open ebook file", Toast.LENGTH_SHORT).show()
        }
    }
    fun processAndSavePdf(
        context: Context,
        filePath: String,
        fileName: String
    ) = viewModelScope.launch {
        try {
            Toast.makeText(context, "Importing...", Toast.LENGTH_SHORT).show()
            val inputData = Data.Builder()
                .putString(PDFImportWorker.BOOK_CACHE_PATH_KEY, filePath)
                .putString(PDFImportWorker.FILE_NAME_KEY, fileName)
                .build()
            val workRequest = OneTimeWorkRequest.Builder(PDFImportWorker::class.java)
                .setInputData(inputData)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Can't open PDF file", Toast.LENGTH_SHORT).show()
        }
    }

    fun processAndSaveCBZ(
        context: Context,
        filePath: String,
        fileName: String
    ) = viewModelScope.launch {
        try {
            Toast.makeText(context, "Importing...", Toast.LENGTH_SHORT).show()
            val inputData = Data.Builder()
                .putString(CBZImportWorker.BOOK_CACHE_PATH_KEY, filePath)
                .putString(CBZImportWorker.FILE_NAME_KEY, fileName)
                .build()
            val workRequest = OneTimeWorkRequest.Builder(CBZImportWorker::class.java)
                .setInputData(inputData)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Can't open PDF file", Toast.LENGTH_SHORT).show()
        }
    }
    fun processAndSaveCBR(
        context: Context,
        filePath: String,
        fileName: String
    ) = viewModelScope.launch {
        try {
            Toast.makeText(context, "Importing...", Toast.LENGTH_SHORT).show()
            val inputData = Data.Builder()
                .putString(CBRImportWorker.BOOK_CACHE_PATH_KEY, filePath)
                .putString(CBRImportWorker.FILE_NAME_KEY, fileName)
                .build()
            val workRequest = OneTimeWorkRequest.Builder(CBRImportWorker::class.java)
                .setInputData(inputData)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Can't open PDF file", Toast.LENGTH_SHORT).show()
        }
    }

}