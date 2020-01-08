package com.provectus_it.bookme.ui.custom_view

import android.content.Context
import android.view.MotionEvent
import android.view.ViewGroup

class InterceptViewGroup(context: Context) : ViewGroup(context) {

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        //no-op
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = true

}