package com.rymin.musicplayer.service

import android.app.Notification
import android.app.Notification.FOREGROUND_SERVICE_IMMEDIATE
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
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver
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

    private val _currentMusic = MutableStateFlow(null as Music?)
    val currentMusic: StateFlow<Music?> get() = _currentMusic

    private val _isLoop = MutableStateFlow(false)
    val isLoop: StateFlow<Boolean> get() = _isLoop

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> get() = _isShuffle

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying

    private lateinit var mediaSession: MediaSessionCompat

    private val playlist = mutableListOf<Music>()
    private var currentIndex = -1

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
                    resumeMusic()
                    updateNotification(isPlaying = true)
                }

                override fun onPause() {
                    isActive = false
                    pauseMusic()
                    updateNotification(isPlaying = false)
                }

                override fun onStop() {
                    isActive = false
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            })
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand ${intent?.action}")
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        createNotificationChannel()
        // 기본 Notification 생성
        val notification = createNotification(false)
        startForeground(NOTIFICATION_ID, notification)
        // Action 처리
        when (intent?.action) {
            Constants.ACTION_START_FOREGROUND -> {
                val notification = createNotification(isPlaying = true)
                startForeground(NOTIFICATION_ID, notification)
            }
            Constants.ACTION_PLAY -> resumeMusic()
            Constants.ACTION_PAUSE -> pauseMusic()
            Constants.ACTION_STOP_FOREGROUND -> stopForegroundService()
        }
        return START_STICKY
    }

    private fun stopForegroundService() {
        stopSelf()
    }


    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Music Player Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for music player notifications"
        }
        channel.setShowBadge(false)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun createPendingIntent(intentAction: String): PendingIntent {
        val intent = Intent(this, MusicPlayerService::class.java).apply {
            action = intentAction
        }
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    private fun createNotification(isPlaying: Boolean): Notification {
        val playPausePendingIntent = createPendingIntent(
            if (isPlaying) Constants.ACTION_PAUSE else Constants.ACTION_PLAY
        )
        val shufflePendingIntent = createPendingIntent(Constants.ACTION_SUFFLE)


        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText(if (isPlaying) "Now playing..." else "No music playing")
            .setSmallIcon(R.drawable.ic_btn_list)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(1, 2, 3)
            )
            .addAction(
                R.drawable.ic_btn_loop, "loop",
                shufflePendingIntent
            )
            .addAction(
                R.drawable.ic_btn_prev, "prev",
                playPausePendingIntent
            )
            .addAction(
                if (isPlaying) R.drawable.ic_btn_pause else R.drawable.ic_btn_play,
                if (isPlaying) "Pause" else "Play",
                playPausePendingIntent
            )
            .addAction(
                R.drawable.ic_btn_next, "next",
                playPausePendingIntent
            )
            .addAction(
                R.drawable.ic_btn_shuffle, "shuffle",
                playPausePendingIntent
            )
            .setOngoing(true)


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
        Timber.d("[Service] playMusic Music ${music.title}")
        if (currentMusic.value?.filePath == music.filePath && mediaPlayer?.isPlaying == true) {
            return
        }
        stopMusic()
        _currentMusic.value = music
        mediaPlayer = MediaPlayer().apply {
            updateMediaMetadata(music)
            setDataSource(applicationContext, Uri.parse(music.filePath))
            prepare()
            start()
            setOnCompletionListener {
                playNextMusic(true)
            }
        }
        _isPlaying.value = true
        updateNotification(true)
    }

    fun pauseMusic() {
        Timber.d("rymins service: pause")
        mediaPlayer?.pause()
        _isPlaying.value = false
        updateNotification(false)
    }

    fun resumeMusic() {
        mediaPlayer?.start()
        _isPlaying.value = true
        updateNotification(true)
    }

    fun setPlaylist(musicList: List<Music>, selectedMusic: Music? = null) {
        playlist.clear()
        playlist.addAll(musicList)
        Timber.d("rymins setplay")

        selectedMusic?.let {
            selectMusic(it)
            shufflePlaylist()
        } ?: run {
            Timber.d("rymins current: set0")
            currentIndex = 0
        }
    }

    private fun selectMusic(music: Music) {
        val index = playlist.indexOfFirst { it.id == music.id }
        if (index != -1) {
            Timber.d("rymins index: $index")
            currentIndex = index
        } else {
            Timber.e("Music not found in playlist")
        }
    }

    fun changeLoopMode() {
        _isLoop.value = !_isLoop.value
    }

    fun changeShuffleMode() {
        _isShuffle.value = !_isShuffle.value
        _isLoop.value = _isShuffle.value
        shufflePlaylist()
    }

    private fun shufflePlaylist() {
        if (_isShuffle.value) {
            playlist.shuffle()
        } else {
            // 셔플 해제 시 원래 순서를 복구하거나 현재 순서를 유지할 수 있습니다.
            playlist.sortBy { it.title } // 예: ID 순으로 정렬
        }
        currentIndex = playlist.indexOfFirst { it.id == _currentMusic.value?.id }
        if (currentIndex == -1) {
            currentIndex = 0
        }
    }

    fun playNextMusic(isComplete: Boolean = false) {
        if (playlist.isNotEmpty()) {
            if (_isLoop.value) {
                currentIndex = (currentIndex + 1) % playlist.size // 다음 곡으로 순환
            } else {
                if (currentIndex + 1 >= playlist.size) {
                    Toast.makeText(this, "다음곡이 없습니다.", Toast.LENGTH_SHORT).show()
                    if (isComplete) return

                } else {
                    currentIndex = (currentIndex + 1) % playlist.size
                }
            }
            val nextMusic = playlist[currentIndex]
            playMusic(nextMusic)
        } else {
            Toast.makeText(this, "플레이 리스트가 비어있습니다.", Toast.LENGTH_SHORT).show()
            Timber.d("Playlist is empty")
        }
    }

    fun playPrevMusic() {
        if (playlist.isNotEmpty()) {
            if (_isLoop.value) {
                currentIndex =
                    if (currentIndex - 1 < 0) playlist.size - 1 else currentIndex - 1
            } else {
                if (currentIndex - 1 < 0) {
                    Toast.makeText(this, "이전곡이 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    currentIndex -= 1 // 이전 곡으로 순환
                }
            }
            val prevMusic = playlist[currentIndex]
            playMusic(prevMusic)
        } else {
            Toast.makeText(this, "플레이 리스트가 비어있습니다.", Toast.LENGTH_SHORT).show()
            Timber.d("Playlist is empty")
        }
    }

    private fun updateNotification(isPlaying: Boolean) {
        val notification = createNotification(isPlaying)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        stopForeground(STOP_FOREGROUND_REMOVE)
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
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
        mediaPlayer?.release()
        mediaSession.release()
    }
}
