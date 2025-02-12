package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.capstone.bookshelf.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale


class TTSService : Service() {
    companion object {
        const val ACTION_START_TTS_SERVICE = "ACTION_START_TTS_SERVICE"
        const val CANCEL = "CANCEL"
        const val NEXT_CHAPTER = "NEXT_CHAPTER"
        const val PREVIOUS_CHAPTER = "PREVIOUS_CHAPTER"
        const val RESUME_PAUSE = "RESUME_PAUSE"
    }
    private val binder = TTSBinder()
    private var session: MediaSessionCompat? = null
    private var textToSpeech: TextToSpeech? = null
    private var textMeasurer: TextMeasurer? = null
    private var audioManager: AudioManager? = null
    private var playbackAttributes: AudioAttributes? = null
    private var backgroundColor by mutableIntStateOf(0)

    private var currentLanguage by mutableStateOf<Locale?>(null)
    private var currentVoice by mutableStateOf<Voice?>(null)
    private var currentPitch by mutableStateOf<Float?>(null)
    private var currentSpeed by mutableStateOf<Float?>(null)
    private var chapterTitle by mutableStateOf("")
    private var textIndent by mutableStateOf(false)
    private var textAlign by mutableStateOf(false)
    private var fontSize by mutableIntStateOf(0)
    private var lineSpacing by mutableIntStateOf(0)
    private var fontFamily by mutableStateOf<FontFamily?>(null)
    private var firstVisibleItemIndex by mutableIntStateOf(0)
    private var chapterParagraphsMap by mutableStateOf<Map<Int, List<String>>>(emptyMap())

    private var totalChapter by mutableIntStateOf(0)
    private var screenWidth by mutableIntStateOf(0)
    private var screenHeight by mutableIntStateOf(0)
    private var bookTitle by mutableStateOf("")

    private var oldPos by mutableIntStateOf(0)
    private var sumLength by mutableIntStateOf(0)
    private var textToSpeakNow by mutableStateOf("")
    private var flowTextLength = mutableStateListOf<Int>()
    private var isTtsInitialized by mutableStateOf(false)
    private var isForegroundService by mutableStateOf(false)
    private var currentReadingPositionInParagraph by mutableIntStateOf(0)
    private var totalParagraphs by mutableIntStateOf(0)

    private val currentParagraphIndex = MutableStateFlow(0)
    private val currentChapterIndex = MutableStateFlow(0)
    private val isSpeaking = MutableStateFlow(false)
    private val isPaused = MutableStateFlow(false)
    private val scrollTimes = MutableStateFlow(0)
    private val flagTriggerScroll = MutableStateFlow(false)
    private var bitmap : Bitmap? = null

    private lateinit var mediaButtonReceiver: MediaButtonReceiver
    private val customActionPreviousChapter = PlaybackStateCompat.CustomAction.Builder(
        PREVIOUS_CHAPTER,
        "Previous Chapter",
        R.drawable.ic_previous_chapter
    )
    private val customActionNextChapter = PlaybackStateCompat.CustomAction.Builder(
        NEXT_CHAPTER,
        "Next Chapter",
        R.drawable.ic_next_chapter
    )
    private val customActionCancel = PlaybackStateCompat.CustomAction.Builder(
        CANCEL,
        "Cancel",
        R.drawable.ic_stop
    )
    private var audioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    resumeReading()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    pauseReading()
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    pauseReading()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    pauseReading()
                }
            }
        }
    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)
        }
        override fun onPlay() {
            Log.d("debug TTS","onPlay")
            resumePausePlayback()
        }
        override fun onPause() {
            Log.d("debug TTS","onPause")
            resumePausePlayback()
        }
        override fun onStop() {
            Log.d("debug TTS","onStop")
            cancelPlayback()
        }
        override fun onSkipToNext() {
            Log.d("debug TTS","onSkipToNext")
            nextChapter()
        }
        override fun onSkipToPrevious() {
            Log.d("debug TTS","onSkipToPrevious")
            if(Build.VERSION.SDK_INT <= 27)
                resumePausePlayback()
            else
                previousChapter()
        }

        override fun onCustomAction(action: String, extras: Bundle?) {
            when (action) {
                PREVIOUS_CHAPTER -> previousChapter()
                RESUME_PAUSE -> resumePausePlayback()
                NEXT_CHAPTER -> nextChapter()
                CANCEL -> cancelPlayback()
            }
            super.onCustomAction(action, extras)
        }

        override fun onMediaButtonEvent(mediaButtonIntent: Intent?): Boolean {
            val keyEvent = mediaButtonIntent?.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            if (keyEvent != null) {
                when (keyEvent.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                        if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                            if (keyEvent.repeatCount == 1) {
                                Log.d("debug TTS","tap headset")
                            } else {
                                Log.d("debug TTS","double tap headset")
                            }
                            return true
                        }
                    }
                    KeyEvent.KEYCODE_HEADSETHOOK -> {
                        Log.d("debug TTS","headset hook")
                        return true
                    }
                }
            }
            return super.onMediaButtonEvent(mediaButtonIntent)
        }
    }
    private var audioFocusRequest : Int = 0
    private var focusRequest : AudioFocusRequest? = null
    inner class TTSBinder : Binder(){
        fun getService() = this@TTSService
        fun initializeTts() = this@TTSService.initializeTts()
        fun currentParagraphIndex() = this@TTSService.currentParagraphIndex.asStateFlow()
        fun currentChapterIndex() = this@TTSService.currentChapterIndex.asStateFlow()
        fun isSpeaking() = this@TTSService.isSpeaking.asStateFlow()
        fun isPaused() = this@TTSService.isPaused.asStateFlow()
        fun scrollTimes() = this@TTSService.scrollTimes.asStateFlow()
        fun flagTriggerScroll() = this@TTSService.flagTriggerScroll.asStateFlow()
        fun setCurrentParagraphIndex(value: Int) {
            this@TTSService.currentParagraphIndex.value = value
        }
        fun setCurrentChapterIndex(value: Int) {
            this@TTSService.currentChapterIndex.value = value
        }
        fun setIsSpeaking(value: Boolean) {
            this@TTSService.isSpeaking.value = value
        }
        fun setIsPaused(value: Boolean) {
            this@TTSService.isPaused.value = value
        }
        fun setScrollTimes(value: Int) {
            this@TTSService.scrollTimes.value = value
        }
        fun setFlagTriggerScroll(value: Boolean) {
            this@TTSService.flagTriggerScroll.value = value
        }
        suspend fun loadImage(path: String){
            this@TTSService.loadImage(path)
        }
        fun setCurrentLanguage(value: Locale?) {
            this@TTSService.currentLanguage = value
            textToSpeech?.language = value ?: Locale.getDefault()
            Log.d("debug TTS","load from set current language in TTS Service")
        }
        fun setCurrentVoice(value: Voice?) {
            this@TTSService.currentVoice = value
            textToSpeech?.voice = value ?: textToSpeech?.defaultVoice
        }
        fun setCurrentPitch(value: Float?) {
            this@TTSService.currentPitch = value
            textToSpeech?.setPitch(value?:1f)
        }
        fun setCurrentSpeed(value: Float?) {
            this@TTSService.currentSpeed = value
            textToSpeech?.setSpeechRate(value?:1f)
        }
        fun setTotalChapter(value: Int) {
            this@TTSService.totalChapter = value
        }
        fun setScreenWidth(value: Int) {
            this@TTSService.screenWidth = value
        }
        fun setScreenHeight(value: Int) {
            this@TTSService.screenHeight = value
        }
        fun setBookTitle(value: String) {
            this@TTSService.bookTitle = value
            if(isForegroundService)
                sendNotification()
        }
        fun setChapterTitle(value: String) {
            this@TTSService.chapterTitle = value
            if(isForegroundService)
                sendNotification()
        }
        fun setTextIndent(value: Boolean) {
            this@TTSService.textIndent = value
        }
        fun setTextAlign(value: Boolean) {
            this@TTSService.textAlign = value
        }
        fun setFontSize(value: Int) {
            this@TTSService.fontSize = value
        }
        fun setLineSpacing(value: Int) {
            this@TTSService.lineSpacing = value
        }
        fun setFontFamily(value: FontFamily?) {
            this@TTSService.fontFamily = value
        }
        fun setFirstVisibleItemIndex(value: Int) {
            this@TTSService.firstVisibleItemIndex = value
        }
        fun setTextMeasure(textMeasurer: TextMeasurer){
            this@TTSService.textMeasurer = textMeasurer
        }
    }
    override fun onCreate() {
        super.onCreate()
        val mediaButtonReceiverIntent = Intent(this, MediaButtonReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonReceiverIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        session = MediaSessionCompat(this, "TTS_SERVICE").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
            setCallback(mediaSessionCallback)
            isActive = true
            setMediaButtonReceiver(pendingIntent)
        }
        createNotificationChannel()
    }
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }
    override fun onDestroy() {
        isPaused.value = false
        session?.release()
        session = null
        if (isForegroundService) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            isForegroundService = false
        }
        unregisterReceiver(mediaButtonReceiver)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (session?.controller?.playbackState?.state == PlaybackStateCompat.STATE_PLAYING) {
            Log.d("debug TTS","mediaSession set PAUSED state")
//            session?.setPlaybackState(
//                PlaybackStateCompat.Builder()
//                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0.0f)
//                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build()
//            )
        } else {
            Log.d("debug TTS","mediaSession set PLAYING state")
//            mediaSession.setPlaybackState(
//                PlaybackStateCompat.Builder()
//                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
//                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build()
//            )
        }
        return START_NOT_STICKY
    }
    private suspend fun loadImage(imagePath: String)  {
        val imageUrl =
            if (imagePath == "error")
                R.mipmap.book_cover_not_available
            else
                imagePath
        val loader = ImageLoader(this)
        val request = ImageRequest.Builder(this)
            .data(imageUrl)
            .allowHardware(false)
            .build()
        val result = (loader.execute(request) as? SuccessResult)?.drawable
        bitmap =  result?.toBitmap()
        bitmap?.let { bitmap ->
            Palette.from(bitmap).generate { palette ->
                val color = palette?.vibrantSwatch?.rgb
                if (color != null) {
                    backgroundColor = color
                }
            }
        }
    }
    fun previousChapter(){
        moveToPreviousChapterOrStop()
        sendNotification()
    }
    fun previousParagraph(){
        playPreviousParagraphOrChapter()
        sendNotification()
    }
    fun resumePausePlayback(){
        if(isPaused.value){
            resumeReading()
            isPaused.value = false
            sendNotification()
            updatePlaybackState()
        } else {
            pauseReading()
            isPaused.value = true
            updatePlaybackState()
            sendNotification()
        }
    }
    fun nextParagraph(){
        playNextParagraphOrChapter()
        sendNotification()
    }
    fun nextChapter(){
        moveToNextChapterOrStop()
        sendNotification()
    }
    fun cancelPlayback(){
        stopReading()
    }
    fun startPlayback(){
        startForegroundService()
        startReading()
        isPaused.value = false
        session?.isActive = true
        updatePlaybackState()
        sendNotification()
    }
    private fun startForegroundService() {
        if (!isForegroundService) {
            val notification = buildNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(1, notification)
            }
            isForegroundService = true
        } else {
            sendNotification()
        }
    }

    private fun stopForegroundService() {
        if (isForegroundService) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            isForegroundService = false
        }
    }
    private fun sendNotification() {
        if (session == null) {
            return
        }
        val notification = buildNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(1, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "TTS_CHANNEL_ID",
            "TTS Playback Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    private fun updatePlaybackState() {
        if (session == null) return
        val state = when {
            isSpeaking.value && !isPaused.value -> PlaybackStateCompat.STATE_PLAYING
            isPaused.value -> PlaybackStateCompat.STATE_PAUSED
            else -> PlaybackStateCompat.STATE_STOPPED
        }
        val currentPositionMs = currentParagraphIndex.value * 1000L + 1000L
        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setState(state, currentPositionMs, 0f)
            .setBufferedPosition(currentPositionMs)
            .addCustomAction(customActionPreviousChapter.build())
            .addCustomAction(customActionNextChapter.build())
            .addCustomAction(customActionCancel.build())
            .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
        session?.setPlaybackState(playbackStateBuilder.build()) // Update the MediaSession
    }
    private fun buildNotification(): Notification {
        if (session == null) {
            return NotificationCompat.Builder(this, "TTS_CHANNEL_ID").build()
        }
        updatePlaybackState()
        val builder: NotificationCompat.Builder
        val durationMs = totalParagraphs * 1000L
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, bookTitle)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, chapterTitle)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val style = MediaStyle()
                .setMediaSession(session?.sessionToken)
            builder = NotificationCompat.Builder(this, "TTS_CHANNEL_ID")
                .setStyle(style)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(bitmap)
                .setContentTitle(bookTitle)
                .setContentText(chapterTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSilent(true)
                .setOngoing(!isPaused.value)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(backgroundColor)
                .setColorized(true)
        } else {
            val style = MediaStyle()
                .setShowActionsInCompactView(0, 2)
                .setMediaSession(session?.sessionToken)
            builder = NotificationCompat.Builder(this, "TTS_CHANNEL_ID")
                .setStyle(style)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(bitmap)
                .setContentTitle(bookTitle)
                .setContentText(chapterTitle)
                .addAction(R.drawable.ic_stop, "Stop", createCancelIntent())
                .addAction(R.drawable.ic_previous_chapter, null, createPreviousChapterIntent())
                .addAction(
                    if (isPaused.value) R.drawable.ic_play else R.drawable.ic_pause,
                    null,
                    createResumePauseIntent()
                )
                .addAction(R.drawable.ic_next_chapter, null, createNextChapterIntent())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSilent(true)
                .setOngoing(!isPaused.value)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(backgroundColor)
                .setColorized(true)
        }
        session?.setMetadata(metadata)
        return builder.build()
    }
    private fun createPreviousChapterIntent(): PendingIntent {
        val previousChapterIntent = Intent(this, TTSService::class.java).apply {
            action = PREVIOUS_CHAPTER
        }
        return PendingIntent.getService(this, 0, previousChapterIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private fun createResumePauseIntent(): PendingIntent {
        val resumePauseIntent = Intent(this, TTSService::class.java).apply {
            action = RESUME_PAUSE
        }
        return PendingIntent.getService(this, 1, resumePauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private fun createCancelIntent(): PendingIntent {
        val cancelIntent = Intent(this, TTSService::class.java).apply {
            action = CANCEL
        }
        return PendingIntent.getService(this, 2, cancelIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private fun createNextChapterIntent(): PendingIntent {
        val nextChapterIntent = Intent(this, TTSService::class.java).apply {
            action = NEXT_CHAPTER
        }
        return PendingIntent.getService(this, 3, nextChapterIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private fun initializeTts() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = currentLanguage?: Locale.getDefault()
                textToSpeech?.voice = currentVoice?: textToSpeech?.defaultVoice
                textToSpeech?.setSpeechRate( currentSpeed?: 1f)
                textToSpeech?.setPitch( currentPitch?: 1f)
                isTtsInitialized = true
            }
        }
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                flagTriggerScroll.value = false
            }
            override fun onDone(utteranceId: String?) {
                playNextParagraphOrChapter()
                flagTriggerScroll.value = false
                currentReadingPositionInParagraph = 0
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {}
            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                super.onRangeStart(utteranceId, start, end, frame)
                if(isSpeaking.value) {
                    val currentPos = textToSpeakNow.substring(0, end).length
                    flagTriggerScroll.value = false
                    currentReadingPositionInParagraph = oldPos + currentPos
                    if (flowTextLength.size > 1) {
                        if (oldPos + currentPos > sumLength) {
                            flagTriggerScroll.value = true
                            scrollTimes.value += 1
                            sumLength += flowTextLength[scrollTimes.value]
                            currentReadingPositionInParagraph = oldPos + currentPos
                        }
                    }
                }
            }
        })
        audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        playbackAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            .setAudioAttributes(playbackAttributes!!)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()
    }
    fun setChapterParagraphs(chapterParagraphs: Map<Int, List<String>>) {
        chapterParagraphsMap = chapterParagraphs
    }
    private fun startReading() {
        if (!isTtsInitialized) {
            return
        }
        if (chapterParagraphsMap.isEmpty()) {
            return
        }
        audioFocusRequest = audioManager!!.requestAudioFocus(focusRequest!!)
        isSpeaking.value = true
        isPaused.value = false
        currentReadingPositionInParagraph = 0
        currentParagraphIndex.value = firstVisibleItemIndex
        if(audioFocusRequest == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            startSpeakCurrentParagraph()
        }
    }
    private fun pauseReading() {
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
        }
    }
    private fun resumeReading() {
        startSpeakCurrentParagraph()
    }
    private fun stopReading() {
        textToSpeech?.stop()
        currentParagraphIndex.value = 0
        currentReadingPositionInParagraph = 0
        isSpeaking.value = false
        isPaused.value = false
        session?.isActive = false
        stopForegroundService()
        updatePlaybackState()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
        audioFocusRequest = audioManager?.abandonAudioFocusRequest(focusRequest!!)!!
    }
    private fun startSpeakCurrentParagraph() {
        if (currentChapterIndex.value != -1) {
            val currentChapterParagraphs = chapterParagraphsMap[currentChapterIndex.value]
            if (currentChapterParagraphs != null && currentParagraphIndex.value < currentChapterParagraphs.size) {
                totalParagraphs = currentChapterParagraphs.size
                val text = currentChapterParagraphs[currentParagraphIndex.value]
                textToSpeakNow = text.substring(currentReadingPositionInParagraph)
                oldPos = text.length - textToSpeakNow.length
                scrollTimes.value = 0
                flowTextLength = processTextLength(
                    text = text,
                    maxWidth = screenWidth,
                    maxHeight = screenHeight,
                    textStyle = TextStyle(
                        textIndent = if(textIndent)
                            TextIndent(firstLine = (fontSize * 2).sp)
                        else
                            TextIndent.None,
                        textAlign = if(textAlign) TextAlign.Justify else TextAlign.Left,
                        fontSize = fontSize.sp,
                        fontFamily = fontFamily,
                        lineBreak = LineBreak.Paragraph,
                        lineHeight = (fontSize + lineSpacing).sp
                    ),
                    textMeasurer = textMeasurer!!
                )
                sumLength = flowTextLength[scrollTimes.value]
                sendNotification()
                textToSpeech?.speak(textToSpeakNow, TextToSpeech.QUEUE_FLUSH, null, "paragraph_${currentChapterIndex.value}_${currentParagraphIndex.value}")
            } else {
                moveToNextChapterOrStop()
            }
        }
    }
    fun playNextParagraphOrChapter() {
        if (currentChapterIndex.value != -1) {
            val currentChapterParagraphs = chapterParagraphsMap[currentChapterIndex.value]
            if (currentChapterParagraphs != null && currentParagraphIndex.value < currentChapterParagraphs.size - 1) {
                currentParagraphIndex.value += 1
                currentReadingPositionInParagraph = 0
                if(isSpeaking.value && !isPaused.value)
                    startSpeakCurrentParagraph()
            } else {
                moveToNextChapterOrStop()
            }
        }
    }
    private fun playPreviousParagraphOrChapter() {
        if (currentChapterIndex.value != -1) {
            val currentChapterParagraphs = chapterParagraphsMap[currentChapterIndex.value]
            if (currentChapterParagraphs != null && currentParagraphIndex.value - 1 >= 0) {
                currentParagraphIndex.value -= 1
                currentReadingPositionInParagraph = 0
                if(isSpeaking.value && !isPaused.value)
                    startSpeakCurrentParagraph()
            } else {
                moveToPreviousChapterOrStop()
            }
        }
    }
    private fun moveToNextChapterOrStop() {
        if (currentChapterIndex.value + 1 <= totalChapter) {
            currentReadingPositionInParagraph = 0
            currentParagraphIndex.value = 0
            currentChapterIndex.value += 1
            if(isSpeaking.value && !isPaused.value)
                startSpeakCurrentParagraph()
        } else {
            stopReading()
        }
    }
    private fun moveToPreviousChapterOrStop() {
        if (currentChapterIndex.value -1 >= 0) {
            currentReadingPositionInParagraph = 0
            currentParagraphIndex.value = 0
            currentChapterIndex.value -= 1
            if(isSpeaking.value && !isPaused.value)
                startSpeakCurrentParagraph()
        } else {
            stopReading()
        }
    }
    fun shutdownTts() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        isTtsInitialized = false
    }
    private fun processTextLength(
        text: String,
        maxWidth: Int,
        maxHeight: Int,
        textStyle: TextStyle,
        textMeasurer: TextMeasurer,
    ): SnapshotStateList<Int> {
        var remainingText = text
        val subStringLength = mutableStateListOf<Int>()
        while (remainingText.isNotEmpty()) {
            val measuredLayoutResult = textMeasurer.measure(
                text = remainingText,
                style = textStyle,
                overflow = TextOverflow.Ellipsis,
                constraints = Constraints(
                    maxWidth = maxWidth,
                    maxHeight = maxHeight
                ),
            )
            if (measuredLayoutResult.hasVisualOverflow) {
                val lastVisibleCharacterIndex = measuredLayoutResult.getLineEnd(
                    lineIndex = measuredLayoutResult.lineCount - 1,
                    visibleEnd = true
                )
                val endIndex = minOf(lastVisibleCharacterIndex, remainingText.length)
                val endSubString = remainingText.substring(0, endIndex)
                subStringLength.add(endSubString.trim().length)
                remainingText = remainingText.substring(endIndex)
            } else {
                subStringLength.add(remainingText.trim().length)
                remainingText = ""
            }
        }
        return subStringLength
    }
}