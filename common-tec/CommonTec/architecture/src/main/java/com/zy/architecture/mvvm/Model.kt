package com.zy.architecture.mvvm

import android.os.Handler
import android.os.Looper
import android.text.TextUtils

/**
 * Created by zhangyi on 19-9-17
 */

class HandleModel : IModel {
    private var viewModel: IViewModel? = null
    private var handler = Handler(Looper.getMainLooper())

    override fun handleData(data: String?) {
        if (TextUtils.isEmpty(data)) {
            return
        }
        handler.removeCallbacksAndMessages(null)
        // 延迟来模拟网络或者磁盘操作
        handler.postDelayed({
            viewModel?.dataHandled("handled data: $data")
        }, 3000)
    }

    override fun clearData() {
        handler.removeCallbacksAndMessages(null)
        viewModel?.dataCleared()
    }

    override fun setViewModel(viewModel: IViewModel) {
        this.viewModel = viewModel
    }
}

interface IModel {
    fun setViewModel(viewModel: IViewModel)
    fun handleData(data: String?)
    fun clearData()
}
