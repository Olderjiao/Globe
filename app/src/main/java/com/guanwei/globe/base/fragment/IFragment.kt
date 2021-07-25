package com.jzh.demo.mvp.base.fragment

import android.os.Bundle
import io.reactivex.annotations.Nullable

interface IFragment {
    /**
     * 初始化数据
     */
    fun initData(@Nullable savedInstanceState: Bundle)

    /**
     * 是否使用eventBus
     */
    fun useEventBus(): Boolean

}