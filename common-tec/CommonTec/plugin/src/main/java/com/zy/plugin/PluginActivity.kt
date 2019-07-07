package com.zy.plugin

import android.os.Bundle
import com.zy.commontec.activity.ainterface.iplugin.BasePluginActivity

class PluginActivity : BasePluginActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val textView = TextView(this)
//        textView.text = "aaa"
//        setContentView(textView)
        setContentView(R.layout.activity_main)
    }
}