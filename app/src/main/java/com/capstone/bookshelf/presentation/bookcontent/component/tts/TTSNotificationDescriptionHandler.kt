package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.app.PendingIntent
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
@UnstableApi
class TTSNotificationDescriptionHandler : PlayerNotificationManager.MediaDescriptionAdapter {
    private var title by mutableStateOf("test title")
    private var contentText by mutableStateOf("test content text")
    private var bitmap : Bitmap? = null

    fun updateTitle(value: String) {
        this.title = value
    }
    fun updateContentText(value: String){
        this.contentText = value
    }
    fun updateBitmap(value: Bitmap?){
        this.bitmap = value
    }

    override fun getCurrentContentTitle(player: Player): CharSequence {
        return title
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return null
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        return contentText
    }

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        return bitmap
    }
}