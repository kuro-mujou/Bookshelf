package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.app.Notification
import android.app.Service.STOP_FOREGROUND_REMOVE
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager

@UnstableApi
class TTSNotificationListener(
    context: Context
) : PlayerNotificationManager.NotificationListener {

    private var notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
    private lateinit var mediaSession : MediaSession
    private lateinit var mediaSessionService: MediaSessionService

    fun getService(service: MediaSessionService, mediaSession: MediaSession) {
        mediaSessionService = service
        this.mediaSession = mediaSession
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
        if (mediaSession.player.playbackState != Player.STATE_ENDED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mediaSessionService.startForeground(notificationId, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
                Log.d("TTSNotificationListener", "startForeground")
            }else{
                mediaSessionService.startForeground(notificationId, notification)
            }
        } else {
            mediaSessionService.stopForeground(STOP_FOREGROUND_REMOVE)
            Log.d("TTSNotificationListener", "stopForeground")
        }
    }
    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)
    }
}