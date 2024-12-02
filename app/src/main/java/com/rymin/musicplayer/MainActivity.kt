package com.rymin.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import com.rymin.musicplayer.ui.MusicListScreen
import com.rymin.musicplayer.viewmodel.MusicListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MusicListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MusicListScreen(
                viewModel = viewModel,
                onMusicSelected = { music -> viewModel.playMusic(music) }
            )
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.selectedAlbum.value != null) {
                    viewModel.showAlbumList()
                } else {
                    finish()
                }
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.unbindService()
    }
}
