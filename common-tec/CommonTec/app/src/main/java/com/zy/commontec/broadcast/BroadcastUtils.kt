package com.zy.commontec.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter

class BroadcastUtils {
    companion object {
        private val broadcastMap = HashMap<String, BroadcastReceiver>()

        fun registerBroadcastReceiver(context: Context, classLoader: ClassLoader, action: String, broadcastName: String) {
            val receiver = classLoader.loadClass(broadcastName).newInstance() as BroadcastReceiver
            val intentFilter = IntentFilter(action)
            context.registerReceiver(receiver, intentFilter)
            broadcastMap[action] = receiver
        }

        fun unregisterBroadcastReceiver(context: Context, action: String) {
            val receiver = broadcastMap.remove(action)
            context.unregisterReceiver(receiver)
        }
    }
}