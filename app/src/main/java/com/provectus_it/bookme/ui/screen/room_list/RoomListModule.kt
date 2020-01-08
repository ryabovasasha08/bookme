package com.provectus_it.bookme.ui.screen.room_list

import org.koin.dsl.module

val roomListModule = module {
    factory { RoomListPresenter(get(), get(), get()) }
    single(createdAtStart = true) { SelectedRoomManager(get()) }
}
