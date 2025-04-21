package com.edergi.playerlib.model

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

data class Track(
    val id: String,
    val title: String,
    val description: String? = null,
    val m3u8Url: String,
    val bundleKey: String? = null,
    val bundleValue: String? = null
) {

    companion object {

        const val TRACK_BUNDLE_KEY = "PLAYERLIB_TRACK"
        const val TRACKS_BUNDLE_KEY = "PLAYERLIB_TRACKS"

        fun from(serializedTrack: String): Track? {
            val parts = serializedTrack.split("|")
            return Track(
                id = parts.getOrNull(0) ?: return null,
                title = parts.getOrNull(1) ?: return null,
                description = parts.getOrNull(2),
                m3u8Url = parts.getOrNull(3) ?: return null,
                bundleKey = parts.getOrNull(4),
                bundleValue = parts.getOrNull(5)
            )
        }
    }

}

internal fun Track.toMediaItem(): MediaItem {
    val metadata = MediaMetadata.Builder().apply {
        setTitle(title)
        setDescription(description)
    }.build()
    return MediaItem.Builder()
        .setUri(m3u8Url)
        .setMediaMetadata(metadata)
        .build()
}

fun Track.serialize(): String =
    listOf(
        id,
        title,
        description ?: "",
        m3u8Url,
        bundleKey ?: "",
        bundleValue ?: ""
    ).joinToString("|")