package com.rymin.musicplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.rymin.musicplayer.R
import com.rymin.musicplayer.data.Music
import com.rymin.musicplayer.utils.Constants
import com.rymin.musicplayer.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.rymin.musicplayer.utils.Constants.NOTIFICATION_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MusicPlayerService : Service() {

    private val binder = MusicPlayerBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var currentMusic: Music? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying
    private lateinit var mediaSession: MediaSessionCompat

    inner class MusicPlayerBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(applicationContext, "MusicService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    isActive = true
                    updateNotification(isPlaying = true)
                }

                override fun onPause() {
                    isActive = false
                    updateNotification(isPlaying = false)
                }

                override fun onStop() {
                    isActive = false
                    stopForeground(true)
                    stopSelf()
                }
            })
            isActive = true // MediaSession 활성화
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        // Action 처리
        when (intent?.action) {
            Constants.ACTION_PLAY -> resumeMusic()
            Constants.ACTION_PAUSE -> pauseMusic()
            Constants.ACTION_STOP -> stopForegroundService()

        }
        // 기본 Notification 생성
        val notification = createNotification("No music playing")
        startForeground(Constants.NOTIFICATION_ID, notification)

        return START_STICKY
    }

    private fun stopForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }

        stopSelf()
    }


    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Music Player Notifications",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel for music player notifications"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun createNotification(contentText: String): Notification {
        val playIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = Constants.ACTION_PLAY
        }
        val playPendingIntent = PendingIntent.getService(
            this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = Constants.ACTION_PAUSE
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = Constants.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 기본 Notification 생성
        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Music Player").setContentText(contentText)
            .setSmallIcon(R.drawable.ic_btn_list)
            .setPriority(NotificationCompat.PRIORITY_HIGH).setOngoing(true)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0, 1, 2)) // 액션 버튼을 표시
            .addAction(R.drawable.ic_btn_play, "Play", playPendingIntent)
            .addAction(R.drawable.ic_btn_pause, "Pause", pausePendingIntent)
            .addAction(R.drawable.ic_btn_stop, "Stop", stopPendingIntent)
            .build()
    }

    fun playMusic(music: Music) {
        if (currentMusic?.filePath == music.filePath && mediaPlayer?.isPlaying == true) {
            return
        }
        stopMusic()

        currentMusic = music
        mediaPlayer = MediaPlayer().apply {
            updateMediaMetadata(music)
            setDataSource(applicationContext, Uri.parse(music.filePath))
            prepare()
            start()
        }
        _isPlaying.value = true
    }
    fun updateMediaMetadata(music: Music) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, music.title) // 곡 제목
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, music.artist) // 아티스트
//            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, music.artist) // 앨범 아트
            .build()

        mediaSession.setMetadata(metadata)
    }
    fun pauseMusic() {
        mediaPlayer?.pause()
        _isPlaying.value = false
    }

     fun resumeMusic() {
        mediaPlayer?.start()
         _isPlaying.value = true
    }

    private fun updateNotification(isPlaying: Boolean) {

        val playIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = Constants.ACTION_PLAY
        }
        val playPendingIntent = PendingIntent.getService(
            this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = Constants.ACTION_PAUSE
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = Constants.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("현재 재생 중")
            .setContentText("곡 제목")
            .setSmallIcon(R.drawable.ic_btn_list)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .addAction(R.drawable.ic_btn_play, "Play", playPendingIntent)
            .addAction(R.drawable.ic_btn_pause, "Pause", pausePendingIntent)
            .addAction(R.drawable.ic_btn_stop, "Stop", stopPendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    fun stopMusic() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMusic()
        mediaSession.release()
    }
}
