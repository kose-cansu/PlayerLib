package com.edergi.playerlib.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C.USAGE_MEDIA
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.edergi.playerlib.PlayerLib
import com.edergi.playerlib.notification.PLibMediaNotificationProvider
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class PlaybackService: MediaSessionService() {

    private var mediaSession: MediaSession? = null

    object Commands {
        const val CATEGORY_NEXT = "edergi.CATEGORY_NEXT"
        const val CATEGORY_PREV = "edergi.CATEGORY_PREV"
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        PlayerLib.instance.config.onCreated?.invoke()

        val player = ExoPlayer.Builder(applicationContext)
            .setHandleAudioBecomingNoisy(true)
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(USAGE_MEDIA)
            .build()

        player.setAudioAttributes(audioAttributes, true)
        player.addListener(PlayerLib.instance.playerListener)

        setMediaNotificationProvider(PLibMediaNotificationProvider(applicationContext))

        val sessionBuilder = MediaSession.Builder(applicationContext, player)

        PlayerLib.instance.config.sessionActivity?.let { activity ->
            val intent = Intent(applicationContext, activity).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            sessionBuilder.setSessionActivity(
                PendingIntent.getActivity(
                    applicationContext, 2000, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }

        sessionBuilder.setCallback(object : MediaSession.Callback {
            override fun onPlaybackResumption(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
                session.player.play()
                val empty = MediaSession.MediaItemsWithStartPosition(listOf(), 0, 0L)
                return Futures.immediateFuture(empty)
            }

            override fun onCustomCommand(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
                customCommand: SessionCommand,
                args: Bundle
            ): ListenableFuture<SessionResult> {
                when (customCommand.customAction) {
                    Commands.CATEGORY_NEXT -> {
                        PlayerLib.instance.config.onCategoryNextCommand?.invoke()
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                    Commands.CATEGORY_PREV -> {
                        PlayerLib.instance.config.onCategoryPrevCommand?.invoke()
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                }
                return super.onCustomCommand(session, controller, customCommand, args)
            }
        })

        mediaSession = sessionBuilder.build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        PlayerLib.instance.config.onTaskRemoved?.invoke()
        val player = mediaSession?.player!!
        if (!player.playWhenReady
            || player.mediaItemCount == 0
            || player.playbackState == Player.STATE_ENDED) {
            mediaSession?.player?.removeListener(PlayerLib.instance.playerListener)
            stopSelf()
        }
    }

    override fun onDestroy() {
        PlayerLib.instance.config.onDestroy?.invoke()
        mediaSession?.run {
            player.removeListener(PlayerLib.instance.playerListener)
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        PlayerLib.instance.config.onGetSession?.invoke(mediaSession)
        return mediaSession
    }

}