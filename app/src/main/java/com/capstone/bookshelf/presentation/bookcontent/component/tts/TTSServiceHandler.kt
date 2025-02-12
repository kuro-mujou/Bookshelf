package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.content.Context
import android.util.Log
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import org.koin.java.KoinJavaComponent.inject

@UnstableApi
class TTSServiceHandler (
    val context : Context
) : Player.Listener{
    private val ttsNotificationManager by inject<TTSNotificationManager>(TTSNotificationManager::class.java)
    private val ttsAudioManager by inject<TTSAudioManager>(TTSAudioManager::class.java)
    private lateinit var mediaSession : MediaSession

    fun getMediaSession(mediaSession: MediaSession) {
        this.mediaSession = mediaSession
    }
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        Log.d("TTSServiceHandler", "onIsPlayingChanged: $isPlaying")
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                Log.d("TTSServiceHandler", "onPlaybackStateChanged: State ready")
                ttsNotificationManager.createNotificationChannel()
                ttsNotificationManager.buildNotification(mediaSession)
                ttsAudioManager.requestAudioFocus()
            }
            Player.STATE_BUFFERING -> {
                Log.d("TTSServiceHandler", "onPlaybackStateChanged: State buffering")
                ttsAudioManager.requestAudioFocus()
            }
            Player.STATE_ENDED -> {
                Log.d("TTSServiceHandler", "onPlaybackStateChanged: State ended")
                ttsNotificationManager.abandonNotification()
                ttsAudioManager.abandonAudioFocus()
            }
            Player.STATE_IDLE -> {
                Log.d("TTSServiceHandler", "onPlaybackStateChanged: State idle")
            }
        }
        super.onPlaybackStateChanged(playbackState)
    }
    override fun onPlayerError(error: PlaybackException) {
        Log.e("TTSServiceHandler", "Player error: ${error.errorCodeName} - ${error.message}", error)
    }
}

sealed class TTSPlayerEvent {
    data object PlayPause : TTSPlayerEvent()
    data object NextChapter : TTSPlayerEvent()
    data object NextParagraph : TTSPlayerEvent()
    data object PreviousChapter : TTSPlayerEvent()
    data object PreviousParagraph : TTSPlayerEvent()
    data object Stop : TTSPlayerEvent()
    data class UpdateProgress(val newProgress: Float) : TTSPlayerEvent()//may not need
}

sealed class TTSMediaState {
    data object Initial : TTSMediaState()
    data class Ready(val duration: Long) : TTSMediaState()
    data class Progress(val progress: Long) : TTSMediaState()
    data class Buffering(val progress: Long) : TTSMediaState()
    data class Playing(val isPlaying: Boolean) : TTSMediaState()
}