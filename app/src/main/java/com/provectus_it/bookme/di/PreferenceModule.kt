package com.provectus_it.bookme.di

import android.content.Context
import com.provectus_it.bookme.preference.Preference_DeveloperSettingsValues
import com.provectus_it.bookme.preference.Preference_KioskModeValues
import org.koin.dsl.module

val preferenceModule = module {
    single { provideKioskModePreference(get()) }
    single { provideDeveloperSettingsValues(get()) }
}

fun provideKioskModePreference(context: Context): Preference_KioskModeValues = Preference_KioskModeValues.getInstance(context)

fun provideDeveloperSettingsValues(context: Context): Preference_DeveloperSettingsValues = Preference_DeveloperSettingsValues.getInstance(context)