package com.rymin.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rymin.musicplayer.ui.MusicListScreen
import com.rymin.musicplayer.viewmodel.MusicListViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MusicListViewModel = koinViewModel()
            MusicListScreen(
                viewModel = viewModel,
                onMusicSelected = { music -> viewModel.playMusic(music) }
            )
        }
    }
}
