package com.provectus_it.bookme.util

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoField
import timber.log.Timber
import java.util.concurrent.TimeUnit

const val SECONDS_IN_MINUTE = 60
const val MILLISECONDS_IN_MINUTE = 60000
const val MILLISECONDS_IN_SECOND = 1000

fun scheduleEveryMinuteUpdate(): Flowable<Unit> {
    val timerProcessor = BehaviorProcessor.createDefault(Unit)
    val delayTimeSeconds = (SECONDS_IN_MINUTE - LocalDateTime.now().second - 1).toLong()
    val delayTimeMillis = (MILLISECONDS_IN_SECOND - LocalDateTime.now().get(ChronoField.MILLI_OF_SECOND)).toLong()

    Completable.complete()
            .andThen(
                    Flowable.interval(
                            delayTimeSeconds * MILLISECONDS_IN_SECOND + delayTimeMillis,
                            MILLISECONDS_IN_MINUTE.toLong(),
                            TimeUnit.MILLISECONDS
                    )
            )
            .doOnNext { timerProcessor.onNext(Unit) }
            .subscribe()

    return timerProcessor
}
