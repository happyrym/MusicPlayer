package com.rymin.musicplayer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rymin.common.config.Constants
import com.rymin.service.MusicPlayerService

class MusicBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Constants.ACTION_START_FOREGROUND) {
            val serviceIntent = Intent(context, MusicPlayerService::class.java)
            serviceIntent.action = Constants.ACTION_START_FOREGROUND
            context.startForegroundService(serviceIntent)
        } else if (action == Constants.ACTION_STOP_FOREGROUND) {
            val serviceIntent = Intent(context, MusicPlayerService::class.java)
            serviceIntent.action = Constants.ACTION_STOP_FOREGROUND
            context.stopService(serviceIntent)
        }
    }
}
