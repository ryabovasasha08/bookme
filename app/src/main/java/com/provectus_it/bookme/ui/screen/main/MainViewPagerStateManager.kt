package com.provectus_it.bookme.ui.screen.main

import com.provectus_it.bookme.ui.screen.main.MainPagerAdapter.Companion.POSITION_MAIN_CONTAINER
import io.reactivex.subjects.PublishSubject

class MainViewPagerStateManager {

    private var swipeToPositionSubject = PublishSubject.create<Int>()

    var swipeToPosition: Int = POSITION_MAIN_CONTAINER
        set(value) {
            field = value
            swipeToPositionSubject.onNext(value)
        }

    fun subscribeForMainViewPagerState(): PublishSubject<Int> = swipeToPositionSubject

}