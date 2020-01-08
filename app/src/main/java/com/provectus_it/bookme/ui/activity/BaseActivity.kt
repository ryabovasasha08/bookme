package com.provectus_it.bookme.ui.activity

import android.view.View.*
import android.widget.Toast
import androidx.annotation.LayoutRes
import com.arellomobile.mvp.MvpAppCompatActivity
import com.provectus_it.bookme.R
import com.provectus_it.bookme.dev_settings_panel.ActivityViewModifier
import com.provectus_it.bookme.util.kiosk_mode.KioskModeManager
import leakcanary.AppWatcher
import org.koin.android.ext.android.get

abstract class BaseActivity : MvpAppCompatActivity() {

    var kioskModeManager = get<KioskModeManager>()

    override fun setContentView(@LayoutRes layoutID: Int) {
        super.setContentView(ActivityViewModifier().modify(layoutInflater.inflate(layoutID, null)))
    }

    override fun onBackPressed() {
        if (kioskModeManager.isKioskModeEnabled()) {
            Toast.makeText(applicationContext, resources.getString(R.string.you_are_not_allowed_to_leave_the_app), Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        kioskModeManager.moveTaskToFront(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppWatcher.objectWatcher.watch(this)
    }

    protected fun hideNavigationBar() {
        window.decorView.systemUiVisibility = (SYSTEM_UI_FLAG_HIDE_NAVIGATION or SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    protected fun hideSystemUi() {
        window.decorView.systemUiVisibility = (
                SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or SYSTEM_UI_FLAG_FULLSCREEN
                        or SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

}