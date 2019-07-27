package com.zy.hotfix.native_hook

import java.lang.reflect.Method

class NativeHookUtils {
    companion object {
        init {
            System.loadLibrary("native-hook")
        }
    }

    external fun patch(fromMethod: Method, toMethod: Method)


    fun getMsg(): String {
        return "from hotfix"
    }
}