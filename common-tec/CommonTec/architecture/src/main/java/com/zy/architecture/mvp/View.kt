package com.zy.architecture.mvp

/**
 * Created by zhangyi on 2019-09-15
 */

interface IView {
    fun setPresenter(presenter: IPresenter)
    fun loading()
    fun showData(data: String)
}
