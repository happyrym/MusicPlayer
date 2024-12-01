package com.rymin.musicplayer.data

data class Music(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val filePath: String,
    val albumId: Long,
)
