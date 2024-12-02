package com.rymin.musicplayer.repository

import com.rymin.musicplayer.data.Album
import com.rymin.musicplayer.data.Music
import kotlinx.coroutines.flow.Flow

class MusicRepositoryImpl(private val dataSource: MusicDataSource) : MusicRepository {
    override suspend fun getMusicList(): Flow<List<Music>> {
        return dataSource.getMusicList()
    }

    override suspend fun getMusicListByAlbum(albumId: Long): Flow<List<Music>> {
        return dataSource.getMusicListByAlbum(albumId)
    }

    override suspend fun getAlbumList(): Flow<List<Album>>{
        return dataSource.getAlbumList()
    }

}
