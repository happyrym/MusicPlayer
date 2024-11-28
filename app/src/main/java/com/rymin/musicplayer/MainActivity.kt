package com.rymin.musicplayer

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.rymin.musicplayer.data.Music
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    val list = mutableListOf<Music>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicScreen()
        }
        fetchMusicList()
    }

    private fun fetchMusicList() {
        // CoroutineScope 사용: lifecycleScope
        lifecycleScope.launch {
            try {
                val musicList = getMusicList() // suspend 함수 호출
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "음악 목록을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // 음악 리스트를 가져오는 함수
    suspend fun getMusicList(): List<Music> {
        return withContext(Dispatchers.IO) { // I/O 스레드에서 실행
            val musicList = mutableListOf<Music>()
            val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
            )
            val cursor = applicationContext.contentResolver.query(
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

                while (it.moveToNext()) {
                    musicList.add(
                        Music(
                            id = it.getLong(idIndex),
                            title = it.getString(titleIndex),
                            artist = it.getString(artistIndex),
                            duration = it.getLong(durationIndex),
                            filePath = it.getString(dataIndex)
                        )
                    )
                }
            }
            musicList
        }
    }


    @Composable
    fun MusicScreen() {
        val musicListState = remember { mutableStateOf<List<Music>>(emptyList()) }

        // Fetch music list using LaunchedEffect
        LaunchedEffect(Unit) {
            musicListState.value = getMusicList()
        }

        // Display music list
        Column {
            Text("라이브러리")
            MusicList(musicList = musicListState.value)
        }
    }

    @Composable
    fun MusicList(musicList: List<Music>) {
        LazyColumn {
            items(musicList) { music ->
                MusicItem(music = music, onClick = {
                    val intent = Intent(this@MainActivity, MusicActivity::class.java)
                    intent.putExtra("musicFilePath", music.filePath)
                    intent.putExtra("musicTitle", music.title)
                    startActivity(intent)
                })
            }
        }
    }
    @Composable
    fun MusicItem(music: Music, onClick: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            Text(text = music.title, style = MaterialTheme.typography.titleMedium)
            Text(text = music.artist, style = MaterialTheme.typography.bodySmall)
        }
    }
}

