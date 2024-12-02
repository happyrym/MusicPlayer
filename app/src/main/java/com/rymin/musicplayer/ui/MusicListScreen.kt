package com.rymin.musicplayer.ui

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rymin.common.data.Album
import com.rymin.common.data.Music
import com.rymin.common.utils.TimeUtils
import com.rymin.musicplayer.R
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
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()
    val albumList by viewModel.albumList.collectAsState()
    val isLoop by viewModel.isLoop.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val volume by viewModel.volume.collectAsState()

    var isBottomSheetVisible by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(currentPosition) }

    LaunchedEffect(currentPosition) {
        sliderPosition = currentPosition
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Music Library", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))
        if (selectedAlbum == null) {
            Box(
                Modifier
                    .weight(1f)
            ) {
                AlbumGridView(albumList,
                    onAlbumClick = { album ->
                        viewModel.selectedAlbum(album)
                    })
            }
        } else {
            IconButton(
                onClick = { viewModel.showAlbumList() },
                modifier = Modifier.padding(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_btn_back),
                    contentDescription = "Back Button",
                    modifier = Modifier.size(24.dp) // 아이콘 크기 설정
                )
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
            ) {
                items(musicList) { music ->
                    MusicItem(music = music, onClick = { onMusicSelected(music) })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        currentMusic?.let { music ->
            Box(Modifier
                .clickable {
                    viewModel.getVolume()
                    isBottomSheetVisible = true
                }) {
                MusicPlayerControls(
                    musicTitle = music.title,
                    artistName = music.artist,
                    currentPosition = currentPosition,
                    duration = duration,
                    isPlaying = isPlaying,
                    isLoop = isLoop,
                    isShuffle = isShuffle,
                    onPlayPauseClick = viewModel::playOrPauseMusic,
                    onSeek = viewModel::seekToPosition,
                    onNextClick = viewModel::playNextMusic,
                    onPrevClick = viewModel::playPrevMusic,
                    onLoopClick = viewModel::changeLoopMode,
                    onShuffleClick = viewModel::changeShuffleMode,
                )
            }
            if (isBottomSheetVisible) {
                MusicInfoBottomSheet(
                    music = music,
                    widget = {
                        MusicPlayerControls(
                            musicTitle = music.title,
                            artistName = music.artist,
                            currentPosition = currentPosition,
                            duration = duration,
                            isPlaying = isPlaying,
                            isLoop = isLoop,
                            isShuffle = isShuffle,
                            onPlayPauseClick = viewModel::playOrPauseMusic,
                            onSeek = viewModel::seekToPosition,
                            onNextClick = viewModel::playNextMusic,
                            onPrevClick = viewModel::playPrevMusic,
                            onLoopClick = viewModel::changeLoopMode,
                            onShuffleClick = viewModel::changeShuffleMode,
                        )
                    },
                    volume = volume,
                    setVolume = { value -> viewModel.setVolume( value) },
                    onDismiss = { isBottomSheetVisible = false }
                )
            }
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
fun AlbumGridView(albums: List<Album>, onAlbumClick: (Album) -> Unit) {

    LazyVerticalGrid(
        columns = GridCells.Adaptive(128.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(albums) { album ->
            AlbumItem(album = album, onAlbumClick = onAlbumClick)
        }
    }
}

@Composable
fun AlbumItem(album: Album, onAlbumClick: (Album) -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onAlbumClick(album) }
    ) {
        val context = LocalContext.current
        when (val albumArt = getAlbumArt(context, album.id)) {
            is Bitmap -> Image(
                bitmap = albumArt.asImageBitmap(),
                contentDescription = album.title,
                modifier = Modifier
                    .size(128.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            is Uri -> AsyncImage(
                model = albumArt,
                contentDescription = album.title,
                modifier = Modifier
                    .size(128.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            else -> Image(
                painter = painterResource(R.drawable.ic_btn_list),
                contentDescription = album.title,
                modifier = Modifier
                    .size(128.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = album.title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = album.artist,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

}

fun getThumbnailUri(context: Context, albumId: Long): Bitmap? {
    val albumUri = ContentUris.withAppendedId(
        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
        albumId
    )

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(
                albumUri,
                Size(128, 128),
                null
            )
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getLegacyAlbumArtUri(context: Context, albumId: Long): Uri? {
    val albumUri = ContentUris.withAppendedId(
        Uri.parse("content://media/external/audio/albumart"),
        albumId
    )
    return albumUri
}

fun getAlbumArt(context: Context, albumId: Long): Any? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getThumbnailUri(context, albumId)
    } else {
        getLegacyAlbumArtUri(context, albumId)
    }
}

@Composable
fun MusicPlayerControls(
    musicTitle: String,
    artistName: String,
    currentPosition: Float,
    duration: Float,
    isPlaying: Boolean,
    isLoop: Boolean,
    isShuffle: Boolean,
    onPlayPauseClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onLoopClick: () -> Unit,
    onShuffleClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = " $musicTitle - $artistName", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = currentPosition,
            valueRange = 0f..duration,
            onValueChange = { position -> onSeek(position) },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "${TimeUtils.formatTime(currentPosition.toLong())} / ${
                TimeUtils.formatTime(
                    duration.toLong()
                )
            }",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            ShuffleButton(isShuffle, onClick = { onShuffleClick() })
            IconButton(
                onClick = { onPrevClick() },
                modifier = Modifier.padding(8.dp)
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.ic_btn_prev),
                    contentScale = ContentScale.Fit,
                    contentDescription = "Prev Button",
                )
            }
            IconButton(
                onClick = { onPlayPauseClick() },
                modifier = Modifier.padding(8.dp)
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = if (isPlaying) R.drawable.ic_btn_pause else R.drawable.ic_btn_play),
                    contentScale = ContentScale.Fit,
                    contentDescription = "Play/Pause Button",
                )
            }
            IconButton(
                onClick = { onNextClick() },
                modifier = Modifier.padding(8.dp)
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(id = R.drawable.ic_btn_next),
                    contentScale = ContentScale.Fit,
                    contentDescription = "Next Button",
                )
            }
            LoopButton(isLoop, onClick = { onLoopClick() })
        }
    }

}

@Composable
fun ShuffleButton(isShuffle: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = { onClick() },
        modifier = Modifier.padding(8.dp)
    ) {
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = if (!isShuffle) R.drawable.ic_btn_shuffle_disable else R.drawable.ic_btn_shuffle),
            contentScale = ContentScale.Fit,
            contentDescription = "Loop Button",
            colorFilter = if (!isShuffle) ColorFilter.tint(Color.Gray) else null,
        )
    }
}

@Composable
fun LoopButton(isLoop: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = { onClick() },
        modifier = Modifier.padding(8.dp)
    ) {
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = if (!isLoop) R.drawable.ic_btn_loop_disable else R.drawable.ic_btn_loop),
            contentScale = ContentScale.Fit,
            contentDescription = "Loop Button",
            colorFilter = if (!isLoop) ColorFilter.tint(Color.Gray) else null,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicInfoBottomSheet(
    music: Music,
    volume: Float,
    setVolume: (value: Float) -> Unit,
    widget: @Composable () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val context = LocalContext.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Top)
                        .padding(start = 32.dp)
                ) {
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center // Box 내 중앙 정렬
                ) {
                    when (val albumArt = getAlbumArt(context, music.albumId)) {
                        is Bitmap -> Image(
                            bitmap = albumArt.asImageBitmap(),
                            contentDescription = "Album Art",
                            modifier = Modifier
                                .size(128.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop
                        )

                        is Uri -> AsyncImage(
                            model = albumArt,
                            contentDescription = "Album Art",
                            modifier = Modifier
                                .size(128.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop
                        )

                        else -> Image(
                            painter = painterResource(R.drawable.ic_btn_list),
                            contentDescription = "Default Album Art",
                            modifier = Modifier
                                .size(128.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.Top)
                        .padding(end = 8.dp)
                ) {
                    Button(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .size(36.dp)
                    ) {
                        Text(text = "X")
                    }
                }
            }
            VolumeController(volume, setVolume)
            Spacer(modifier = Modifier.height(16.dp))
            widget()
        }
    }
}

@Composable
fun VolumeController(
    volume: Float,
    setVolume: (value: Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Volume", style = MaterialTheme.typography.titleMedium)

        Slider(
            value = volume,
            onValueChange = { newValue ->
                setVolume(newValue) // 볼륨 값 업데이트
            },
            valueRange = 0f..1f, // 볼륨 범위 (0 ~ 1)
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Text(
            text = "Current Volume: ${(volume * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall
        )
    }
}