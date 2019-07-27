package com.zy.hotfix

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_patch.*

class PatchActivity : Activity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, PatchActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patch)
        text.setOnClickListener {
            val utils = HotfixUtils()
            utils.toast(this)
        }
    }
}