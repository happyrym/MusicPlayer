package com.rymin.musicplayer.data

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val albumArt: String // 앨범 이미지 경로 (URL 또는 로컬 경로)
)
