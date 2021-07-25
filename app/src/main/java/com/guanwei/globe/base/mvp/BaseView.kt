package com.guanwei.globe.base.mvp

import com.trello.rxlifecycle2.LifecycleTransformer

interface BaseView {
    fun <T> bindToLifecycle(): LifecycleTransformer<T>

    //显示加载
    fun showLoading()

    //隐藏加载
    fun hideLoading()

}