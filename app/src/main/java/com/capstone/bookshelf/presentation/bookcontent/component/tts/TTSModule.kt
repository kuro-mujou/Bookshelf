package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import org.koin.dsl.module

@UnstableApi
val ttsModule = module {
//    single {
//        val looper : Looper = Looper.getMainLooper()
//        val context : Context = get()
//        val ttsServiceHandler : TTSServiceHandler = get()
//        TTSPlayer(looper, context).apply { addListener(ttsServiceHandler)}
//    } binds arrayOf(
//        SimpleBasePlayer :: class,
//        OnInitListener :: class
//    )
//    single {
//        val context : Context = get()
//        val ttsPlayer : TTSPlayer = get()
//        val ttsPlayerCallback : TTSPlayerCallback = get()
//        MediaSession.Builder(context,ttsPlayer)
//            .setCallback(ttsPlayerCallback)
//            .build()
//    }
    single {
        AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }
    single {
        val context : Context = get()
        TTSServiceHandler(context)
    }
    single {
        TTSPlayerCallback()
    }
    single {
        TTSNotificationDescriptionHandler()
    }
    single {
        val context : Context = get()
        TTSNotificationListener(context)
    }
    single {
        TTSAudioManager()
    }
    single {
        val context :Context = get()
        TTSNotificationManager(context)
    }
}