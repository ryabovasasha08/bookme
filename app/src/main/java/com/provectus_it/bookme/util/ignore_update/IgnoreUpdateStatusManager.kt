package com.provectus_it.bookme.util.ignore_update

import io.reactivex.subjects.BehaviorSubject

class IgnoreUpdateStatusManager {

    var shouldIgnoreUpdateSubject = BehaviorSubject.createDefault(false)

    fun subscribeForIgnoreUpdateStatus(): BehaviorSubject<Boolean> = shouldIgnoreUpdateSubject

}