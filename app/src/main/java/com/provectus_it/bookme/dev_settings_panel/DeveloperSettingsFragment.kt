package com.provectus_it.bookme.dev_settings_panel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.provectus_it.bookme.R
import kotlinx.android.synthetic.main.fragment_developer_settings.*
import org.koin.android.ext.android.get

class DeveloperSettingsFragment : MvpAppCompatFragment(), DeveloperSettingsView {

    @InjectPresenter
    lateinit var presenter: DeveloperSettingsPresenter

    @ProvidePresenter
    fun providePresenter() = get<DeveloperSettingsPresenter>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_developer_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        developerSettingsTurnOffKioskModeSwitch.setOnClickListener {
            presenter.notifyKioskModeSwitchChecked(context!!, developerSettingsTurnOffKioskModeSwitch.isChecked)
        }
        developerSettingsTurnOffLeakCanarySwitch.setOnClickListener {
            presenter.notifyLeakCanarySwitchChecked(developerSettingsTurnOffLeakCanarySwitch.isChecked)
        }
    }

    override fun setGitSha(gitSha: String) {
        developerSettingsGitShaTextView.text = gitSha
    }

    override fun setBuildDate(date: String) {
        developerSettingsBuildDateTextView.text = date
    }

    override fun setBuildVersionCode(versionCode: String) {
        developerSettingsBuildVersionCodeTextView.text = versionCode
    }

    override fun setBuildVersionName(versionName: String) {
        developerSettingsBuildVersionNameTextView.text = versionName
    }

    override fun setKioskModeSwitchChecked(isChecked: Boolean) {
        developerSettingsTurnOffKioskModeSwitch.isChecked = isChecked
    }

    override fun setLeakCanarySwitchChecked(isChecked: Boolean) {
        developerSettingsTurnOffLeakCanarySwitch.isChecked = isChecked
    }
}