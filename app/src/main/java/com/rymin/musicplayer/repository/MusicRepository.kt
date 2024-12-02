package com.rymin.musicplayer.repository

import com.rymin.musicplayer.data.Album
import com.rymin.musicplayer.data.Music

interface MusicRepository {
    suspend fun getMusicList(): List<Music>
    suspend fun getMusicListByAlbum(albumId: Long): List<Music>
    suspend fun getAlbumList(): List<Album>

}