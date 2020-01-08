package com.provectus_it.bookme.util

import com.provectus_it.bookme.Constants
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime


fun changeQuickBookingButtonsTime(remainingTime: Long): MutableList<Duration> {
    val index = Constants.DURATIONS.indexOfFirst { it.toMillis() <= remainingTime }
    val lastIndex = Constants.DURATIONS.lastIndex

    return if (isIndexInDurationsRange(index)) {
        when {
            isIndexBetweenHalfAnHourAndHour(index) -> mutableListOf(Constants.DURATIONS[index + 4], Constants.DURATIONS[index + 1], Constants.DURATIONS[index])
            isIndexMoreThanHourAndAHalf(index) -> mutableListOf(Constants.DURATIONS[index + 3], Constants.DURATIONS[index + 1], Constants.DURATIONS[index])
            else -> mutableListOf(Constants.DURATIONS[index + 2], Constants.DURATIONS[index + 1], Constants.DURATIONS[index])
        }
    } else {
        mutableListOf(Constants.DURATIONS[lastIndex], Constants.DURATIONS[lastIndex - 1], Constants.DURATIONS[lastIndex - 2])
    }
}

private fun isIndexBetweenHalfAnHourAndHour(index: Int): Boolean = index == 2

private fun isIndexMoreThanHourAndAHalf(index: Int): Boolean = index == 0

fun isIndexInDurationsRange(index: Int): Boolean = index < Constants.DURATIONS.lastIndex - LAST_AVAILABLE_DURATION_INDEX_DIFFERENCE && index >= 0

fun isDurationLessThanFiveMins(eventFromDateTime: LocalDateTime, eventToDateTime: LocalDateTime) = Duration.between(eventFromDateTime, eventToDateTime).toMinutes() < MIN_DURATION_OF_FREE_ITEM

private const val MIN_DURATION_OF_FREE_ITEM = 5
const val LAST_AVAILABLE_DURATION_INDEX_DIFFERENCE = 2