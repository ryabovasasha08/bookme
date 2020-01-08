package com.provectus_it.bookme.ui.screen.add_event

import org.koin.dsl.module
import org.threeten.bp.LocalDateTime

val addEventModule = module {
    factory { (
                      eventStartTime: LocalDateTime,
                      eventEndTime: LocalDateTime,
                      defaultRoomRemainingTime: Long,
                      defaultRoomId: String
              ) ->
        AddEventPresenter(
                eventStartTime,
                eventEndTime,
                defaultRoomRemainingTime,
                defaultRoomId,
                get(),
                get(),
                get(),
                get()
        )
    }

    single(createdAtStart = true) { LastAddedEventManager() }
}