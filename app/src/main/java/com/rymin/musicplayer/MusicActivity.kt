package com.rymin.musicplayer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay


class MusicActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val musicFilePath = intent.getStringExtra("musicFilePath")
        val musicTitle = intent.getStringExtra("musicTitle")

        setContent {
            MusicPlayerScreen(musicTitle = musicTitle ?: "Unknown", musicFilePath = musicFilePath)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    @Composable
    fun MusicPlayerScreen(musicTitle: String, musicFilePath: String?) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        var currentPosition by remember { mutableFloatStateOf(0f) }
        var duration by remember { mutableFloatStateOf(0f) }
        var isPlaying by remember { mutableStateOf(false) }

        LaunchedEffect(musicFilePath) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, Uri.parse(musicFilePath))
                prepare()
                duration = this.duration.toFloat()
            }
        }

        // Coroutine으로 현재 재생 위치 업데이트
        LaunchedEffect(isPlaying) {
            while (isPlaying) {
                delay(1000) // 1초 간격으로 업데이트
                mediaPlayer?.let {
                    currentPosition = it.currentPosition.toFloat()
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(text = musicTitle, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Slider로 재생 위치 표시 및 조정
            Slider(
                value = currentPosition,
                valueRange = 0f..duration,
                onValueChange = { position ->
                    currentPosition = position
                },
                onValueChangeFinished = {
                    mediaPlayer?.seekTo(currentPosition.toInt())
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${formatTime(currentPosition.toLong())} / ${formatTime(duration.toLong())}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Play/Pause 버튼
            Button(
                onClick = {
                    if (isPlaying) {
                        mediaPlayer?.pause()
                    } else {
                        mediaPlayer?.start()
                    }
                    isPlaying = !isPlaying
                }
            ) {
                Text(if (isPlaying) "Pause" else "Play")
            }
        }
    }

    // 시간 포맷 변경 (ms -> mm:ss)
    private fun formatTime(milliseconds: Long): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    private fun playMusic(context: Context, filePath: String) {
        try {
            mediaPlayer?.release() // 기존 MediaPlayer 해제
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(filePath))
                prepare() // 준비
                start()   // 재생 시작
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to play music", Toast.LENGTH_SHORT).show()
        }
    }
}
