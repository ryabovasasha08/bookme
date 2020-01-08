package com.provectus_it.bookme.dev_settings_panel

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

var developerSettingsModule: Module = module {
    single { DeveloperSettingsModel(androidContext()) }
    factory { DeveloperSettingsPresenter(get(), get(), get()) }
}