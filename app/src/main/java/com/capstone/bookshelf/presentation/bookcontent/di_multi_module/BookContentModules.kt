package com.capstone.bookshelf.presentation.bookcontent.di_multi_module

//import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSMediaViewModel
//import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSNotificationManager
//import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSPlayer
//import com.capstone.bookshelf.presentation.bookcontent.component.tts.TTSServiceHandler
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPaletteViewModel
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerViewModel
import com.capstone.bookshelf.presentation.bookcontent.topbar.TopBarViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

@UnstableApi
val bookContentModule = module {
    viewModelOf(::BottomBarViewModel)
    viewModelOf(::AutoScrollViewModel)
    viewModelOf(::ContentViewModel)
    viewModelOf(::DrawerContainerViewModel)
    viewModelOf(::TopBarViewModel)
    viewModelOf(::ColorPaletteViewModel)
//    viewModelOf(::TTSMediaViewModel)
}
//@UnstableApi
//val ttsModule = module {
//
//    single {
//        AudioAttributes.Builder()
//            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
//            .setUsage(C.USAGE_MEDIA)
//            .build()
//    }
//
//    single {
//        val context: Context = get()
//        val audioAttributes: AudioAttributes = get()
//        ExoPlayer.Builder(context)
//            .setAudioAttributes(audioAttributes, true)
//            .setHandleAudioBecomingNoisy(true)
//            .setTrackSelector(DefaultTrackSelector(context))
//            .build()
//    }
//    single {
//        TTSPlayer(Looper.getMainLooper(), get())
//    }
//
//    single {
//        val context: Context = get()
//        val player: ExoPlayer = get()
//        MediaSession.Builder(context, player).build()
//    }
//
//    single {
//        val context: Context = get()
//        val player: ExoPlayer = get()
//        TTSNotificationManager(context, player)
//    }
//
//    single {
//        val context: Context = get()
//        TTSServiceHandler(context)
//    }
//}
