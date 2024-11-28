package com.rymin.musicplayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rymin.musicplayer.data.Music

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicListScreen(
//    viewModel: MusicListViewModel, // ViewModel 연결
    onMusicSelected: (Music) -> Unit // 음악 선택 시 동작 (상세 화면 이동)
) {
//    val isLoading by viewModel.isLoading.collectAsState()
val isLoading = false
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Music Player", style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
            }
        }
    }
}

@Composable
fun MusicListItem(music: Music, onMusicClick: (Music) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMusicClick(music) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(
                text = music.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = music.artist,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "${music.duration / 1000}s",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
