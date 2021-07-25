package com.jzh.demo.mvp.base.activity

interface IActivity {

    /**
     * 初始化view
     */
    fun initView(): Int

    /**
     * 初始化数据
     */
    fun initData()

    /**
     * 初始化点击事件 事件回调
     */

    fun initListener()

    /**
     * 是否使用eventBus
     */
    fun useEventBus(): Boolean

}