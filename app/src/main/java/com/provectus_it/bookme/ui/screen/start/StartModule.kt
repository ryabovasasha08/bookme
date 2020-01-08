package com.provectus_it.bookme.ui.screen.start

import com.provectus_it.bookme.ui.screen.start.event_state.EventStatePresenter
import com.provectus_it.bookme.ui.screen.start.quick_booking.QuickBookingPresenter
import org.koin.dsl.module

val startModule = module {
    single { StartPresenter(get(), get(), get()) }
    factory { EventStatePresenter() }
    single { QuickBookingPresenter(get()) }
}