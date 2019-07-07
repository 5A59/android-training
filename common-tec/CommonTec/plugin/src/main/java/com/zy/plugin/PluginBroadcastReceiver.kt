package com.zy.plugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PluginBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Logger.d("receive broadcast in plugin")
    }
}