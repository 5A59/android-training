package com.zy.architecture.mvvm

import android.arch.lifecycle.MutableLiveData
import android.os.Handler
import android.os.Looper
import android.text.TextUtils

/**
 * Created by zhangyi on 19-9-17
 */

interface IViewModel {
    fun setModel(model: IModel)
    fun handleText(text: String?)
    fun clearData()
    fun dataHandled(data: String?)
    fun dataCleared()
}

class ViewModel : IViewModel {
    private var model: IModel? = null
    var inputText: MutableLiveData<String> = MutableLiveData()
    var handledText: MutableLiveData<String> = MutableLiveData()

    init {
        inputText.observeForever {
            handleText(it)
        }
        handledText.value = "default msg"
    }

    override fun handleText(text: String?) {
        if (TextUtils.isEmpty(text)) {
            handledText.value = "default msg"
            return
        }
        handledText.value = "handle data ..."
        model?.handleData(text)
    }

    override fun clearData() {
        model?.clearData()
    }

    override fun setModel(model: IModel) {
        this.model = model
        model.setViewModel(this)
    }

    override fun dataHandled(data: String?) {
        handledText.value = data
    }

    override fun dataCleared() {
        inputText.value = ""
    }
}
