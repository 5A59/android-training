package com.zy.commontec.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

class StubService : Service() {
    var serviceName: String? = null
    var pluginService: Service? = null

    companion object {
        var pluginClassLoader: ClassLoader? = null
        fun startService(context: Context, classLoader: ClassLoader, serviceName: String) {
            pluginClassLoader = classLoader
            val intent = Intent(context, StubService::class.java)
            intent.putExtra("serviceName", serviceName)
            context.startService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val res = super.onStartCommand(intent, flags, startId)
        serviceName = intent?.getStringExtra("serviceName")
        pluginService = pluginClassLoader?.loadClass(serviceName)?.newInstance() as Service
        pluginService?.onCreate()
        return pluginService?.onStartCommand(intent, flags, startId) ?: res
    }

    override fun onDestroy() {
        super.onDestroy()
        pluginService?.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}