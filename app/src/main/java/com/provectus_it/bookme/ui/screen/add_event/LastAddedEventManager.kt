package com.provectus_it.bookme.ui.screen.add_event

import io.reactivex.subjects.PublishSubject
import org.threeten.bp.LocalDateTime

class LastAddedEventManager {

    private var lastAddedEventSubject = PublishSubject.create<Unit>()

    var lastAddedEventId: Int = 0
        set(value) {
            field = value
            lastAddedEventSubject.onNext(Unit)
        }

    var lastAddedEventEndTime: LocalDateTime = LocalDateTime.now()

    fun subscribeForAddedEvents(): PublishSubject<Unit> = lastAddedEventSubject
}