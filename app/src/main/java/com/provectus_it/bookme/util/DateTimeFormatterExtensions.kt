package com.provectus_it.bookme.util

import org.threeten.bp.Duration
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

fun DateTimeFormatter.format(duration: Duration): String {
    val localTime = LocalTime.MIDNIGHT.plus(duration)

    return this.format(localTime)
}
