package com.zy.architecture.mvc

/**
 * Created by zhangyi on 2019-09-14
 */

interface IController {
    fun setModel(model: IModel)
    fun onDataChanged(data: String)
    fun clearData()
}