package com.jzh.demo.mvp.base.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.jzh.demo.mvp.base.mvp.BasePresenter
import com.trello.rxlifecycle2.components.support.RxFragment
import org.greenrobot.eventbus.EventBus
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class BaseFragment<VB : ViewBinding, P : BasePresenter<*, *>> : RxFragment(), IFragment {

    protected var binding: VB? = null
    protected var mPresenter: P? = null

    /**
     * view
     */
    private var mRootView: View? = null

    /**
     * 上下文
     */
    private var mActivity: Activity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mActivity = activity
        mPresenter = createPresenter()

        if (mRootView == null) {
            mRootView = initVB()
            initData(savedInstanceState!!)
        }
        val parent: ViewGroup = mRootView?.parent as ViewGroup
        parent.removeView(mRootView)
        //如果使用eventBus
        if (useEventBus()) {
            EventBus.getDefault().register(this)
        }

        return mRootView
    }

    protected abstract fun createPresenter(): P

    /**
     * 初始化viewBinding
     */
    private fun initVB(): View {
        val genericSuperclass: Type? = javaClass.genericSuperclass
        val aClass: Class<*> =
            ((genericSuperclass as ParameterizedType).actualTypeArguments[0]) as Class<*>

        val declaredMethod: Method = aClass.getDeclaredMethod("inflate", LayoutInflater::class.java)

        binding = declaredMethod.invoke(null, layoutInflater) as VB
        return binding!!.root

    }

    override fun onDestroyView() {
        super.onDestroyView()

        //eventBus资源释放
        if (useEventBus()) {
            EventBus.getDefault().unregister(this)
        }

        if (mPresenter != null) {
            mPresenter?.detachView()
        }

    }

    override fun useEventBus(): Boolean = false

}