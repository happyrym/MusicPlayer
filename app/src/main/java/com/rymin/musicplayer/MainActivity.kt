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
    private var musicPlayerService: MusicPlayerService? = null
    private var isServiceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MusicListScreen(
                viewModel = viewModel,
                onMusicSelected = { music -> viewModel.playMusic(music) }
            )
        }

        Intent(this, MusicPlayerService::class.java).also { intent ->
            startForegroundService(intent)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.MusicPlayerBinder
            musicPlayerService = binder.getService()
            viewModel.bindService(binder.getService())
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Timber.d("rymins stop")
            musicPlayerService = null
            isServiceBound = false
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        viewModel.unbindService()
        unbindService(serviceConnection)
    }
}
