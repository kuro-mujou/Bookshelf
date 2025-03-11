package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.content.Context
import androidx.media3.common.util.UnstableApi
import org.koin.dsl.module

@UnstableApi
val ttsModule = module {
    single {
        val context: Context = get()
        TTSServiceHandler(
            context = context
        )
    }
}