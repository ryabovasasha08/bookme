package com.provectus_it.bookme.ui.screen.event_list

import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.threeten.bp.LocalDate

val eventListModule = module {
    factory { (
                      shouldShowPointer: Boolean,
                      currentRoomId: String,
                      currentDate: LocalDate,
                      isSingle: Boolean
              ) ->
        EventListPresenter(
                get(),
                get(),
                get(),
                get { parametersOf(shouldShowPointer) },
                shouldShowPointer,
                currentRoomId,
                currentDate,
                isSingle)
    }
    factory { (shouldShowPointer: Boolean) -> EventAdapter(shouldShowPointer) }
}