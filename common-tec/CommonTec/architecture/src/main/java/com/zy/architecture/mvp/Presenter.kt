package com.zy.architecture.mvp

/**
 * Created by zhangyi on 2019-09-15
 */

interface IPresenter {
    fun setView(view: IView)
    fun setModel(model: IModel)
    fun dataHandled(data: String)
    fun dataCleared()
    fun onTextChanged(text: String)
    fun onClearBtnClicked()
}

class Presenter : IPresenter {
    private var model: IModel? = null
    private var view: IView? = null

    override fun setModel(model: IModel) {
        this.model = model
    }

    override fun setView(view: IView) {
        this.view = view
    }

    override fun dataHandled(data: String) {
        view?.showData(data)
    }

    override fun dataCleared() {
        view?.showData("")
    }

    override fun onTextChanged(text: String) {
        view?.loading()
        model?.handleData(text)
    }

    override fun onClearBtnClicked() {
        model?.clearData()
    }
}
