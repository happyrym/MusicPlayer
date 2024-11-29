package com.rymin.musicplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rymin.musicplayer.data.Music
import com.rymin.musicplayer.viewmodel.MusicListViewModel

@Composable
fun MusicListScreen(
    viewModel: MusicListViewModel,
    onMusicSelected: (Music) -> Unit
) {
    val musicList by viewModel.musicList.collectAsState(emptyList())
    val currentMusic by viewModel.currentMusic.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val duration by viewModel.duration.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Music Library", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
        ) {
            items(musicList) { music ->
                MusicItem(music = music, onClick = { onMusicSelected(music) })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        currentMusic?.let { music ->
            MusicPlayerControls(
                musicTitle = music.title,
                currentPosition = currentPosition,
                duration = duration,
                isPlaying = isPlaying,
                onPlayPauseClick = viewModel::playOrPauseMusic,
                onSeek = viewModel::seekToPosition
            )
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
        Text(text = music.title, style = MaterialTheme.typography.bodyMedium)
        Text(text = music.artist, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun MusicPlayerControls(
    musicTitle: String,
    currentPosition: Float,
    duration: Float,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onSeek: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Now Playing: $musicTitle", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = currentPosition,
            valueRange = 0f..duration,
            onValueChange = { position -> onSeek(position) },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "${formatTime(currentPosition.toLong())} / ${formatTime(duration.toLong())}",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onPlayPauseClick() }) {
            Text(if (isPlaying) "Pause" else "Play")
        }
    }
}

fun formatTime(milliseconds: Long): String {
    val minutes = (milliseconds / 1000) / 60
    val seconds = (milliseconds / 1000) % 60
    return "%02d:%02d".format(minutes, seconds)
}
