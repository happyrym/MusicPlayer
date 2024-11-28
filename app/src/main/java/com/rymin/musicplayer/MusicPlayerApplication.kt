package com.rymin.musicplayer

import android.app.Application
import android.util.Log
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber
import com.rymin.musicplayer.di.modules

class MusicPlayerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("rymins"," start")
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MusicPlayerApplication)
//            modules(modules)
        }
        // Timber 초기화
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.i("MusicPlayerApplication started")
    }
}
