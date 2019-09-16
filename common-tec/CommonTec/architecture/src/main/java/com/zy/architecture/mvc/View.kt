package com.zy.architecture.mvc

/**
 * Created by zhangyi on 2019-09-14
 */

interface IView {
    fun setController(controller: IController)
    fun dataHanding()
    fun onDataHandled(data: String)
}
