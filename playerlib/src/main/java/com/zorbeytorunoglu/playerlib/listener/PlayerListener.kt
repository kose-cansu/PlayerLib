package com.edergi.playerlib.listener

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.edergi.playerlib.PlayerLib

class PlayerListener(
    private val pLibConfig: PlayerLib.Config
): Player.Listener {

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        pLibConfig.onPlayerEvent?.invoke(PlayerEvent.PlayerError(error))
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        pLibConfig.onPlayerEvent?.invoke(PlayerEvent.PlaybackStateChanged(playbackState))
    }

    override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
        super.onDeviceVolumeChanged(volume, muted)
        pLibConfig.onPlayerEvent?.invoke(PlayerEvent.DeviceVolumeChanged(volume, muted))
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        pLibConfig.onPlayerEvent?.invoke(PlayerEvent.IsPlayingChanged(isPlaying))
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        pLibConfig.onPlayerEvent?.invoke(PlayerEvent.MediaItemTransition(mediaItem, reason))
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        pLibConfig.onPlayerEvent?.invoke(PlayerEvent.MediaMetadataChanged(mediaMetadata))
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        super.onPlayerErrorChanged(error)
        pLibConfig.onPlayerEvent?.invoke(PlayerEvent.PlayerErrorChanged(error))
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        super.onPositionDiscontinuity(oldPosition, newPosition, reason)
        pLibConfig.onPlayerEvent?.invoke(
            PlayerEvent.OnPositionDiscontinuity(
                oldPosition,
                newPosition,
                reason
            )
        )
    }

}

sealed interface PlayerEvent {
    data class PlayerError(val exception: PlaybackException): PlayerEvent
    data class PlaybackStateChanged(val state: Int): PlayerEvent
    data class DeviceVolumeChanged(val volume: Int, val muted: Boolean): PlayerEvent
    data class IsPlayingChanged(val isPlaying: Boolean): PlayerEvent
    data class MediaItemTransition(val mediaItem: MediaItem?, val reason: Int): PlayerEvent
    data class MediaMetadataChanged(val mediaMetadata: MediaMetadata): PlayerEvent
    data class PlayerErrorChanged(val error: PlaybackException?): PlayerEvent
    data class OnPositionDiscontinuity(val oldPosition: Player.PositionInfo, val newPosition: Player.PositionInfo, val reason: Int):
        PlayerEvent
}