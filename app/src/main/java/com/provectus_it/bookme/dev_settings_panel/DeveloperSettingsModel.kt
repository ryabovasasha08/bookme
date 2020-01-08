package com.provectus_it.bookme.dev_settings_panel

import android.content.Context
import com.provectus_it.bookme.BuildConfig
import hu.supercluster.paperwork.Paperwork

class DeveloperSettingsModel(context: Context) {

    private val paperwork = Paperwork(context)

    fun getGitSha(): String {
        return paperwork.get("gitSha")
    }

    fun getBuildDate(): String {
        return paperwork.get("buildDate")
    }

    fun getBuildVersionCode(): String {
        return BuildConfig.VERSION_CODE.toString()
    }

    fun getBuildVersionName(): String {
        return BuildConfig.VERSION_NAME
    }
}