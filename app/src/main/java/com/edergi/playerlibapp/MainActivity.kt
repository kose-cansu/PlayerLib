package com.edergi.playerlibapp

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.UnstableApi
import com.edergi.playerlib.PlayerLib
import com.edergi.playerlib.model.Track

class MainActivity: AppCompatActivity() {

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        askNotificationPermission()

        val track = Track(
            title = "EDergi",
            description = "Öne Çıkanlar",
            m3u8Url = "https://e-dergi.turktelekom.com.tr/api/media/document/voice-newsletter/stream/679fb444733c8b5559fbce7a/output.m3u8"
        )

        val tracks = mutableListOf<Track>().apply {
            repeat(10) { add(track) }
        }.toList()

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

    }

    // this is required (for some versions of android) for PlayerLib to be able to display foreground service
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 5858)
            }
        }
    }
}