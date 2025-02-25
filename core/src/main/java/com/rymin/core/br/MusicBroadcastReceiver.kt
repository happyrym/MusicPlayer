package com.rymin.core.br

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rymin.common.config.Constants
import com.rymin.core.service.MusicPlayerService

class MusicBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        when (action) {
            Constants.ACTION_STOP_FOREGROUND -> {
                val serviceIntent = Intent(context, MusicPlayerService::class.java)
                serviceIntent.action = Constants.ACTION_STOP_FOREGROUND
                context.stopService(serviceIntent)
            }
            else -> {
                val serviceIntent = Intent(context, MusicPlayerService::class.java)
                serviceIntent.action = action
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
