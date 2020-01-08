package com.provectus_it.bookme.util

import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*

class DateTimeFormatters {
    companion object {
        val HOURS_MINUTES_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("H'h' m'm'")
        val MINUTES_SECONDS_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("m'm' s's'")
        val SECONDS_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("s")
        val HOURS_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("H'h'")
        val MINUTES_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("mm'm'")
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("E, MMM d")
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val SIMPLE_DATE_FORMATTER: SimpleDateFormat = SimpleDateFormat("E, MMM d", Locale.ROOT)
    }
}