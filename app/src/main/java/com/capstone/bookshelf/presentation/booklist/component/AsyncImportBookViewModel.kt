package com.capstone.bookshelf.presentation.booklist.component

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.util.CBZImportWorker
import com.capstone.bookshelf.util.EpubImportWorker
import com.capstone.bookshelf.util.PDFImportWorker
import kotlinx.coroutines.launch

class AsyncImportBookViewModel(
    private val bookRepository: BookRepository
) : ViewModel() {

    fun processAndSaveBook(
        context: Context,
        filePath: String,
        fileName: String
    ) = viewModelScope.launch {
        try {
            Toast.makeText(context, "Importing...", Toast.LENGTH_SHORT).show()
            val inputData = Data.Builder()
                .putString(EpubImportWorker.INPUT_URI_KEY, filePath)
                .putString(EpubImportWorker.ORIGINAL_FILENAME_KEY, fileName)
                .build()
            val workRequest = OneTimeWorkRequest.Builder(EpubImportWorker::class.java)
                .setInputData(inputData)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
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
                .putString(PDFImportWorker.INPUT_URI_KEY, filePath)
                .putString(PDFImportWorker.ORIGINAL_FILENAME_KEY, fileName)
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
                .putString(CBZImportWorker.INPUT_URI_KEY, filePath)
                .putString(CBZImportWorker.ORIGINAL_FILENAME_KEY, fileName)
                .build()
            val workRequest = OneTimeWorkRequest.Builder(CBZImportWorker::class.java)
                .setInputData(inputData)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Can't open CBZ file", Toast.LENGTH_SHORT).show()
        }
    }
}