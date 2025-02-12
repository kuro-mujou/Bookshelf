package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.media.MediaPlayer
import android.util.Log

class MediaPlayerTest : MediaPlayer() {
    override fun start() {
        Log.d("debug","start")
        super.start()
    }
    override fun pause() {
        Log.d("debug","pause")
        super.pause()
    }
    override fun stop() {
        Log.d("debug","stop")
        super.stop()
    }
    override fun release() {
        Log.d("debug","release")
        super.release()
    }
    override fun setDataSource(path: String) {
        Log.d("debug","setDataSource")
        super.setDataSource(path)
    }
    override fun prepare() {
        Log.d("debug","prepare")
    }
}