package com.zy.commontec.activity.ainterface

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.zy.commontec.activity.StubBaseActivity
import com.zy.commontec.activity.ainterface.iplugin.IPluginActivity

class StubInterfaceActivity : StubBaseActivity() {
    private var activity: IPluginActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = activityClassLoader?.loadClass(activityName)?.newInstance() as IPluginActivity?
        activity?.attach(this)
        activity?.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        activity?.onStart()
    }

    override fun onResume() {
        super.onResume()
        activity?.onResume()
    }

    override fun onPause() {
        super.onPause()
        activity?.onPause()
    }

    override fun onStop() {
        super.onStop()
        activity?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.onDestroy()
    }

    companion object {
        fun startPluginActivity(context: Context, pluginPath: String, activityName: String) {
            val intent = Intent(context, StubInterfaceActivity::class.java)
            intent.putExtra("pluginPath", pluginPath)
            intent.putExtra("activityName", activityName)
            context.startActivity(intent)
        }
    }
}