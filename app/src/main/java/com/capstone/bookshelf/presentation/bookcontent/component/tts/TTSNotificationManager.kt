package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service.STOP_FOREGROUND_REMOVE
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.capstone.bookshelf.presentation.bookcontent.component.tts.PlaybackService.Companion.CHANNEL_ID
import com.capstone.bookshelf.presentation.bookcontent.component.tts.PlaybackService.Companion.CHANNEL_NAME
import com.capstone.bookshelf.presentation.bookcontent.component.tts.PlaybackService.Companion.NOTIFICATION_ID
import org.koin.java.KoinJavaComponent.inject

@UnstableApi
class TTSNotificationManager (
    private val context: Context,
) {
    private val ttsNotificationDescriptionHandler by inject<TTSNotificationDescriptionHandler>(TTSNotificationDescriptionHandler::class.java)
    private val ttsNotificationListener by inject<TTSNotificationListener>(TTSNotificationListener::class.java)
    private var notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
    private lateinit var mediaSessionService: MediaSessionService
    private var player: Player? = null
    fun getService(service: MediaSessionService, player: TTSPlayer) {
        mediaSessionService = service
        this.player = player
    }

    init {
        createNotificationChannel()
    }

    fun buildNotification(mediaSession: MediaSession) {
        PlayerNotificationManager.Builder(context,NOTIFICATION_ID,CHANNEL_ID)
            .setMediaDescriptionAdapter(ttsNotificationDescriptionHandler)
            .setNotificationListener(ttsNotificationListener)
            .build()
            .apply {
                setMediaSessionToken(mediaSession.platformToken)
                setPlayer(player)
            }
    }

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
        Log.d("NotificationManager","create notification channel")
    }
    fun abandonNotification(){
        mediaSessionService.stopForeground(STOP_FOREGROUND_REMOVE)
        notificationManager.cancel(0)
        Log.d("NotificationManager","stop foreground")
    }
    fun abandonNotificationChannel(){
        notificationManager.deleteNotificationChannel(CHANNEL_ID)
    }
}