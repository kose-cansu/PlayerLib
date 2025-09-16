package com.edergi.playerlib

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import com.edergi.playerlib.listener.PlayerEvent
import com.edergi.playerlib.listener.PlayerListener
import com.edergi.playerlib.model.PlaybackDuration
import com.edergi.playerlib.model.Track
import com.edergi.playerlib.model.toMediaItem
import com.edergi.playerlib.service.PlaybackService

private const val DEFAULT_CHANNEL_ID = "player_lib_channel"
private const val DEFAULT_NOTIFICATION_ID = 2000
private const val DEFAULT_CHANNEL_NAME = "Player Lib Channel"

class PlayerLib(internal val config: Config) {

    private val context
        get() = config.context

    private val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))

    private var mediaController: MediaController? = null

    private val pendingActions = mutableListOf<(MediaController) -> Unit>()

    internal val playerListener = PlayerListener(config)

    private var positionUpdateTracker: PositionUpdateTracker? = null

    init {
        val controllableFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllableFuture.addListener({
            mediaController = if (controllableFuture.isDone) {
                controllableFuture.get().also {
                    it.addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            config.onPlayerEvent?.invoke(PlayerEvent.IsPlayingChanged(isPlaying))
                        }
                    })
                    synchronized(pendingActions) {
                        pendingActions.forEach { action -> action(it) }
                        pendingActions.clear()
                    }
                    startPositionUpdateTracker(it)
                }
            } else {
                null
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun startPositionUpdateTracker(controller: MediaController) {
        positionUpdateTracker?.stop()
        if (config.periodicPositionUpdateEnabled == true) {
            positionUpdateTracker = PositionUpdateTracker(
                updateIntervalMs = config.positionUpdateDelay,
                mediaController = controller,
                onUpdate = config.onPlaybackPositionUpdate
            ).also { it.start() }
        }
    }

    private fun runWhenReady(action: (MediaController) -> Unit) {
        val controller = mediaController
        if (controller != null) {
            action(controller)
        } else {
            synchronized(pendingActions) {
                pendingActions.add(action)
            }
        }
    }

    fun play(track: Track) {
        runWhenReady { controller ->
            controller.stop()
            controller.setMediaItem(track.toMediaItem())
            controller.prepare()
            controller.playWhenReady = true
        }
    }

    fun play(tracks: List<Track>) {
        runWhenReady { controller ->
            controller.stop()
            controller.setMediaItems(tracks.map(Track::toMediaItem))
            controller.prepare()
            controller.playWhenReady = true
        }
    }

    fun pause() {
        runWhenReady { it.pause() }
    }

    fun seekToNextMediaItem() {
        runWhenReady { it.seekToNextMediaItem() }
    }

    fun seekToNext() {
        runWhenReady { it.seekToNext() }
    }

    fun seekToPrevious() {
        runWhenReady { it.seekToPrevious() }
    }

    fun seekToPreviousMediaItem() {
        runWhenReady { it.seekToPreviousMediaItem() }
    }

    fun seekTo(position: Long) {
        runWhenReady { it.seekTo(position) }
    }

    fun seekToIndex(mediaIndex: Int, position: Long = 0L) {
        runWhenReady { it.seekTo(mediaIndex, position) }
    }

    fun addTracks(tracks: List<Track>) {
        runWhenReady { it.addMediaItems(tracks.map(Track::toMediaItem)) }
    }

    fun addTrack(track: Track) {
        runWhenReady { it.addMediaItem(track.toMediaItem()) }
    }

    fun release() {
        runWhenReady { it.release() }
        positionUpdateTracker?.stop()
        positionUpdateTracker = null
    }

    fun stop() {
        runWhenReady { it.stop() }
    }

    fun removeTracks(fromIndex: Int, toIndex: Int) {
        runWhenReady { it.removeMediaItems(fromIndex, toIndex) }
    }

    fun clearMediaItems() {
        runWhenReady { it.clearMediaItems() }
    }

    fun addPlayerListener(listener: Player.Listener) {
        runWhenReady { it.addListener(listener) }
    }

    fun removePlayerListener(listener: Player.Listener) {
        runWhenReady { it.removeListener(listener) }
    }

    fun moveMediaItem(fromIndex: Int, toIndex: Int) {
        runWhenReady { it.moveMediaItem(fromIndex, toIndex) }
    }

    fun moveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) {
        runWhenReady { it.moveMediaItems(fromIndex, toIndex, newIndex) }
    }

    fun setMediaItems(mediaItems: List<MediaItem>) {
        runWhenReady { it.setMediaItems(mediaItems) }
    }

    fun setMediaItem(mediaItem: MediaItem) {
        runWhenReady { it.setMediaItem(mediaItem) }
    }

    fun setMediaItems(mediaItems: List<MediaItem>, startIndex: Int, startPositionMs: Long) {
        runWhenReady { it.setMediaItems(mediaItems, startIndex, startPositionMs) }
    }

    fun play() {
        runWhenReady { it.play() }
    }

    fun setPlayWhenReady(playWhenReady: Boolean) {
        runWhenReady { it.playWhenReady = playWhenReady }
    }

    fun hasPreviousMediaItem(): Boolean = mediaController?.hasPreviousMediaItem() == true

    fun hasNextMediaItem(): Boolean = mediaController?.hasNextMediaItem() == true

    fun setPlaybackSpeed(speed: Float) {
        runWhenReady { it.setPlaybackSpeed(speed) }
    }

    class Config private constructor(
        internal val context: Context,
        internal val sessionActivity: Class<out Activity>?,
        internal val largeIcon: Bitmap?,
        internal val smallIcon: IconCompat?,
        internal val positionUpdateDelay: Long,
        internal val redirectionIntentBundle: Bundle?,
        internal val channelId: String,
        internal val channelName: String,
        internal val notificationId: Int,
        internal val smallIconResourceId: Int?,
        internal val periodicPositionUpdateEnabled: Boolean?,
        internal val setUseRewindAction: Boolean,
        internal val setUseFastForwardAction: Boolean,
        internal val setUseRewindActionInCompactView: Boolean,
        internal val setUseFastForwardActionInCompactView: Boolean,
        internal val setUseChronometer: Boolean,
        internal val setUsePlayPauseActions: Boolean,
        internal val setUseNextAction: Boolean,
        internal val setUsePreviousAction: Boolean,
        internal val setUseStopAction: Boolean,
        internal val setColorized: Boolean?,
        internal val setColor: Int?,
        internal val setShowPlayButtonIfPlaybackIsSuppressed: Boolean?,
        internal val setShouldStayAwake: Boolean,
        internal val onCreated: (() -> Unit)?,
        internal val onDestroy: (() -> Unit)?,
        internal val onPlaybackPositionUpdate: ((PlaybackDuration?, MediaItem?) -> Unit)?,
        internal val onTaskRemoved: (() -> Unit)?,
        internal val onGetSession: ((MediaSession?) -> Unit)?,
        internal val onStartCommand: (() -> Unit)?,
        internal val onPlayerEvent: ((PlayerEvent) -> Unit)?,
        internal val onAudioFocusLoss: (() -> Unit)?,
        internal val onAudioFocusGain: (() -> Unit)?,
        internal val onAudioFocusLossTransient: (() -> Unit)?,
        internal val onAudioFocusLossTransientCanDuck: (() -> Unit)?,
        internal val onCategoryNextCommand: (() -> Unit)?,
        internal val onCategoryPrevCommand: (() -> Unit)?
    ) {
        class Builder {
            private var context: Context? = null
            private var sessionActivity: Class<out Activity>? = null
            private var largeIcon: Bitmap? = null
            private var smallIcon: IconCompat? = null
            private var positionUpdateDelay: Long = 1000L
            private var redirectionIntentBundle: Bundle? = null
            private var channelId: String = DEFAULT_CHANNEL_ID
            private var channelName: String = DEFAULT_CHANNEL_NAME
            private var notificationId: Int = DEFAULT_NOTIFICATION_ID
            private var smallIconResourceId: Int? = null
            private var periodicPositionUpdateEnabled: Boolean? = null
            private var useRewindAction: Boolean = true
            private var rewindActionInCompactView: Boolean = true
            private var useFastForwardAction: Boolean = true
            private var useFastForwardActionInCompactView: Boolean = true
            private var useChronometer: Boolean = true
            private var usePlayPauseActions: Boolean = true
            private var useNextAction: Boolean = true
            private var usePreviousAction: Boolean = true
            private var useStopAction: Boolean = true
            private var colorized: Boolean? = null
            private var color: Int? = null
            private var showPlayButtonIfPlaybackIsSuppressed: Boolean? = null
            private var shouldStayAwake: Boolean = true
            private var onCreated: (() -> Unit)? = null
            private var onDestroy: (() -> Unit)? = null
            private var onPlaybackPositionUpdate: ((PlaybackDuration?, MediaItem?) -> Unit)? = null
            private var onTaskRemoved: (() -> Unit)? = null
            private var onGetSession: ((MediaSession?) -> Unit)? = null
            private var onStartCommand: (() -> Unit)? = null
            private var onPlayerEvent: ((PlayerEvent) -> Unit)? = null
            private var onAudioFocusLoss: (() -> Unit)? = null
            private var onAudioFocusGain: (() -> Unit)? = null
            private var onAudioFocusLossTransient: (() -> Unit)? = null
            private var onAudioFocusLossTransientCanDuck: (() -> Unit)? = null
            private var onCategoryNextCommand: (() -> Unit)? = null
            private var onCategoryPrevCommand: (() -> Unit)? = null

            fun setContext(context: Context) = apply {
                this.context = context.applicationContext
            }

            fun setSessionActivity(activityClass: Class<out Activity>) {
                this.sessionActivity = activityClass
            }

            fun setLargeIcon(bitmap: Bitmap) {
                this.largeIcon = bitmap
            }

            fun setSmallIcon(icon: IconCompat) {
                this.smallIcon = icon
            }

            fun setPositionUpdateDelay(delay: Long) {
                this.positionUpdateDelay = delay
            }

            fun setShouldStayAwake(shouldStayAwake: Boolean) = apply {
                this.shouldStayAwake = shouldStayAwake
            }

            fun setChannelId(channelId: String) = apply {
                this.channelId = channelId
            }

            fun setChannelName(channelName: String) = apply {
                this.channelName = channelName
            }

            fun setNotificationId(notificationId: Int) = apply {
                this.notificationId = notificationId
            }

            fun setOnDestroyed(onDestroy: () -> Unit) = apply {
                this.onDestroy = onDestroy
            }

            fun setOnTaskRemoved(onTaskRemoved: () -> Unit) = apply {
                this.onTaskRemoved = onTaskRemoved
            }

            fun setOnCreated(onCreated: () -> Unit) = apply {
                this.onCreated = onCreated
            }

            fun setOnGetSession(onGetSession: (MediaSession?) -> Unit) = apply {
                this.onGetSession = onGetSession
            }

            fun setOnStartCommand(onStartCommand: () -> Unit) = apply {
                this.onStartCommand = onStartCommand
            }

            fun setOnPlayerEvent(onPlayerEvent: (PlayerEvent) -> Unit) = apply {
                this.onPlayerEvent = onPlayerEvent
            }

            fun setOnPlaybackPositionUpdate(onPlaybackPositionUpdate: (PlaybackDuration?, MediaItem?) -> Unit) = apply {
                this.onPlaybackPositionUpdate = onPlaybackPositionUpdate
            }

            fun setOnAudioFocusLoss(onAudioFocusLoss: () -> Unit) = apply {
                this.onAudioFocusLoss = onAudioFocusLoss
            }

            fun setOnAudioFocusGain(onAudioFocusGain: () -> Unit) = apply {
                this.onAudioFocusGain = onAudioFocusGain
            }

            fun setOnAudioFocusLossTransient(onAudioFocusLossTransient: () -> Unit) = apply {
                this.onAudioFocusLossTransient = onAudioFocusLossTransient
            }

            fun setOnAudioFocusLossTransientCanDuck(onAudioFocusLossTransientCanDuck: () -> Unit) = apply {
                this.onAudioFocusLossTransientCanDuck = onAudioFocusLossTransientCanDuck
            }

            fun setRedirectionIntentBundle(redirectionIntentBundle: Bundle) = apply {
                this.redirectionIntentBundle = redirectionIntentBundle
            }

            fun setSmallIconResourceId(smallIconResourceId: Int) = apply {
                this.smallIconResourceId = smallIconResourceId
            }

            fun setPeriodicPositionUpdateEnabled(enabled: Boolean) = apply {
                this.periodicPositionUpdateEnabled = enabled
            }

            fun setUseRewindAction(enabled: Boolean) = apply {
                this.useRewindAction = enabled
            }

            fun setUseFastForwardAction(enabled: Boolean) = apply {
                this.useFastForwardAction = enabled
            }

            fun setUseRewindActionInCompactView(enabled: Boolean) = apply {
                this.rewindActionInCompactView = enabled
            }

            fun setUseFastForwardActionInCompactView(enabled: Boolean) = apply {
                this.useFastForwardActionInCompactView = enabled
            }

            fun setUseChronometer(enabled: Boolean) = apply {
                this.useChronometer = enabled
            }

            fun setUsePlayPauseActions(enabled: Boolean) = apply {
                this.usePlayPauseActions = enabled
            }

            fun setUseNextAction(enabled: Boolean) = apply {
                this.useNextAction = enabled
            }

            fun setUsePreviousAction(enabled: Boolean) = apply {
                this.usePreviousAction = enabled
            }

            fun setUseStopAction(enabled: Boolean) = apply {
                this.useStopAction = enabled
            }

            fun setColorized(colorized: Boolean) = apply {
                this.colorized = colorized
            }

            fun setColor(color: Int) = apply {
                this.color = color
            }

            fun setShowPlayButtonIfPlaybackIsSuppressed(showPlayButtonIfPlaybackIsSuppressed: Boolean) = apply {
                this.showPlayButtonIfPlaybackIsSuppressed = showPlayButtonIfPlaybackIsSuppressed
            }

            fun setOnCategoryNextCommand(block: () -> Unit) = apply {
                this.onCategoryNextCommand = block
            }

            fun setOnCategoryPrevCommand(block: () -> Unit) = apply {
                this.onCategoryPrevCommand = block
            }

            fun build(): Config {
                requireNotNull(context) { "Context is required." }
                val config = Config(
                    context = context!!,
                    sessionActivity = sessionActivity,
                    largeIcon = largeIcon,
                    smallIcon = smallIcon,
                    positionUpdateDelay = positionUpdateDelay,
                    redirectionIntentBundle = redirectionIntentBundle,
                    channelId = channelId,
                    channelName = channelName,
                    notificationId = notificationId,
                    smallIconResourceId = smallIconResourceId,
                    periodicPositionUpdateEnabled = periodicPositionUpdateEnabled,
                    setUseRewindAction = useRewindAction,
                    setUseFastForwardAction = useFastForwardAction,
                    setUseRewindActionInCompactView = rewindActionInCompactView,
                    setUseFastForwardActionInCompactView = useFastForwardActionInCompactView,
                    setUseChronometer = useChronometer,
                    setUsePlayPauseActions = usePlayPauseActions,
                    setUseNextAction = useNextAction,
                    setUsePreviousAction = usePreviousAction,
                    setUseStopAction = useStopAction,
                    setColorized = colorized,
                    setColor = color,
                    setShowPlayButtonIfPlaybackIsSuppressed = showPlayButtonIfPlaybackIsSuppressed,
                    setShouldStayAwake = shouldStayAwake,
                    onCreated = onCreated,
                    onDestroy = onDestroy,
                    onPlaybackPositionUpdate = onPlaybackPositionUpdate,
                    onTaskRemoved = onTaskRemoved,
                    onGetSession = onGetSession,
                    onStartCommand = onStartCommand,
                    onPlayerEvent = onPlayerEvent,
                    onAudioFocusLoss = onAudioFocusLoss,
                    onAudioFocusGain = onAudioFocusGain,
                    onAudioFocusLossTransient = onAudioFocusLossTransient,
                    onAudioFocusLossTransientCanDuck = onAudioFocusLossTransientCanDuck,
                    onCategoryNextCommand = onCategoryNextCommand,
                    onCategoryPrevCommand = onCategoryPrevCommand
                )
                return config
            }
        }
    }

    private class PositionUpdateTracker(
        private val updateIntervalMs: Long = 1000L,
        private val mediaController: MediaController?,
        private val onUpdate: ((PlaybackDuration?, MediaItem?) -> Unit)?
    ) {
        private val handler = Handler(Looper.getMainLooper())

        private val updateRunnable = object : Runnable {
            override fun run() {
                mediaController?.let { controller ->
                    val duration = controller.duration
                    val position = controller.currentPosition
                    val playbackDuration = if (duration != C.TIME_UNSET) {
                        PlaybackDuration(duration, position)
                    } else null
                    onUpdate?.invoke(playbackDuration, controller.currentMediaItem)
                } ?: onUpdate?.invoke(null, null)
                handler.postDelayed(this, updateIntervalMs)
            }
        }

        fun start() {
            handler.post(updateRunnable)
        }

        fun stop() {
            handler.removeCallbacks(updateRunnable)
        }
    }

    object Command {
        data object Rewind {
            const val DISPLAY_NAME = "Rewind"
            const val COMMAND = Player.COMMAND_SEEK_BACK
        }
        data object FastForward {
            const val DISPLAY_NAME = "Fast Forward"
            const val COMMAND = Player.COMMAND_SEEK_FORWARD
        }
    }

    companion object {
        @Volatile
        private var _instance: PlayerLib? = null

        val instance: PlayerLib
            get() = _instance ?: throw UninitializedPropertyAccessException("PlayerLib is not initialized. Use PlayerLibFactory first.")

        internal fun initialize(playerLib: PlayerLib) {
            synchronized(this) {
                _instance?.release()
                _instance = playerLib
            }
        }

        val isInitialized: Boolean
            get() = _instance != null
    }
}