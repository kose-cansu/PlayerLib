# PlayerLib
Makes having a playback session easier.

[![](https://jitpack.io/v/edergi/PlayerLib.svg)](https://jitpack.io/#edergi/PlayerLib)
[Sample app]

## How to use

### Setup

First, implement PlayerLib into your app:
```groovy
dependencies {
    implementation("com.github.edergi:PlayerLib:2.0.0")
}
```
See: https://jitpack.io/#edergi/PlayerLib/2.0.0

Second, you need to have the PlayerLib's service declared in your app's manifest file. You also need to have the required permissions declared in there. Let's go step by step:
1. Declare the service in your 'AndroidManifest.xml' of your app:
    ```xml
        <service
            android:name="com.edergi.playerlibapp.service.PlaybackService"
            android:foregroundServiceType="mediaPlayback"
            android:exported="true">
            <intent-filter>
                <action android:name="androidx.media3.session.MediaSessionService"/>
            </intent-filter>
        </service>
    ```
2. Declare the required permissions:
    ```xml
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    ```
    - Internet is optional yet it is needed if you plan to get the audio files (m3u8, mp3, etc.) from network.
    - Post notifications is required for some versions of Android for this library to be able to post a foreground service notification. **Don't forget to ask that permission from your user, otherwise the library won't work.**
    - Wake lock is to keep the user's device partially locked (to keep the service running)

### Initializing

- PlayerLib requires a context to initialize a SessionToken which plays a bridge role between the library and your app.
  For that, you need to use PlayerLibFactory. Best place to initialize it would probably be your starting entry point such as your Application class or MainActivity (depends on how you plan to use the library though).
    ```kotlin
        PlayerLibFactory.init(context = this) {
            // your configurations
        }
    ```
  There are multiple factory methods.
    - "init" functions are for using PlayerLib's own singleton which can be accessed by "PlayerLib.instance" throughout your app.
    - "create" functions are for creating the instance only. Ideal if you plan to have it in your own DI system. (recommended approach)
    ```kotlin
        fun init(context: Context) {
            return PlayerLib.Config.Builder().apply {
                setContext(context)
            }.build().let { config -> PlayerLib(config).let { lib -> PlayerLib.initialize(lib) } }
        }
        fun init(context: Context, configure: PlayerLib.Config.Builder.() -> Unit) {
            PlayerLib.Config.Builder()
                .setContext(context)
                .apply(configure)
                .build()
                .let { config -> PlayerLib(config).let { lib -> PlayerLib.initialize(lib) } }
        }
        fun create(context: Context): PlayerLib {
            return PlayerLib.Config.Builder().apply {
                setContext(context)
            }.build().let { config -> PlayerLib(config) }
        }
        fun create(context: Context, configure: PlayerLib.Config.Builder.() -> Unit): PlayerLib {
            return PlayerLib.Config.Builder()
                .setContext(context)
                .apply(configure)
                .build()
                .let { config -> PlayerLib(config) }
        }
    ```
### Configuration

There are a lot of configurations options to list them all here. Some of them will be explained in here yet others are pretty much self explanatory.

- sessionActivity (nullable): When users clicks on the foreground notification, they will be heading to this activity. If you make it null, the tap will do nothing.
- largeIcon (nullable): We can say that it's some sort of a background icon of the notification (for some devices, each model like Samsung, Xiaomi, Oppo they all have different media notification styles. You need to test to see what fits for you.).
- periodicPositionUpdateEnabled & onPlaybackPositionUpdate (nullable): There are no built in callbacks/listeners that is being executed to notify its subscribers about the current position of the current media item. There is only one built in way (as far as I could see), yet, if I recall right, it is being executed every nanosecond, which probably would make the user's life harder. So, we had to make a custom handler on it, which is triggered each second (configurable) untill the service's death.
- There are many other configurations, check them all out in the scope of the config builder.
  Simple configuration:
    ```kotlin
        PlayerLibFactory.init(this) {

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

            setOnAudioFocusGain {
                log("Audio focused gained.")
            }

            setOnCreated {
                log("PlaybackService has been destroyed.")
            }

            // other configurations...

        }
    ```

### Start to use

All media related commands are going through PlayerLib.instance (or your own singleton). Here is an example:
```kotlin
findViewById<Button>(R.id.button).setOnClickListener {
    PlayerLib.instance.play(tracks)
}
findViewById<Button>(R.id.stop).setOnClickListener {
    PlayerLib.instance.stop()
}
findViewById<Button>(R.id.pause).setOnClickListener {
    PlayerLib.instance.pause()
}
findViewById<Button>(R.id.seekToNext).setOnClickListener {
    PlayerLib.instance.seekToNext()
}
findViewById<Button>(R.id.seekToPrevious).setOnClickListener {
    PlayerLib.instance.seekToPrevious()
}
findViewById<Button>(R.id.play).setOnClickListener {
    PlayerLib.instance.play()
}
findViewById<Button>(R.id.setPlaybackSpeed1).setOnClickListener {
    PlayerLib.instance.setPlaybackSpeed(1f)
}
findViewById<Button>(R.id.setPlaybackSpeed1_25).setOnClickListener {
    PlayerLib.instance.setPlaybackSpeed(1.25f)
}
findViewById<Button>(R.id.setPlaybackSpeed1_5).setOnClickListener {
    PlayerLib.instance.setPlaybackSpeed(1.5f)
}
findViewById<Button>(R.id.setPlaybackSpeed2).setOnClickListener {
    PlayerLib.instance.setPlaybackSpeed(2f)
}
// other commands, check them all out in PlayerLib.instance
```

[Sample App]: <https://github.com/edergi/PlayerLib/tree/master/app>