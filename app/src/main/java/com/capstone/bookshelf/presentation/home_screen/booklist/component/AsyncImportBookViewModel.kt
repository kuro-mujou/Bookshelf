package com.capstone.bookshelf.presentation.home_screen.booklist.component

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.capstone.bookshelf.domain.repository.BookRepository
import com.capstone.bookshelf.worker.CBZImportWorker
import com.capstone.bookshelf.worker.DriveEPUBImportWorker
import com.capstone.bookshelf.worker.EPUBImportWorker
import com.capstone.bookshelf.worker.PDFImportWorker
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
            val inputData = workDataOf(
                EPUBImportWorker.INPUT_URI_KEY to filePath,
            )
            val workRequest = OneTimeWorkRequest.Builder(EPUBImportWorker::class.java)
                .setInputData(inputData)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Can't open ebook file", Toast.LENGTH_SHORT).show()
        }
    }

    fun processAndSavePdf(
        context: Context,
        filePath: String,
        fileName: String,
        specialIntent: String
    ) = viewModelScope.launch {
        try {
            Toast.makeText(context, "Importing...", Toast.LENGTH_SHORT).show()
            val inputData = Data.Builder()
                .putString(PDFImportWorker.INPUT_URI_KEY, filePath)
                .putString(PDFImportWorker.ORIGINAL_FILENAME_KEY, fileName)
                .putString(PDFImportWorker.SPECIAL_INTENT_KEY, specialIntent)
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

    fun enqueueImportFromDriveLink(context: Context, driveLink: String) {
        val inputData = workDataOf(
            DriveEPUBImportWorker.INPUT_DRIVE_LINK_KEY to driveLink,
        )
        val importWorkRequest = OneTimeWorkRequest.Builder(DriveEPUBImportWorker::class.java)
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(context).enqueue(importWorkRequest)
    }

    fun test(context: Context, uri: String) = viewModelScope.launch {
        try {
            Toast.makeText(context, "Importing...", Toast.LENGTH_SHORT).show()
            val inputData = workDataOf(
                EPUBImportWorker.INPUT_URI_KEY to uri,
            )
            val workRequest = OneTimeWorkRequest.Builder(EPUBImportWorker::class.java)
                .setInputData(inputData)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Can't open ebook file", Toast.LENGTH_SHORT).show()
        }
    }
}