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
            id = "1234",
            title = "EDergi",
            description = "Öne Çıkanlar",
            m3u8Url = "https://cs25-2v4.vkuseraudio.net/s/v1/ac/6eEbeJLYA40oJpXiZweJ4kxFQbVUFlthr122mnRtEQkQrgQhb2zT3PUJTKYt7RBEJMuqylePyew0eoF-fmsI6Ag3QpmudRgotTQJEzadtSOXFSkcykXiLVXHZQdxOINZO-u4ujacHRlZWz3rV0FJnXjZTezQtHtZZSCBOz8hku4jm_k/index.m3u8",
            paperId = "5678"
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

    override fun onDestroy() {
        super.onDestroy()
        PlayerLib.instance.clearMediaItems()
        PlayerLib.instance.stop()
    }

    // this is required (for some versions of android) for PlayerLib to be able to display foreground service
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 2000)
            }
        }
    }
}