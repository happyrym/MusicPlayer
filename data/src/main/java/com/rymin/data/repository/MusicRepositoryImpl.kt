package com.rymin.data.repository

import com.rymin.common.data.Album
import com.rymin.common.data.Music
import com.rymin.data.repository.MusicDataSource
import com.rymin.data.repository.MusicRepository
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
