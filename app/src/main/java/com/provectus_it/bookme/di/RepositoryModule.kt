package com.provectus_it.bookme.di

import com.provectus_it.bookme.repository.EventRepository
import com.provectus_it.bookme.repository.RoomRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { RoomRepository(get(), get(), get(), get()) }
    single { EventRepository(get(), get(), get(), get(), get(), get()) }
}
