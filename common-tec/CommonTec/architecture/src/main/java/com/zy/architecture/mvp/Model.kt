package com.zy.architecture.mvp

import android.os.Handler
import android.os.Looper
import android.text.TextUtils

/**
 * Created by zhangyi on 2019-09-14
 */

class HandleModel : IModel {
    private var presenter: IPresenter? = null
    private var handler = Handler(Looper.getMainLooper())

    override fun handleData(data: String) {
        if (TextUtils.isEmpty(data)) {
            return
        }
        handler.removeCallbacksAndMessages(null)
        // 延迟来模拟网络或者磁盘操作
        handler.postDelayed({
            presenter?.dataHandled("handled data: $data")
        }, 3000)
    }

    override fun clearData() {
        handler.removeCallbacksAndMessages(null)
        presenter?.dataCleared()
    }

    override fun setPresenter(presenter: IPresenter) {
        this.presenter = presenter
    }

}

interface IModel {
    fun setPresenter(presenter: IPresenter)
    fun handleData(data: String)
    fun clearData()
}
