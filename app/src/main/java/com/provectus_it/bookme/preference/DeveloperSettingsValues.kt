package com.provectus_it.bookme.preference

import com.skydoves.preferenceroom.KeyName
import com.skydoves.preferenceroom.PreferenceEntity

@PreferenceEntity("DeveloperSettingsValues")
open class DeveloperSettingsValues {
    @KeyName("isLeakCanaryEnabled")
    @JvmField
    val isLeakCanaryEnabled: Boolean = false
}