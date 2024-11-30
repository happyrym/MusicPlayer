package com.rymin.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rymin.musicplayer.service.MusicPlayerService
import com.rymin.musicplayer.ui.MusicListScreen
import com.rymin.musicplayer.viewmodel.MusicListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MainActivity : ComponentActivity() {

    // Koin의 ViewModel 주입
    private val viewModel: MusicListViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MusicListScreen(
                viewModel = viewModel,
                onMusicSelected = { music -> viewModel.playMusic(music) }
            )
        }
    }

    override fun onBackPressed() {
        if (viewModel.selectedAlbum != null) {
            viewModel.showAlbumList()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.unbindService()
    }
}
