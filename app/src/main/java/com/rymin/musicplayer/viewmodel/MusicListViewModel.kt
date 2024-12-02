package com.rymin.musicplayer.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.os.IBinder
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rymin.musicplayer.data.Album
import com.rymin.musicplayer.data.Music
import com.rymin.musicplayer.repository.MusicRepository
import com.rymin.musicplayer.service.MusicPlayerService
import com.rymin.musicplayer.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MusicListViewModel(
    private val appContext: Context,
    private val musicRepository: MusicRepository
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
                if (_isPlaying.value) {
                    updateSlider()
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
        appContext.unbindService(serviceConnection)
    }

    private fun fetchMusicList() {
        viewModelScope.launch {
            _musicList.value = musicRepository.getMusicList()
            _albumList.value = musicRepository.getAlbumList()
        }
    }


    fun showAlbumList() {
        _selectedAlbum.value = null
    }

    fun selectedAlbum(album: Album) {
        _selectedAlbum.value = album // 선택된 앨범 상태 업데이트
        viewModelScope.launch {
            _musicList.value = musicRepository.getMusicListByAlbum(album.id) // 앨범 ID로 음악 목록 로드
        }
    }

    fun playMusic(music: Music) {
        startMusicService(appContext)
        bindToService(appContext)
        viewModelScope.launch {
            //bind 시간 대기
            delay(200)
            Timber.d("rymins _musicList.value: ${_musicList.value}")
            musicPlayerService?.setPlaylist(_musicList.value, music)
            musicPlayerService?.playMusic(music)
            _duration.value = musicPlayerService?.getDuration()?.toFloat() ?: 0f
            _isPlaying.value = true
            while (_isPlaying.value) {
                _currentPosition.value = musicPlayerService?.getCurrentPosition()?.toFloat() ?: 0f
                delay(500)
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
    }

    private fun updateSlider() {
        viewModelScope.launch {
            while (_isPlaying.value) {
                _currentPosition.value =
                    musicPlayerService?.getCurrentPosition()?.toFloat() ?: 0f
                delay(500)
            }
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

    private fun startMusicService(context: Context) {
        val intent = Intent(Constants.ACTION_START_FOREGROUND)
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }

    private fun stopMusicService(context: Context) {
        val intent = Intent(Constants.ACTION_STOP_FOREGROUND)
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }
}
