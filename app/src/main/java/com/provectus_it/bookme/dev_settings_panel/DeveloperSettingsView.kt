package com.provectus_it.bookme.dev_settings_panel

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface DeveloperSettingsView : MvpView {

    fun setGitSha(gitSha: String)

    fun setBuildDate(date: String)

    fun setBuildVersionCode(versionCode: String)

    fun setBuildVersionName(versionName: String)

    fun setKioskModeSwitchChecked(isChecked: Boolean = true)

    fun setLeakCanarySwitchChecked(isChecked: Boolean = true)
}
