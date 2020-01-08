package com.provectus_it.bookme.ui.custom_view

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.WindowManager
import androidx.viewpager.widget.ViewPager
import kotlin.math.sqrt

class FragmentViewPager : ViewPager {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setScreenWidth(context)
    }

    private val density = resources.displayMetrics.density

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        val eventX = event!!.x / density
        val eventY = event.y / density
        val width = SCREEN_WIDTH

        return if ((eventX > width / 2) && (eventX < width - PADDING_FROM_SCREEN_END) && IS_MAIN_CONTAINER && !isBackSwipeButton(eventX, eventY)) false
        else super.onInterceptTouchEvent(event)
    }

    private fun isBackSwipeButton(eventX: Float, eventY: Float): Boolean {
        val buttonCenterX = SCREEN_WIDTH - PADDING_FROM_SCREEN_END - BACK_SWIPE_BUTTON_RADIUS
        val buttonCenterY = SCREEN_HEIGHT / 2
        val distanceBetweenSwipeAndCenter = sqrt((eventX - buttonCenterX) * (eventX - buttonCenterX) + (eventY - buttonCenterY) * (eventY - buttonCenterY))

        return distanceBetweenSwipeAndCenter <= BACK_SWIPE_BUTTON_RADIUS
    }

    private fun setScreenWidth(context: Context) {
        val windowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE)
        val display = (windowManager as WindowManager).defaultDisplay
        val size = Point()
        display.getSize(size)
        SCREEN_WIDTH = size.x / density
        SCREEN_HEIGHT = size.y / density
    }

    companion object {
        const val BACK_SWIPE_BUTTON_RADIUS = 32
        const val PADDING_FROM_SCREEN_END = 8
        var IS_MAIN_CONTAINER = false
        var SCREEN_WIDTH = 0F
        var SCREEN_HEIGHT = 0F
    }

}