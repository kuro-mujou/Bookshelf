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
import kotlinx.coroutines.launch
import nl.siegmann.epublib.domain.Book
import java.io.File

class AsyncImportBookViewModel(
    private val bookRepository: BookRepository
) : ViewModel() {

    fun processAndSaveBook(
        book: Book,
        context: Context,
        cacheFilePath: String
    ) = viewModelScope.launch {
        try {
            val title = book.title
            val isAlreadyImported = bookRepository.isBookExist(title)
            if (isAlreadyImported) {
                Toast.makeText(context, "Book already imported", Toast.LENGTH_SHORT).show()
                deleteCacheFile(cacheFilePath)
            } else {
                val inputData = Data.Builder()
                    .putString(BookImportWorker.BOOK_TITLE_KEY, book.title)
                    .putString(BookImportWorker.BOOK_CACHE_PATH_KEY, cacheFilePath)
                    .build()

                val workRequest = OneTimeWorkRequest.Builder(BookImportWorker::class.java)
                    .setInputData(inputData)
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)
            }
        }catch (e: Exception){
            Toast.makeText(context, "Can't open book file", Toast.LENGTH_SHORT).show()
        }
    }
    private fun deleteCacheFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }
    }
}