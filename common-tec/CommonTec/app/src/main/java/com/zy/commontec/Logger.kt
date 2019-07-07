package com.zy.commontec

import android.util.Log

class Logger {
    companion object {
        private const val TAG = "COMMON_TEC"
        fun d (msg: String) {
            Log.d(TAG, msg)
        }
    }
}