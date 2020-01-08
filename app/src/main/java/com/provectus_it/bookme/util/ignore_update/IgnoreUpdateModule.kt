package com.provectus_it.bookme.util.ignore_update

import org.koin.dsl.module

val ignoreUpdateModule = module {
    single(createdAtStart = true) { IgnoreUpdateStatusManager() }
}