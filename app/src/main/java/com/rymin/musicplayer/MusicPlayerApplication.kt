package com.rymin.musicplayer

import android.app.Application
import android.content.IntentFilter
import android.util.Log
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber
import com.rymin.musicplayer.di.modules
import com.rymin.musicplayer.service.MusicBroadcastReceiver
import com.rymin.musicplayer.utils.Constants

class MusicPlayerApplication : Application() {

    private val musicBroadcastReceiver = MusicBroadcastReceiver()

    override fun onCreate() {
        super.onCreate()
        Timber.i("MusicPlayerApplication onCreate")
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MusicPlayerApplication)
            modules(modules)
        }
        // Timber 초기화
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        registerMusicBroadcastReceiver()
    }

    private fun registerMusicBroadcastReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(Constants.ACTION_START_FOREGROUND)
            addAction(Constants.ACTION_STOP_FOREGROUND)
        }
        registerReceiver(musicBroadcastReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
        Timber.i("MusicBroadcastReceiver registered")
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(musicBroadcastReceiver)
        Timber.i("MusicBroadcastReceiver unregistered")
    }
}
