package com.zy.commontec

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.zy.commontec.activity.reflect.StubReflectActivity
import com.zy.commontec.activity.ainterface.StubInterfaceActivity
import com.zy.commontec.activity.hook.AppInstrumentation
import com.zy.commontec.activity.hook.PluginContext
import com.zy.commontec.broadcast.BroadcastUtils
import com.zy.commontec.contentprovider.PluginUtils
import com.zy.commontec.service.StubService
import dalvik.system.DexClassLoader
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var pluginClassLoader: DexClassLoader
    private lateinit var pluginPath: String
    private lateinit var activityName: String
    private lateinit var nativeLibDir: File
    private lateinit var dexOutPath: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

        // activity
        loadInterfaceText.setOnClickListener {
            StubInterfaceActivity.startPluginActivity(this, pluginPath, activityName)
        }
        loadReflectText.setOnClickListener {
            StubReflectActivity.startPluginActivity(this, pluginPath, activityName)
        }
        loadHookText.setOnClickListener {
            val intent = Intent()
            intent.setClass(this, pluginClassLoader.loadClass(activityName))
            startActivity(intent)
        }

        // service
        loadServiceText.setOnClickListener {
            StubService.startService(this, pluginClassLoader, "com.zy.plugin.PluginService")
        }

        // broadcast
        registerBroadcastText.setOnClickListener {
            BroadcastUtils.registerBroadcastReceiver(this, pluginClassLoader, "test_plugin_broadcast", "com.zy.plugin.PluginBroadcastReceiver")
        }
        unregisterBroadcastText.setOnClickListener {
            BroadcastUtils.unregisterBroadcastReceiver(this, "test_plugin_broadcast")
        }
        sendBroadcastText.setOnClickListener {
            val intent = Intent()
            intent.action = "test_plugin_broadcast"
            sendBroadcast(intent)
        }

        // content provider
        queryContentProviderText.setOnClickListener {
            val uri = Uri.parse("content://com.zy.stubprovider/plugin1")
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor.moveToFirst()
            val res = cursor.getString(0)
            Logger.d("provider query res: $res")
        }
        queryContentProviderText.setOnClickListener {
            val uri = Uri.parse("content://com.zy.stubprovider/plugin2")
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor.moveToFirst()
            val res = cursor.getString(0)
            Logger.d("provider query res: $res")
        }
    }

    private fun init() {
        extractPlugin()
        pluginPath = File(filesDir.absolutePath, "plugin.apk").absolutePath
        activityName = "com.zy.plugin.PluginActivity"
        nativeLibDir = File(filesDir, "pluginlib")
        dexOutPath = File(filesDir, "dexout")
        if (!dexOutPath.exists()) {
            dexOutPath.mkdirs()
        }
        pluginClassLoader = DexClassLoader(pluginPath, dexOutPath.absolutePath, nativeLibDir.absolutePath, this::class.java.classLoader)
        PluginUtils.classLoader = pluginClassLoader
        AppInstrumentation.inject(this, PluginContext(pluginPath, this, application, pluginClassLoader))
    }

    private fun extractPlugin() {
        var inputStream = assets.open("plugin.apk")
        File(filesDir.absolutePath, "plugin.apk").writeBytes(inputStream.readBytes())
    }
}
