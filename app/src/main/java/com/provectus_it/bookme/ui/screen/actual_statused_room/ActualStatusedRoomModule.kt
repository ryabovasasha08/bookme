package com.provectus_it.bookme.ui.screen.actual_statused_room

import org.koin.dsl.module

val actualStatusedRoomModule = module {
    factory { ActualStatusedRoomPresenter(get(), get()) }
}