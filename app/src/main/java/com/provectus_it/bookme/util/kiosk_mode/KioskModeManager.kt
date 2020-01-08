package com.provectus_it.bookme.util.kiosk_mode

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.*
import android.widget.Toast
import com.provectus_it.bookme.R
import com.provectus_it.bookme.preference.Preference_KioskModeValues
import com.provectus_it.bookme.ui.custom_view.InterceptViewGroup
import org.koin.core.KoinComponent
import timber.log.Timber

class KioskModeManager(
        private val kioskModePreference: Preference_KioskModeValues
) : KoinComponent {

    private var windowManager: WindowManager? = null
    private var interceptView: InterceptViewGroup? = null

    fun isKioskModeEnabled(): Boolean = kioskModePreference.getIsKioskModeEnabled()

    fun setKioskModeEnabled(context: Context, isEnabled: Boolean) {
        if (isEnabled)
            lockStatusBar(context)
        else {
            unlockStatusBar()
        }

        kioskModePreference.putIsKioskModeEnabled(isEnabled)
    }

    internal fun moveTaskToFront(activity: Activity) {
        val isLocked = isKioskModeEnabled()

        if (!isLocked) return

        (activity.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .moveTaskToFront(activity.taskId, ActivityManager.MOVE_TASK_WITH_HOME)
        Toast.makeText(activity, activity.resources.getString(R.string.you_are_not_allowed_to_leave_the_app), Toast.LENGTH_SHORT).show()
    }

    private fun lockStatusBar(context: Context) {
        windowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams()
        params.apply {
            type = TYPE_SYSTEM_ERROR
            gravity = Gravity.TOP
            flags = FLAG_NOT_FOCUSABLE or FLAG_NOT_TOUCH_MODAL or FLAG_LAYOUT_IN_SCREEN
            width = MATCH_PARENT
            val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            var result = 0
            if (resId > 0) result = context.resources.getDimensionPixelSize(resId)
            height = result
            format = PixelFormat.TRANSPARENT
        }
        interceptView = InterceptViewGroup(context)

        try {
            windowManager!!.addView(interceptView, params)
        } catch (e: RuntimeException) {
            Timber.e(e, "Failed to add view to window manager")
        }
    }

    private fun unlockStatusBar() {
        if (windowManager == null || interceptView == null || !interceptView!!.isShown) return

        try {
            windowManager!!.removeView(interceptView)
        } catch (e: RuntimeException) {
            Timber.e(e, "Failed to remove view from window manager")
        }
    }

}
