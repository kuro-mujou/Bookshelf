package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.capstone.bookshelf.R
import kotlinx.coroutines.launch

@UnstableApi
class PlayerViewModel : ViewModel() {
    var mediaController: MediaController? = null

    fun initialize(context: Context) {
        if (mediaController != null) {
            return // Already initialized
        }
        viewModelScope.launch {
            val serviceIntent = Intent(context, PlaybackService::class.java)
            context.startService(serviceIntent)

        }
        val serviceComponentName = ComponentName(context, PlaybackService::class.java)
        val sessionToken = SessionToken(context, serviceComponentName)

        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get()
                Log.d("PlayerViewModel", "MediaController initialized successfully")
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Failed to build MediaController", e)
                mediaController = null
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun play(mediaItem: MediaItem) {
        mediaController?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun togglePlayPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }
    fun stop() {
        mediaController?.stop()
    }
    fun release() {
        mediaController?.release()
    }
    fun seekToNext() {
        mediaController?.seekToNext()
    }
    fun seekToPrevious() {
        mediaController?.seekToPrevious()
    }
    fun seekBack() {
        mediaController?.seekBack()
    }
    fun seekForward() {
        mediaController?.seekForward()
    }

    private suspend fun loadImage(imagePath: String, context: Context) : Bitmap? {
        val imageUrl =
            if (imagePath == "error")
                R.mipmap.book_cover_not_available
            else
                imagePath
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .build()
        val result = (loader.execute(request) as? SuccessResult)?.drawable
        return result?.toBitmap()
    }
    override fun onCleared() {
        mediaController?.run {
            stop()
            release()
        }
        super.onCleared()
    }
}