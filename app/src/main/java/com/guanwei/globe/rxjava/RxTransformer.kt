package com.guanwei.globe.rxjava

import com.guanwei.globe.base.mvp.BaseView
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

object RxTransformer {
    /**
     * 界面请求  无参数线程切换
     */
    fun <T> transformer(): ObservableTransformer<T, T> {
        return ObservableTransformer<T, T> { observable: Observable<T> ->
            observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * 界面请求  不需要loading调用 使用RxLifeCycle
     */
    fun <T> transformer(view: BaseView): ObservableTransformer<T, T> {
        return ObservableTransformer { observable: Observable<T> ->
            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(view.bindToLifecycle())
        }
    }

    /**
     * 界面请求 需要加载和隐藏loading时调用,使用RxLifeCycle
     */
    @JvmStatic
    fun <T> transformWithLoading(view: BaseView): ObservableTransformer<T, T> {
        return ObservableTransformer { observable: Observable<T> ->
            observable
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    view.showLoading() //显示进度条
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally {
                    view.hideLoading() //隐藏进度条
                }.compose(view.bindToLifecycle())
        }
    }

}