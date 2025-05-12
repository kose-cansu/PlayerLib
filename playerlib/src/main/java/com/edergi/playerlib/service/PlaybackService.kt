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

class PlaybackService: MediaSessionService() {

    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        PlayerLib.instance.config.onCreated?.invoke()

        val player = ExoPlayer.Builder(this)
            .build()

        player.addListener(PlayerLib.instance.playerListener)

        setMediaNotificationProvider(PLibMediaNotificationProvider(this))

        mediaSession = MediaSession.Builder(this, player).apply {
            PlayerLib.instance.config.sessionActivity?.let { activity ->
                val intent = Intent(this@PlaybackService, activity).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }

                setSessionActivity(
                    PendingIntent.getActivity(
                        this@PlaybackService,
                        2000,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }
        }.build()

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