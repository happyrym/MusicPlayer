package com.rymin.musicplayer.repository

import com.rymin.musicplayer.data.Music

interface MusicRepository {
    suspend fun getMusicList(): List<Music>
}