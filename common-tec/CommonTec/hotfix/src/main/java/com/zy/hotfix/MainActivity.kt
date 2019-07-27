package com.zy.hotfix

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.zy.hotfix.insert_dex.InsertDexUtils
import com.zy.hotfix.instant_run.InstantRunUtils
import com.zy.hotfix.native_hook.NativeHookUtils
import dalvik.system.DexClassLoader
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

// jni 开发配置：https://developer.android.com/ndk/guides/?hl=zh-CN
class MainActivity : AppCompatActivity() {
    private lateinit var pluginPath: String
    private lateinit var nativeLibDir: File
    private lateinit var dexOutPath: File
    private lateinit var pluginClassLoader: DexClassLoader

    private var nativeHookUtils = NativeHookUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        initPatch()
        // native hook
        nativeHookText.setOnClickListener {
            val toMethod = pluginClassLoader.loadClass("com.zy.hotfix.native_hook.PatchNativeHookUtils").getMethod("getMsg")
            val fromMethod = nativeHookUtils.javaClass.getMethod("getMsg")
            nativeHookUtils.patch(fromMethod, toMethod)
        }
        showNativeHookMsgText.setOnClickListener {
            Toast.makeText(this, nativeHookUtils.getMsg(), Toast.LENGTH_LONG).show()
        }

        // insert dex
        insertDexText.setOnClickListener {
            // 不能及时生效，已经加载过的 class 必须重新加载
            InsertDexUtils.injectDexAtFirst(pluginPath, dexOutPath.absolutePath)
        }
        startPatchActivityText.setOnClickListener {
            PatchActivity.start(this)
        }

        // instant run
        instantRunText.setOnClickListener {
            InstantRunUtils.inject(pluginClassLoader)
        }
        uninstallInstantRunText.setOnClickListener {
            InstantRunUtils.unInject(pluginClassLoader)
        }
        showInstantRunMsgText.setOnClickListener {
            val util = InstantRunUtils()
            Toast.makeText(this, util.msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun initPatch() {
        extractPlugin()
        pluginPath = File(filesDir.absolutePath, "patch.apk").absolutePath
        nativeLibDir = File(filesDir, "patchlib")
        dexOutPath = File(filesDir, "dexout")
        if (!dexOutPath.exists()) {
            dexOutPath.mkdirs()
        }
        pluginClassLoader = DexClassLoader(pluginPath, dexOutPath.absolutePath, nativeLibDir.absolutePath, this::class.java.classLoader)
    }

    private fun extractPlugin() {
        var inputStream = assets.open("patch.apk")
        File(filesDir.absolutePath, "patch.apk").writeBytes(inputStream.readBytes())
    }
}