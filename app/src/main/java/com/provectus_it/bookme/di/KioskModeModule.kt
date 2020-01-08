package com.provectus_it.bookme.di

import com.provectus_it.bookme.util.kiosk_mode.KioskModeManager
import org.koin.dsl.module

val kioskModeModule = module {
    single { KioskModeManager(get()) }
}