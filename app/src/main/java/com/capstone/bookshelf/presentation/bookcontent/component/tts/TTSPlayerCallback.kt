package com.capstone.bookshelf.presentation.bookcontent.component.tts

import android.os.Bundle
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.component.tts.PlaybackService.Companion.ACTION_NEXT_CHAPTER
import com.capstone.bookshelf.presentation.bookcontent.component.tts.PlaybackService.Companion.ACTION_PREVIOUS_CHAPTER
import com.capstone.bookshelf.presentation.bookcontent.component.tts.PlaybackService.Companion.ACTION_STOP
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

@UnstableApi
class TTSPlayerCallback : MediaSession.Callback{
    private val customCommandStop = SessionCommand(ACTION_STOP, Bundle.EMPTY)
    private val customCommandNextChapter = SessionCommand(ACTION_NEXT_CHAPTER, Bundle.EMPTY)
    private val customCommandPreviousChapter = SessionCommand(ACTION_PREVIOUS_CHAPTER, Bundle.EMPTY)
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val sessionCommands =
            MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(customCommandStop)
                .add(customCommandPreviousChapter)
                .add(customCommandNextChapter)
                .build()
        val playerCommands =
            MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                .remove(Player.COMMAND_SEEK_TO_PREVIOUS)
                .remove(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                .remove(Player.COMMAND_SEEK_TO_NEXT)
                .remove(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                .build()
        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
            .setCustomLayout(
                ImmutableList.of(
                    createPreviousChapterButton(customCommandPreviousChapter),
                    createNextChapterButton(customCommandNextChapter),
                    createStopButton(customCommandStop),
                )
            )
            .setAvailablePlayerCommands(playerCommands)
            .setAvailableSessionCommands(sessionCommands)
            .build()
    }
    private fun createStopButton(customCommandFavorites: SessionCommand): CommandButton {
        return CommandButton.Builder()
            .setDisplayName("Save to favorites")
            .setIconResId(R.drawable.ic_stop)
            .setSessionCommand(customCommandFavorites)
            .build()
    }
    private fun createPreviousChapterButton(customCommand: SessionCommand): CommandButton {
        return CommandButton.Builder()
            .setDisplayName("previous chapter")
            .setIconResId(R.drawable.ic_previous_chapter)
            .setSessionCommand(customCommand)
            .build()
    }
    private fun createNextChapterButton(customCommand: SessionCommand): CommandButton {
        return CommandButton.Builder()
            .setDisplayName("next chapter")
            .setIconResId(R.drawable.ic_next_chapter)
            .setSessionCommand(customCommand)
            .build()
    }
    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        when (customCommand.customAction) {
            ACTION_STOP -> {
                Log.d("TTSPlayerCallback","action stop")
                session.player.stop()
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            ACTION_PREVIOUS_CHAPTER -> {
                Log.d("TTSPlayerCallback","action previous chapter")
                session.player.seekBack()
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            ACTION_NEXT_CHAPTER -> {
                Log.d("TTSPlayerCallback","action next chapter")
                session.player.seekForward()
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            else -> {
                Log.d("TTSPlayerCallback","unknown action")
            }
        }
        return super.onCustomCommand(session, controller, customCommand, args)
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>
    ): ListenableFuture<List<MediaItem>> {
        mediaSession.player.addMediaItems(mediaItems)
        return Futures.immediateFuture(mediaItems)
    }
    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        mediaSession.player.setMediaItems(mediaItems, startIndex, startPositionMs)
        return Futures.immediateFuture(
            MediaSession.MediaItemsWithStartPosition(
                mediaItems,
                startIndex,
                startPositionMs
            )
        )
    }
}
