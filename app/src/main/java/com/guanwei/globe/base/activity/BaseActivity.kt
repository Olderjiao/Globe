package com.jzh.demo.mvp.base.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.guanwei.globe.R
import com.gyf.barlibrary.ImmersionBar
import com.jzh.demo.mvp.base.mvp.BasePresenter
import com.jzh.demo.mvp.base.mvp.IPresenter
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import org.greenrobot.eventbus.EventBus
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class BaseActivity<VB : ViewBinding, P : BasePresenter<*, *>> : RxAppCompatActivity(),
    IActivity {

    protected var binding: VB? = null
    protected var mPresenter: P? = null

    //沉浸状态栏管理
    protected var mImmersionBar: ImmersionBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter = createPresenter()

        initVB()
        if (isImmersionBarEnabled()) {
            //设置沉浸式状态栏
            initImmersionBar()
        }

        //是否使用eventBus
        if (useEventBus()) {
            EventBus.getDefault().register(this)
        }
        initData()
        initListener()
    }

    private fun initVB() {
        val genericSuperclass: Type? = javaClass.genericSuperclass
        val aClass: Class<*> =
            ((genericSuperclass as ParameterizedType).actualTypeArguments[0]) as Class<*>

        val declaredMethod: Method = aClass.getDeclaredMethod("inflate", LayoutInflater::class.java)

        binding = (declaredMethod.invoke(null, layoutInflater)) as VB
        setContentView(binding?.root)

    }

    /**
     * 沉浸式状态栏
     */
    protected fun initImmersionBar() {
        mImmersionBar = ImmersionBar
            .with(this).run {
                statusBarColor(R.color.colorPrimaryDark)
                fitsSystemWindows(true)
            }
        mImmersionBar?.init()
    }

    /**
     * 是否可以使用沉浸式状态栏
     */
    protected fun isImmersionBarEnabled(): Boolean {
        return false
    }

    protected abstract fun createPresenter(): P

    override fun onDestroy() {
        super.onDestroy()
        //如果有eventBus
        if (useEventBus()) {
            EventBus.getDefault().unregister(this)
        }

        if (mImmersionBar != null) {
            mImmersionBar?.destroy()
        }

        if (mPresenter != null) {
            mPresenter?.detachView()
        }

    }

    override fun useEventBus(): Boolean = false

}