package com.jzh.demo.mvp.base.mvp

import com.guanwei.globe.base.mvp.BaseModel
import com.guanwei.globe.base.mvp.BaseView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BasePresenter<V : BaseView, M : BaseModel>
/*如果当前页面需要view 则使用此构造*/
constructor(view: V) : IPresenter {

    protected var mView: V? = null
    protected var mModel: M? = null

    /**
     * 1、在UI层创建的时候（比如onCreate之类的），实例化CompositeDisposable；
     * 2、把subscribe订阅返回的Disposable对象加入管理器；
     * 3、UI销毁时清空订阅的对象。
     */
    private var mCompositeDisposable: CompositeDisposable? = null

    init {
        attachView()
    }

    init {
        mView = view
        attachView()
    }

    /*如果当前页面需要view与model 则使用此构造*/
    constructor(view: V, model: M) : this(view) {
        mModel = model
    }

    /**
     * 停止正在执行的 RxJava 任务,避免内存泄漏
     */
    protected fun addDisposable(disposable: Disposable) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = CompositeDisposable()
        }
        //将所有 Disposable 放入集中处理
        mCompositeDisposable!!.add(disposable)
    }

    private fun unDispose() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable!!.clear()
            mCompositeDisposable = null
        }
    }

    override fun attachView() {

    }

    override fun detachView() {
        if (mView != null) {
            mView = null
        }
        unDispose()
    }
}