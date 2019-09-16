package com.zy.architecture.mvc

import android.os.Handler
import android.os.Looper
import android.text.TextUtils

/**
 * Created by zhangyi on 2019-09-14
 */

class HandleModel : IModel {
    private var view: IView? = null
    private var controller: IController? = null
    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun setController(controller: IController) {
        this.controller = controller
    }

    override fun setView(view: IView) {
        this.view = view
    }

    override fun handleData(data: String) {
        if (TextUtils.isEmpty(data)) {
            return
        }
        view?.dataHanding()
        handler.removeCallbacksAndMessages(null)
        // 延迟来模拟网络或者磁盘操作
        handler.postDelayed({
            view?.onDataHandled("handled data: $data")
        }, 3000)
    }

    override fun clearData() {
        handler.removeCallbacksAndMessages(null)
        view?.onDataHandled("")
    }
}

interface IModel {
    fun setView(view: IView)
    fun setController(controller: IController)
    fun handleData(data: String)
    fun clearData()
}
