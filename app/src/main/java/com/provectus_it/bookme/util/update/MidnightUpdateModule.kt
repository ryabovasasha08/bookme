package com.provectus_it.bookme.util.update

import org.koin.dsl.module

val midnightUpdateModule = module {
    single { MidnightUpdateManager() }
}
