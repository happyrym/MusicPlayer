package com.rymin.musicplayer.repository

import com.rymin.musicplayer.data.Album
import com.rymin.musicplayer.data.Music

class MusicRepositoryImpl(private val dataSource: MusicDataSource) : MusicRepository {
    override suspend fun getMusicList(): List<Music> {
        return dataSource.getMusicList()
    }

    override suspend fun getMusicListByAlbum(albumId: Long): List<Music> {
        return dataSource.getMusicListByAlbum(albumId)
    }

    override suspend fun getAlbumList(): List<Album> {
        return dataSource.getAlbumList()
    }

}
