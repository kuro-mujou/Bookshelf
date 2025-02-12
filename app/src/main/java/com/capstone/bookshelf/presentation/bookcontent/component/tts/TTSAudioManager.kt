package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.util.Log
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class TTSAudioManager {
    private lateinit var audioManager: AudioManager
    private lateinit var audioAttributes: AudioAttributes
    private lateinit var audioFocusRequest: AudioFocusRequest
    private lateinit var mediaSession : MediaSession

    fun getMediaSession(mediaSession: MediaSession){
        this.mediaSession = mediaSession
    }

    private var audioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    Log.d("TTSAudioManager","audio focus gain")
                    requestAudioFocus()
                    mediaSession.player.play()
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    Log.d("TTSAudioManager","audio focus Loss")
                    mediaSession.player.pause()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    Log.d("TTSAudioManager","audio focus loss transient")
//                        mediaSession.player.pause()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    Log.d("TTSAudioManager","audio focus loss transient can duck")
                    mediaSession.player.pause()
                }
            }
        }
    
    fun getAudioManager(
        mediaSessionService: MediaSessionService
    ){
        audioManager = mediaSessionService.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createAudioAttributes()
    }
    private fun createAudioAttributes(){
        audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()
    }
    fun requestAudioFocus(){
        Log.d("TTSAudioManager","Audio focus requested")
        audioManager.requestAudioFocus(audioFocusRequest)
    }
    fun abandonAudioFocus(){
        Log.d("TTSAudioManager","Audio focus abandoned")
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
    }
}