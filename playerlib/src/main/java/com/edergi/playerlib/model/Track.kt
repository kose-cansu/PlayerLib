package com.edergi.playerlib.model

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

data class Track(
    val id: String,
    val title: String,
    val description: String? = null,
    val m3u8Url: String,
    val bundleKey: String? = null,
    val bundleValue: String? = null,
    val paperId: String
)

internal fun Track.toMediaItem(): MediaItem {
    val metadata = MediaMetadata.Builder().apply {
        setTitle(title)
        setAlbumArtist(paperId)
        setDescription(description)
    }.build()
    return MediaItem.Builder()
        .setUri(m3u8Url)
        .setMediaId(id)
        .setMediaMetadata(metadata)
        .build()
}