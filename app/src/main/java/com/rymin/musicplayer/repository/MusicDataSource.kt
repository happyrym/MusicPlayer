package com.rymin.musicplayer.repository
import android.content.Context
import android.provider.MediaStore
import com.rymin.musicplayer.data.Music
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicDataSource(private val context: Context) {
    suspend fun getMusicList(): List<Music> {
        return withContext(Dispatchers.IO) {
            val musicList = mutableListOf<Music>()
            val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
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
            musicList
        }
    }

}
