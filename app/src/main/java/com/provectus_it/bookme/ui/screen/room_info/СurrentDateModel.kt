package com.provectus_it.bookme.ui.screen.room_info

import android.annotation.SuppressLint
import com.provectus_it.bookme.repository.EventRepository
import com.provectus_it.bookme.util.update.MidnightUpdateManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.threeten.bp.LocalDate
import timber.log.Timber

class CurrentDateModel(
        private val eventRepository: EventRepository,
        private val midnightUpdateManager: MidnightUpdateManager
) {

    private var currentDateSubject = BehaviorSubject.createDefault(LocalDate.now())

    private var startDate: LocalDate = LocalDate.now()

    var currentDate: LocalDate = startDate
        set(value) {
            field = value
            eventRepository.updateAllEventsIfRequired(value)
            currentDateSubject.onNext(value)
        }

    init {
        subscribeForRefreshEvents()
    }

    @SuppressLint("CheckResult")
    private fun subscribeForRefreshEvents() {
        midnightUpdateManager.subscribeForMidnightUpdate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            startDate = LocalDate.now()
                            currentDate = LocalDate.now()
                        },
                        { Timber.e(it, "Failed to subscribe on refreshing events at midnight") }
                )
    }

    fun subscribeForCurrentDay(): BehaviorSubject<LocalDate> = currentDateSubject

    fun reset() {
        currentDate = startDate
    }

}