package com.provectus_it.bookme.ui.screen.main

import org.koin.dsl.module

val mainModule = module {
    factory { MainPresenter(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single(createdAtStart = true) { MainViewPagerStateManager() }
}