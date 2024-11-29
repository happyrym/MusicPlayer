package com.rymin.musicplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rymin.musicplayer.R
import com.rymin.musicplayer.data.Music
import com.rymin.musicplayer.utils.Constants
import com.rymin.musicplayer.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.rymin.musicplayer.utils.Constants.NOTIFICATION_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

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
                    stopForeground( STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            })
            isActive = true // MediaSession 활성화
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        // 기본 Notification 생성
        val notification = createNotification(false)
        startForeground(NOTIFICATION_ID, notification)
        // Action 처리
        when (intent?.action) {
            Constants.ACTION_PLAY -> resumeMusic()
            Constants.ACTION_PAUSE -> pauseMusic()
            Constants.ACTION_STOP -> stopForegroundService()
        }
        return START_STICKY
    }

    private fun stopForegroundService() {
        Timber.d("rymins stop?")
        stopForeground(STOP_FOREGROUND_REMOVE)
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
    private fun createNotification(isPlaying: Boolean): Notification {
        // PendingIntent 생성
        val playPauseIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = if (isPlaying) Constants.ACTION_PAUSE else Constants.ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(
            this, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = Constants.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // NotificationCompat.Builder 생성
        val builder = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText(if (isPlaying) "Now playing..." else "No music playing")
            .setSmallIcon(R.drawable.ic_btn_list)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0, 1, 2))
            .addAction(
                if (isPlaying) R.drawable.ic_btn_pause else R.drawable.ic_btn_play,
                if (isPlaying) "Pause" else "Play",
                playPausePendingIntent
            )
            .addAction(R.drawable.ic_btn_stop, "Stop", stopPendingIntent)

        // Notification 생성 및 반환
        return builder.build()
    }

    private fun updateMediaMetadata(music: Music) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, music.title) // 곡 제목
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, music.artist) // 아티스트
            .build()

        mediaSession.setMetadata(metadata)
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
        updateNotification(true)
    }
    fun pauseMusic() {
        mediaPlayer?.pause()
        _isPlaying.value = false
        updateNotification(false)
    }

     fun resumeMusic() {
        mediaPlayer?.start()
         _isPlaying.value = true
         updateNotification(true)
    }

    private fun updateNotification(isPlaying: Boolean) {
        Timber.d("rymins update isPlaying : $isPlaying")
        val notification = createNotification(isPlaying)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        stopForeground(STOP_FOREGROUND_REMOVE) // Foreground Service에서 Notification 제거
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID) // Notification 제거

        mediaPlayer = null
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
