package com.madaoh

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.IdRes
import android.view.*

/**
 * Created by tianpeng on 16/9/7.
 */
object StatusBarUtils {

    fun setStatusBarTransparent(activity: Activity?) {
        if (activity == null) {
            return
        }

        val window = activity.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSystemUiVisibility(window)
            window.statusBarColor = Color.TRANSPARENT
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setSystemUiVisibility(window)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    private fun setSystemUiVisibility(window: Window) {
        val decorView = window.decorView
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        decorView.systemUiVisibility = option
    }

    fun addStatusBarHeightAndSetPaddingByViewID(activity: Activity, @IdRes viewID: Int) {
        addStatusBarHeightAndSetPadding(activity, viewID)
    }

    fun addStatusBarHeightAndSetPaddingByView(context: Activity, view: View) {
        if (view != null) {
            addStatusBarHeightAndSetPadding(context, view)
        }
    }

    /**
     * 给View加上状态栏的高度
     */
    private fun addStatusBarHeightAndSetPadding(context: Context, obj: Any) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }

        try {
            val view: View?
            if (obj is Int) {
                view = (context as Activity).window.decorView.findViewById<View>(obj)
            } else if (obj is View) {
                view = obj
            } else {
                return
            }

            view?.viewTreeObserver?.addOnPreDrawListener(
                    object : ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            val params = view.layoutParams

                            val statusBarHeight = getStatusBarHeight(
                                    context.applicationContext)
                            params.height = statusBarHeight + view.height
                            val l = view.paddingLeft
                            val r = view.paddingRight
                            val b = view.paddingBottom
                            view.setPadding(l, statusBarHeight, r, b)

                            view.viewTreeObserver.removeOnPreDrawListener(this)
                            return true
                        }
                    })
        } catch (e: Exception) {

        }

    }

    fun addStatusBarHeightAndSetMarginTopByViewId(context: Activity, @IdRes viewID: Int) {
        addStatusBarHeightAndSetMarginTop(context, viewID)

    }

    fun addStatusBarHeightAndSetMarginTopByView(context: Activity, view: View) {
        if (view != null) {
            addStatusBarHeightAndSetMarginTop(context, view)
        }
    }

    /**
     * 给View加上状态栏的高度
     */
    private fun addStatusBarHeightAndSetMarginTop(context: Context, obj: Any) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }

        try {
            val view: View?
            if (obj is Int) {
                view = (context as Activity).window.decorView.findViewById<View>(obj)
            } else if (obj is View) {
                view = obj
            } else {
                return
            }

            view?.viewTreeObserver?.addOnPreDrawListener(
                    object : ViewTreeObserver.OnPreDrawListener {
                        override fun onPreDraw(): Boolean {
                            var params = view.layoutParams

                            val statusBarHeight = getStatusBarHeight(
                                    context.applicationContext)
                            if (params is ViewGroup.MarginLayoutParams) {
                                params.topMargin = statusBarHeight
                                view.layoutParams = params
                            }

                            view.viewTreeObserver.removeOnPreDrawListener(this)
                            return true
                        }
                    })
        } catch (e: Exception) {

        }

    }

    fun getStatusBarHeight(context: Context?): Int {
        var result = 0
        if (context != null) {
            val resourceId = context.resources.getIdentifier("status_bar_height", "dimen",
                    "android")
            if (resourceId > 0) {
                result = context.resources.getDimensionPixelSize(resourceId)
            }
        }
        return result
    }

}
