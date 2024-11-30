package com.rymin.musicplayer.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rymin.musicplayer.data.Music
import com.rymin.musicplayer.service.MusicPlayerService
import com.rymin.musicplayer.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MusicListViewModel(application: Application) : ViewModel() {
    private val appContext = application.applicationContext

    private val _musicList = MutableStateFlow<List<Music>>(emptyList())
    val musicList: StateFlow<List<Music>> get() = _musicList

    private val _currentMusic = MutableStateFlow<Music?>(null)
    val currentMusic: StateFlow<Music?> get() = _currentMusic

    private val _currentPosition = MutableStateFlow(0f)
    val currentPosition: StateFlow<Float> get() = _currentPosition

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying

    private val _duration = MutableStateFlow(0f)
    val duration: StateFlow<Float> get() = _duration

    private var isServiceBound = false


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.MusicPlayerBinder
            musicPlayerService = binder.getService()
            bindService(binder.getService())
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicPlayerService = null
            isServiceBound = false
        }
    }
    init {
        fetchMusicList()
    }

    private var musicPlayerService: MusicPlayerService? = null

    fun bindService(service: MusicPlayerService) {
        this.musicPlayerService = service
        viewModelScope.launch {
            service.isPlaying.collect { isPlaying ->
                _isPlaying.value = isPlaying
            }
        }
    }

    fun unbindService() {
        musicPlayerService = null
        appContext.unbindService(serviceConnection)
    }

    private fun fetchMusicList() {
        viewModelScope.launch {
            _musicList.value = getMusicList()
        }
    }

    private suspend fun getMusicList(): List<Music> {
        return withContext(Dispatchers.IO) {
            val musicList = mutableListOf<Music>()
            val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
            )
            val cursor = appContext.contentResolver.query(
                musicUri,
                projection,
                "${MediaStore.Audio.Media.IS_MUSIC} != 0",
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )

            cursor?.use {
                val idIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (it.moveToNext()) {
                    musicList.add(
                        Music(
                            id = it.getLong(idIndex),
                            title = it.getString(titleIndex),
                            artist = it.getString(artistIndex),
                            duration = it.getLong(durationIndex),
                            filePath = it.getString(dataIndex)
                        )
                    )
                }
            }
            musicList
        }
    }

    fun playMusic(music: Music) {
        startMusicService(appContext)
        bindToService(appContext)
        viewModelScope.launch {
            delay(500) // 바인딩이 완료될 때까지 기다림
            musicPlayerService?.playMusic(music)
            _currentMusic.value = music
            _duration.value = musicPlayerService?.getDuration()?.toFloat() ?: 0f
            _isPlaying.value = true
        }
        viewModelScope.launch {
            while (true) {
                _currentPosition.value = musicPlayerService?.getCurrentPosition()?.toFloat() ?: 0f
                delay(500)
            }
        }
    }
    fun bindToService(context: Context) {
        val intent = Intent(context, MusicPlayerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun playOrPauseMusic() {
            if (_isPlaying.value) {
                stopMusicService(appContext)
                musicPlayerService?.pauseMusic()
            } else {
                musicPlayerService?.resumeMusic()
            }
            _isPlaying.value = !_isPlaying.value
    }

    fun seekToPosition(position: Float) {
        musicPlayerService?.seekTo(position.toInt())
        _currentPosition.value = position
    }

    override fun onCleared() {
        super.onCleared()
        musicPlayerService = null
    }


    fun startMusicService(context: Context) {
        val intent = Intent(Constants.ACTION_START_FOREGROUND)
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }

    fun stopMusicService(context: Context) {
        val intent = Intent(Constants.ACTION_STOP_FOREGROUND)
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }
}
