package com.rymin.service

import android.app.Notification
import android.app.Notification.FOREGROUND_SERVICE_IMMEDIATE
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.rymin.common.config.Constants.ACTION_LOOP
import com.rymin.common.config.Constants.ACTION_NEXT
import com.rymin.common.config.Constants.ACTION_PAUSE
import com.rymin.common.config.Constants.ACTION_PLAY
import com.rymin.common.config.Constants.ACTION_PREV
import com.rymin.common.config.Constants.ACTION_SHUFFLE
import com.rymin.common.config.Constants.ACTION_START_FOREGROUND
import com.rymin.common.config.Constants.ACTION_STOP_FOREGROUND
import com.rymin.common.config.Constants.NOTIFICATION_CHANNEL_ID
import com.rymin.common.config.Constants.NOTIFICATION_CHANNEL_NAME
import com.rymin.common.config.Constants.NOTIFICATION_ID
import com.rymin.common.data.Music
import com.rymin.common.ui.R
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
        createNotificationChannel()

        mediaSession = MediaSessionCompat(applicationContext, "MusicService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onCustomAction(action: String?, extras: Bundle?) {
                    super.onCustomAction(action, extras)
                    when (action) {
                        ACTION_PREV -> playPrevMusic()
                        ACTION_NEXT -> playNextMusic()
                        ACTION_LOOP -> changeLoopMode()
                        ACTION_SHUFFLE -> changeShuffleMode()
                    }

                }

                override fun onPlay() {
                    isActive = true
                    resumeMusic()
                    updateNotification()
                }

                override fun onPause() {
                    isActive = false
                    pauseMusic()
                    updateNotification()
                }

                override fun onStop() {
                    isActive = false
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }

                override fun onSeekTo(pos: Long) {
                    super.onSeekTo(pos)
                    seekTo(pos.toInt())
                }
            })
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand ${intent?.action}")
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        createNotificationChannel()
        when (intent?.action) {
            ACTION_START_FOREGROUND -> {
                val notification = createNotification()
                startForeground(NOTIFICATION_ID, notification)
            }

            ACTION_STOP_FOREGROUND -> stopForegroundService()
            ACTION_PLAY -> resumeMusic()
            ACTION_PAUSE -> pauseMusic()
        }
        return START_STICKY
    }

    private fun stopForegroundService() {
        stopSelf()
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        var channel = manager?.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
        if (channel == null) {
            channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for music player notifications"
            }
            channel.setShowBadge(false)
            manager?.createNotificationChannel(channel)
        }

    }

    private fun createNotification(): Notification {
        val notificationIntent =
            applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
                ?.apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_btn_list)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setContentIntent(pendingIntent)
            .setOngoing(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
        }
        return builder.build()
    }

    private fun updateMediaMetadata(music: Music) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, music.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, music.artist)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, music.duration)
            .build()

        mediaSession.setMetadata(metadata)
    }

    fun playMusic(music: Music) {
        Timber.d("[Service] playMusic Music ${music.title}")
        if (currentMusic.value?.filePath == music.filePath && mediaPlayer?.isPlaying == true) {
            return
        }
        _currentMusic.value = music
        mediaPlayer?.stop()
        mediaPlayer?.release()
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
        updateNotification()
        updatePlaybackState()
    }

    fun pauseMusic() {
        mediaPlayer?.pause()
        _isPlaying.value = false
        updateNotification()
        updatePlaybackState()
    }

    fun resumeMusic() {
        mediaPlayer?.start()
        _isPlaying.value = true
        updateNotification()
        updatePlaybackState()
    }

    fun setPlaylist(musicList: List<Music>, selectedMusic: Music? = null) {
        playlist.clear()
        playlist.addAll(musicList)

        selectedMusic?.let {
            selectMusic(it)
            shufflePlaylist()
        } ?: run {
            currentIndex = 0
        }
    }

    private fun selectMusic(music: Music) {
        val index = playlist.indexOfFirst { it.id == music.id }
        if (index != -1) {
            currentIndex = index
        } else {
            Timber.e("Music not found in playlist")
        }
    }

    fun changeLoopMode() {
        _isLoop.value = !_isLoop.value
        updatePlaybackState()
    }

    fun changeShuffleMode() {
        _isShuffle.value = !_isShuffle.value
        _isLoop.value = _isShuffle.value
        shufflePlaylist()
        updatePlaybackState()
    }

    private fun shufflePlaylist() {
        if (_isShuffle.value) {
            playlist.shuffle()
        } else {
            playlist.sortBy { it.title }
        }
        currentIndex = playlist.indexOfFirst { it.id == _currentMusic.value?.id }
        if (currentIndex == -1) {
            currentIndex = 0
        }
    }

    fun playNextMusic(isComplete: Boolean = false) {
        if (playlist.isNotEmpty()) {
            if (_isLoop.value) {
                currentIndex = (currentIndex + 1) % playlist.size
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
                    currentIndex -= 1
                }
            }
            val prevMusic = playlist[currentIndex]
            playMusic(prevMusic)
        } else {
            Toast.makeText(this, "플레이 리스트가 비어있습니다.", Toast.LENGTH_SHORT).show()
            Timber.d("Playlist is empty")
        }
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        updatePlaybackState()
    }


    private fun updatePlaybackState() {
        val state = if (_isPlaying.value) {
            PlaybackStateCompat.STATE_PLAYING
        } else {
            PlaybackStateCompat.STATE_PAUSED
        }
        val currentPosition = (mediaPlayer?.currentPosition ?: 0).toLong()
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_STOP or
                            PlaybackStateCompat.ACTION_SEEK_TO
                )
                .addCustomAction(ACTION_PREV, ACTION_PREV, R.drawable.ic_btn_prev)
                .addCustomAction(ACTION_NEXT, ACTION_NEXT, R.drawable.ic_btn_next)
                .addCustomAction(
                    ACTION_LOOP,
                    ACTION_LOOP,
                    if (_isLoop.value) R.drawable.ic_btn_loop else R.drawable.ic_btn_loop_disable
                )
                .addCustomAction(
                    ACTION_SHUFFLE,
                    ACTION_SHUFFLE,
                    if (_isShuffle.value) R.drawable.ic_btn_shuffle else R.drawable.ic_btn_shuffle_disable
                )
                .setState(
                    state, currentPosition, if (_isPlaying.value) 1.0f else 0.0f
                )
                .build()
        )
    }
}
