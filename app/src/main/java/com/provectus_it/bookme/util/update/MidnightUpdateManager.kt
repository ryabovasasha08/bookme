package com.provectus_it.bookme.util.update

import io.reactivex.subjects.PublishSubject

class MidnightUpdateManager {

    private val midnightUpdateSubject = PublishSubject.create<Unit>()

    fun doOnMidnightUpdate() = midnightUpdateSubject.onNext(Unit)

    fun subscribeForMidnightUpdate() = midnightUpdateSubject

}