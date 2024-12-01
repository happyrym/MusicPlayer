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
import com.rymin.musicplayer.data.Album
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

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> get() = _selectedAlbum

    private val _playlist = MutableStateFlow<List<Music>>(emptyList())
    val playlist: StateFlow<List<Music>> get() = _playlist

    private val _albumList = MutableStateFlow<List<Album>>(emptyList())
    val albumList: StateFlow<List<Album>> get() = _albumList

    private val _currentMusic = MutableStateFlow<Music?>(null)
    val currentMusic: StateFlow<Music?> get() = _currentMusic

    private val _currentPosition = MutableStateFlow(0f)
    val currentPosition: StateFlow<Float> get() = _currentPosition

    private val _isLoop = MutableStateFlow(false)
    val isLoop: StateFlow<Boolean> get() = _isLoop


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
    }

    fun unbindService() {
        musicPlayerService = null
        appContext.unbindService(serviceConnection)
    }

    private fun fetchMusicList() {
        viewModelScope.launch {
            Timber.d("rymins fetch")
            _musicList.value = getMusicList()
            _albumList.value = getAlbumList()
        }
    }

    private suspend fun getAlbumList(): List<Album> {
        return withContext(Dispatchers.IO) {
            val albumList = mutableListOf<Album>()
            val albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            )

            val cursor = appContext.contentResolver.query(
                albumUri,
                projection,
                null,
                null,
                "${MediaStore.Audio.Albums.ALBUM} ASC"
            )

            cursor?.use {
                val idIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
                val albumIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
                val artistIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
                val numberOfSongsIndex =
                    it.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)

                while (it.moveToNext()) {
                    albumList.add(
                        Album(
                            id = it.getLong(idIndex),
                            title = it.getString(albumIndex),
                            artist = it.getString(artistIndex),
                            numberOfSongs = it.getInt(numberOfSongsIndex),
                        )
                    )
                }
            }
            albumList
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

    fun showAlbumList() {
        _selectedAlbum.value = null
    }

    fun selectedAlbum(album: Album) {
        _selectedAlbum.value = album // 선택된 앨범 상태 업데이트
        viewModelScope.launch {
            Timber.d("rymins selsecalbum")
            _musicList.value = getMusicListByAlbum(album.id) // 앨범 ID로 음악 목록 로드
        }
    }

    private suspend fun getMusicListByAlbum(albumId: Long): List<Music> {
        return withContext(Dispatchers.IO) {
            val musicList = mutableListOf<Music>()
            Timber.d("rymins here1")
            val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
            )
            val selection = "${MediaStore.Audio.Media.ALBUM_ID} = ?"
            val selectionArgs = arrayOf(albumId.toString())
            val cursor = appContext.contentResolver.query(
                musicUri,
                projection,
                selection,
                selectionArgs,
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

    fun seekToPosition(position: Float) {
        musicPlayerService?.seekTo(position.toInt())
        _currentPosition.value = position
    }

    override fun onCleared() {
        super.onCleared()
        musicPlayerService = null
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
