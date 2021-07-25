package com.guanwei.globe.ui.activity.main

import com.guanwei.globe.base.mvp.BaseModel
import com.guanwei.globe.base.mvp.BaseView
import com.guanwei.globe.http.BaseResponse
import io.reactivex.Observable
import okhttp3.RequestBody

interface MainContract {
    /**
     * 数据返回
     */
    interface MainView : BaseView {
        fun postLocation(response: BaseResponse<Any>)
    }

    /**
     * 数据请求
     */
    interface MainModel : BaseModel {
        fun postLocation(json: RequestBody): Observable<BaseResponse<Any>>
    }
}