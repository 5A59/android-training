package com.zy.plugin

import android.app.Service
import android.content.Intent
import android.os.IBinder

class PluginService : Service() {

    override fun onCreate() {
        Logger.d("plugin onCreate")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d("plugin onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Logger.d("plugin onDestory")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}