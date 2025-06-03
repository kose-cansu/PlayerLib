package com.edergi.playerlibapp

import android.app.Application
import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import com.edergi.playerlib.PlayerLib
import com.edergi.playerlib.PlayerLibFactory
import com.edergi.playerlib.listener.PlayerEvent

class PlayerLibApp: Application() {

    override fun onCreate() {
        super.onCreate()
        PlayerLibFactory.init(applicationContext) {

            setSessionActivity(MainActivity::class.java)

            setOnPlaybackPositionUpdate { playbackDuration, mediaItem ->
                log("Here is the new position of ${mediaItem?.mediaMetadata?.displayTitle}: ${playbackDuration?.currentPosition}/${playbackDuration?.totalDuration}")
            }

            setOnPlayerEvent { playerEvent: PlayerEvent ->
                when (playerEvent) {
                    is PlayerEvent.DeviceVolumeChanged -> {
                        log("Device volume has changed to: ${playerEvent.volume}, isMuted: ${playerEvent.muted}")
                    }
                    is PlayerEvent.IsPlayingChanged -> {
                        log("Is playing has changed to: ${playerEvent.isPlaying}")
                    }
                    is PlayerEvent.MediaItemTransition -> {
                        log("Media item has changed to: ${playerEvent.mediaItem}")
                    }
                    is PlayerEvent.MediaMetadataChanged -> {
                        log("Media metadata has changed to: ${playerEvent.mediaMetadata}")
                    }
                    is PlayerEvent.OnPositionDiscontinuity -> {
                        log("On position discontinuity")
                    }
                    is PlayerEvent.PlaybackStateChanged -> {
                        log("Playback state has changed to this state code: ${playerEvent.state}")
                        log("Is ended: ${playerEvent.state == ExoPlayer.STATE_ENDED}")
                        log("Is idle: ${playerEvent.state == ExoPlayer.STATE_IDLE}")
                        log("Is buffering: ${playerEvent.state == ExoPlayer.STATE_BUFFERING}")
                    }
                    is PlayerEvent.PlayerError -> {
                        log("Player error: ${playerEvent.exception}")
                    }
                    is PlayerEvent.PlayerErrorChanged -> {
                        log("Player error changed to: ${playerEvent.error}")
                    }
                }
            }

            setOnAudioFocusLoss {
                PlayerLib.instance.pause() // Oynatıcıyı durdur
            }

            setOnAudioFocusLossTransient {
                PlayerLib.instance.pause() // Geçici kayıpta da durdur
            }

            setOnAudioFocusLossTransientCanDuck {
                PlayerLib.instance.setPlaybackSpeed(0.5f) // İsterseniz sesi kısabilir veya hızı düşürebilirsiniz
            }

            setOnCreated {
                log("PlaybackService has been destroyed.")
            }

            // other configurations...

        }
    }

    private fun log(message: String) {
        Log.d("PlayerLibApp", message)
    }

}