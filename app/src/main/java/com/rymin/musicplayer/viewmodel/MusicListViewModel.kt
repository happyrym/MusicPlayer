package com.rymin.musicplayer.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rymin.common.config.Constants
import com.rymin.common.data.Album
import com.rymin.common.data.Music
import com.rymin.data.usecase.MusicListUseCase
import com.rymin.core.service.MusicPlayerService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class MusicListViewModel(
    private val appContext: Context,
    private val musicListUseCase: MusicListUseCase
) : ViewModel() {

    private val _musicList = MutableStateFlow<List<Music>>(emptyList())
    val musicList: StateFlow<List<Music>> get() = _musicList

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> get() = _selectedAlbum

    private val _albumList = MutableStateFlow<List<Album>>(emptyList())
    val albumList: StateFlow<List<Album>> get() = _albumList

    private val _currentMusic = MutableStateFlow<Music?>(null)
    val currentMusic: StateFlow<Music?> get() = _currentMusic

    private val _currentPosition = MutableStateFlow(0f)
    val currentPosition: StateFlow<Float> get() = _currentPosition

    private val _isLoop = MutableStateFlow(false)
    val isLoop: StateFlow<Boolean> get() = _isLoop

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> get() = _isShuffle

    private val _volume = MutableStateFlow(0f)
    val volume: StateFlow<Float> get() = _volume


    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying

    private val _duration = MutableStateFlow(0f)
    val duration: StateFlow<Float> get() = _duration

    private var isServiceBound = false

    private var musicPlayerService: MusicPlayerService? = null


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

    fun bindService(service: MusicPlayerService) {
        this.musicPlayerService = service
        viewModelScope.launch {
            service.isPlaying.collect { isPlaying ->
                _isPlaying.value = isPlaying
                viewModelScope.launch {
                    updateSliderFlow().collectLatest { position ->
                        _currentPosition.value = position
                    }
                }
            }
        }
        viewModelScope.launch {
            service.currentMusic.collect { music ->
                _currentMusic.value = music
            }
        }
        viewModelScope.launch {
            service.isLoop.collect { isLoop ->
                _isLoop.value = isLoop
            }
        }
        viewModelScope.launch {
            service.isShuffle.collect { isShuffle ->
                _isShuffle.value = isShuffle
            }
        }
    }

    fun unbindService() {
        musicPlayerService = null
        if(isServiceBound)
        appContext.unbindService(serviceConnection)
    }

    private fun fetchMusicList() {
        viewModelScope.launch {
            musicListUseCase.getMusicList().collect {
                _musicList.value = it
            }
            musicListUseCase.getAlbumList().collect {
                _albumList.value = it
            }
        }
    }


    fun showAlbumList() {
        _selectedAlbum.value = null
    }

    fun selectedAlbum(album: Album) {
        _selectedAlbum.value = album // 선택된 앨범 상태 업데이트
        viewModelScope.launch {
            musicListUseCase.getMusicListByAlbum(album.id).collect {
                _musicList.value = it
            }
        }
    }

    fun playMusic(music: Music) {
        startMusicService()
        bindToService()
        viewModelScope.launch {
            flow {
                // Service 바인딩 후 지연 시간
                delay(200)

                // 플레이리스트 및 음악 재생
                musicPlayerService?.setPlaylist(_musicList.value, music)
                musicPlayerService?.playMusic(music)

                _duration.value = musicPlayerService?.getDuration()?.toFloat() ?: 0f
                _isPlaying.value = true
                emitAll(updateSliderFlow())
            }.onStart {
                _currentPosition.value = 0f
            }.collectLatest { position ->
                _currentPosition.value = position
            }
        }
    }

    fun playNextMusic() {
        musicPlayerService?.playNextMusic()
    }

    fun playPrevMusic() {
        musicPlayerService?.playPrevMusic()
    }

    fun changeLoopMode() {
        musicPlayerService?.changeLoopMode()
    }

    fun changeShuffleMode() {
        musicPlayerService?.changeShuffleMode()
    }

    fun bindToService() {
        val intent = Intent(appContext, MusicPlayerService::class.java)
        appContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun playOrPauseMusic() {
        if (_isPlaying.value) {
            stopMusicService()
            musicPlayerService?.pauseMusic()
        } else {
            musicPlayerService?.resumeMusic()
        }
    }

    private fun updateSliderFlow() = flow {
        while (_isPlaying.value) {
            emit(musicPlayerService?.getCurrentPosition()?.toFloat() ?: 0f)
            delay(500) // 500ms 간격으로 업데이트
        }
    }
    fun seekToPosition(position: Float) {
        musicPlayerService?.seekTo(position.toInt())
        _currentPosition.value = position
    }

    override fun onCleared() {
        super.onCleared()
        musicPlayerService = null
    }

    fun getVolume() {
        val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        _volume.value = currentVolume / maxVolume.toFloat()
    }

    fun setVolume(volume: Float) {
        val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val newVolume = (volume * maxVolume).toInt()
        _volume.value = volume
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
    }

    private fun startMusicService() {
        val intent = Intent(Constants.ACTION_START_FOREGROUND)
        intent.setPackage(appContext.packageName)
        appContext.sendBroadcast(intent)
    }

    private fun stopMusicService() {
        val intent = Intent(Constants.ACTION_STOP_FOREGROUND)
        intent.setPackage(appContext.packageName)
        appContext.sendBroadcast(intent)
    }

}
