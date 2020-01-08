package com.provectus_it.bookme.dev_settings_panel

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import com.provectus_it.bookme.BuildConfig
import com.provectus_it.bookme.R

class ActivityViewModifier {

    fun <T : View> modify(view: T): T {
        val drawerLayout = view.findViewById(R.id.devSettingsDrawer) as DrawerLayout

        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerClosed(drawerView: View) {
                if (!BuildConfig.DEBUG) drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        })

        val layoutParams = DrawerLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )

        layoutParams.gravity = Gravity.END

        if (!BuildConfig.DEBUG) drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        val startView = LayoutInflater.from(view.context).inflate(R.layout.developer_settings_view, drawerLayout, false)

        drawerLayout.addView(startView, layoutParams)

        return view
    }
}