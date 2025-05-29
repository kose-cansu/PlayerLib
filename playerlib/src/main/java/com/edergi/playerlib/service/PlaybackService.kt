package com.edergi.playerlib.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.edergi.playerlib.PlayerLib
import com.edergi.playerlib.notification.PLibMediaNotificationProvider
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class PlaybackService: MediaSessionService() {

    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        PlayerLib.instance.config.onCreated?.invoke()

        val player = ExoPlayer.Builder(applicationContext).build()
        player.addListener(PlayerLib.instance.playerListener)

        setMediaNotificationProvider(PLibMediaNotificationProvider(applicationContext))

        val sessionBuilder = MediaSession.Builder(applicationContext, player)

        PlayerLib.instance.config.sessionActivity?.let { activity ->
            val intent = Intent(applicationContext, activity).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            sessionBuilder.setSessionActivity(
                PendingIntent.getActivity(
                    applicationContext,
                    2000,
                    intent,
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

                val emptyMediaItems = MediaSession.MediaItemsWithStartPosition(
                    listOf(),
                    0,
                    0L
                )

                return Futures.immediateFuture(emptyMediaItems)
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