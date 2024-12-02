package com.rymin.musicplayer.repository

import com.rymin.musicplayer.data.Album
import com.rymin.musicplayer.data.Music
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun getMusicList(): Flow<List<Music>>
    suspend fun getMusicListByAlbum(albumId: Long): Flow<List<Music>>
    suspend fun getAlbumList(): Flow<List<Album>>

}