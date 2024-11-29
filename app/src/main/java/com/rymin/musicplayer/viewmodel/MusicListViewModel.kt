package com.rymin.musicplayer.viewmodel

import android.app.Application
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rymin.musicplayer.data.Music
import com.rymin.musicplayer.service.MusicPlayerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        musicPlayerService?.playMusic(music)

        _currentMusic.value = music
        _duration.value = musicPlayerService?.getDuration()?.toFloat() ?: 0f
        _isPlaying.value = true

        viewModelScope.launch {
            while (true) {
                _currentPosition.value = musicPlayerService?.getCurrentPosition()?.toFloat() ?: 0f
                delay(500)
            }
        }
    }

    fun playOrPauseMusic() {
            if (_isPlaying.value) {
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
}
