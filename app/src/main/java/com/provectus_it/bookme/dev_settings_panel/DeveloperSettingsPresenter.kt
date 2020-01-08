package com.provectus_it.bookme.dev_settings_panel

import android.content.Context
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.provectus_it.bookme.preference.Preference_DeveloperSettingsValues
import com.provectus_it.bookme.util.kiosk_mode.KioskModeManager
import leakcanary.LeakCanary
import org.koin.core.KoinComponent

@InjectViewState
class DeveloperSettingsPresenter(
        private val developerSettingsModel: DeveloperSettingsModel,
        private val kioskModeManager: KioskModeManager,
        private val developerSettingsValues: Preference_DeveloperSettingsValues
) : MvpPresenter<DeveloperSettingsView>(), KoinComponent {

    override fun onFirstViewAttach() {
        syncDeveloperSettings()
        viewState.setKioskModeSwitchChecked(kioskModeManager.isKioskModeEnabled())
        setupLeakCanary(developerSettingsValues.getIsLeakCanaryEnabled())
    }

    private fun syncDeveloperSettings() {
        viewState.setGitSha(developerSettingsModel.getGitSha())
        viewState.setBuildDate(developerSettingsModel.getBuildDate())
        viewState.setBuildVersionCode(developerSettingsModel.getBuildVersionCode())
        viewState.setBuildVersionName(developerSettingsModel.getBuildVersionName())
    }

    fun notifyKioskModeSwitchChecked(context: Context, isChecked: Boolean) {
        kioskModeManager.setKioskModeEnabled(context, isChecked)
    }

    fun notifyLeakCanarySwitchChecked(isChecked: Boolean) {
        developerSettingsValues.putIsLeakCanaryEnabled(isChecked)
        setupLeakCanary(isChecked)
    }

    private fun setupLeakCanary(isEnabled: Boolean) {
        viewState.setLeakCanarySwitchChecked(isEnabled)
        LeakCanary.config = LeakCanary.config.copy(dumpHeap = isEnabled)
    }
}
