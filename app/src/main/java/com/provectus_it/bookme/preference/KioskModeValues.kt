package com.provectus_it.bookme.preference

import com.skydoves.preferenceroom.KeyName
import com.skydoves.preferenceroom.PreferenceEntity

@PreferenceEntity("KioskModeValues")
open class KioskModeValues {
    @KeyName("isKioskModeEnabled")
    @JvmField
    val isKioskModeEnabled: Boolean = true

    @KeyName("isFirstApplicationLaunch")
    @JvmField
    val isFirstApplicationLaunch: Boolean = true
}