package com.edergi.playerlib.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.edergi.playerlib.PlayerLib

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

        val fastForward = CommandButton.Builder()
            .setPlayerCommand(PlayerLib.Command.FastForward.COMMAND)
            .setEnabled(true)
            .setDisplayName(PlayerLib.Command.FastForward.DISPLAY_NAME)
            .setIconResId(androidx.media3.session.R.drawable.media3_icon_skip_forward_15)
            .build()

        val rewind = CommandButton.Builder()
            .setPlayerCommand(PlayerLib.Command.Rewind.COMMAND)
            .setEnabled(true)
            .setDisplayName(PlayerLib.Command.Rewind.DISPLAY_NAME)
            .setIconResId(androidx.media3.session.R.drawable.media3_icon_skip_back_5)
            .build()

        val buttons = ImmutableList.builder<CommandButton>().apply {
            add(rewind)
            mediaButtons.forEach { defBut -> add(defBut) }
            add(fastForward)
        }.build()

        return super.addNotificationActions(
            mediaSession,
            buttons,
            builder,
            actionFactory
        )
    }

}