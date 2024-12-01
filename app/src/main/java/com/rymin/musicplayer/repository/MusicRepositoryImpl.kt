package com.rymin.musicplayer.repository

import com.rymin.musicplayer.data.Music

class MusicRepositoryImpl(private val dataSource: MusicDataSource) : MusicRepository {
    override suspend fun getMusicList(): List<Music> {
        return dataSource.getMusicList()
    }
}
