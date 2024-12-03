package com.rymin.data.repository

import android.content.Context
import android.provider.MediaStore
import com.rymin.common.data.Album
import com.rymin.common.data.Music
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MusicDataSource(private val context: Context) {
    suspend fun getMusicList(): Flow<List<Music>> = flow {
        val musicList = mutableListOf<Music>()
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val cursor = context.contentResolver.query(
            musicUri,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)


            while (it.moveToNext()) {
                musicList.add(
                    Music(
                        id = it.getLong(idIndex),
                        title = it.getString(titleIndex),
                        artist = it.getString(artistIndex),
                        duration = it.getLong(durationIndex),
                        filePath = it.getString(dataIndex),
                        albumId = it.getLong(albumIdIndex),
                    )
                )
            }
        }
        emit(musicList)
    }

    suspend fun getMusicListByAlbum(albumId: Long): Flow<List<Music>> = flow {
        val musicList = mutableListOf<Music>()
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )
        val selection = "${MediaStore.Audio.Media.ALBUM_ID} = ?"
        val selectionArgs = arrayOf(albumId.toString())
        val cursor = context.contentResolver.query(
            musicUri,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                musicList.add(
                    Music(
                        id = it.getLong(idIndex),
                        title = it.getString(titleIndex),
                        artist = it.getString(artistIndex),
                        duration = it.getLong(durationIndex),
                        filePath = it.getString(dataIndex),
                        albumId = albumId,
                    )
                )
            }
        }
        emit(musicList)
    }

    suspend fun getAlbumList(): Flow<List<Album>> = flow {
        val albumList = mutableListOf<Album>()
        val albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
        )

        val cursor = context.contentResolver.query(
            albumUri,
            projection,
            null,
            null,
            "${MediaStore.Audio.Albums.ALBUM} ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val albumIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
            val numberOfSongsIndex =
                it.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)

            while (it.moveToNext()) {
                albumList.add(
                    Album(
                        id = it.getLong(idIndex),
                        title = it.getString(albumIndex),
                        artist = it.getString(artistIndex),
                        numberOfSongs = it.getInt(numberOfSongsIndex),
                    )
                )
            }
        }
        emit(albumList)
    }
}
