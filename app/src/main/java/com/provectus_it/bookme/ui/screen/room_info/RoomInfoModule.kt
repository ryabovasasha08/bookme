package com.provectus_it.bookme.ui.screen.room_info

import org.koin.dsl.module

val roomInfoModule = module {
    factory { RoomInfoPresenter(get(), get()) }
    single(createdAtStart = true) { CurrentDateModel(get(), get()) }
}