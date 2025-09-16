package com.edergi.playerlib.notification

import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import com.google.common.collect.ImmutableList
import com.edergi.playerlib.PlayerLib
import com.edergi.playerlib.service.PlaybackService

@UnstableApi
class PLibMediaNotificationProvider(
    context: Context
): DefaultMediaNotificationProvider(context) {

    override fun getNotificationContentText(metadata: MediaMetadata): CharSequence {
        return "Öne Çıkan Haberler"
    }

    override fun getNotificationContentTitle(metadata: MediaMetadata): CharSequence? {
        return metadata.title
    }

    override fun addNotificationActions(
        mediaSession: MediaSession,
        mediaButtons: ImmutableList<CommandButton>,
        builder: NotificationCompat.Builder,
        actionFactory: MediaNotification.ActionFactory
    ): IntArray {

        PlayerLib.instance.config.smallIcon?.let { builder.setSmallIcon(it) }
        builder.setLargeIcon(PlayerLib.instance.config.largeIcon)

        val categoryPrev = CommandButton.Builder()
            .setSessionCommand(SessionCommand(PlaybackService.Commands.CATEGORY_PREV, Bundle.EMPTY))
            .setEnabled(true)
            .setDisplayName(PlayerLib.Command.FastForward.DISPLAY_NAME)
            .setIconResId(androidx.media3.session.R.drawable.media3_icon_skip_forward_15)
            .build()

        val categoryNext = CommandButton.Builder()
            .setSessionCommand(SessionCommand(PlaybackService.Commands.CATEGORY_NEXT, Bundle.EMPTY))
            .setEnabled(true)
            .setDisplayName(PlayerLib.Command.Rewind.DISPLAY_NAME)
            .setIconResId(androidx.media3.session.R.drawable.media3_icon_skip_back_5)
            .build()

        val filteredDefaults = mediaButtons.filterNot { def ->
            def.playerCommand == Player.COMMAND_SEEK_TO_PREVIOUS ||
                    def.playerCommand == Player.COMMAND_SEEK_TO_NEXT ||
                    def.playerCommand == Player.COMMAND_SEEK_FORWARD ||
                    def.playerCommand == Player.COMMAND_SEEK_BACK
        }

        val buttons = ImmutableList.builder<CommandButton>().apply {
            add(categoryPrev)
            filteredDefaults.forEach { add(it) }
            add(categoryNext)
        }.build()

        return super.addNotificationActions(
            mediaSession,
            buttons,
            builder,
            actionFactory
        )
    }
}