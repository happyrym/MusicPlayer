package com.rymin.data.repository

import com.rymin.common.data.Album
import com.rymin.common.data.Music
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun getMusicList(): Flow<List<Music>>
    suspend fun getMusicListByAlbum(albumId: Long): Flow<List<Music>>
    suspend fun getAlbumList(): Flow<List<Album>>

}