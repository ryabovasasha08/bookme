package com.provectus_it.bookme.util

import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

fun LocalDateTime.endOfDay(): LocalDateTime {
    return this.toLocalDate().atStartOfDay().plusDays(1)
}

fun LocalDateTime.toEpochMilliAtDefaultZone(): Long {
    return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}