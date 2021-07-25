package com.guanwei.globe.ui.activity.main

import com.blankj.utilcode.util.ToastUtils
import com.guanwei.globe.http.BaseResponse
import com.guanwei.globe.rxjava.BaseObserver
import com.guanwei.globe.rxjava.RxTransformer
import com.jzh.demo.mvp.base.mvp.BasePresenter
import okhttp3.RequestBody

class MainPresenter(view: MainContract.MainView) :
    BasePresenter<MainContract.MainView, MainContract.MainModel>(view, MainModel()) {

    fun postLocation(json: RequestBody) {
        mModel
            ?.postLocation(json)
            ?.compose(RxTransformer.transformWithLoading(mView!!))
            ?.subscribe(object : BaseObserver<BaseResponse<Any>>() {
                override fun onSuccess(success: BaseResponse<Any>) {
                    mView!!.postLocation(success)
                }

                override fun onField(msg: String) {
                    ToastUtils.showLong(msg)
                }
            })
    }

}