package com.rymin.musicplayer

import android.app.Application
import timber.log.Timber

class MusicPlayerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Timber 초기화
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.i("MusicPlayerApplication started")
    }
}
