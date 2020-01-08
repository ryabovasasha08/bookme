package com.provectus_it.bookme.util

import com.provectus_it.bookme.entity.FreeEvent
import com.provectus_it.bookme.ui.screen.event_list.EventObject
import org.threeten.bp.LocalDate

fun addFreeEvents(eventList: List<EventObject>, currentDate: LocalDate): List<EventObject> {
    val startDateTime = currentDate.atStartOfDay()
    val endDateTime = currentDate.atStartOfDay().endOfDay()

    val newList = mutableListOf<EventObject>()

    if (eventList.isEmpty()) {
        newList.add(FreeEvent(startDateTime, endDateTime, false))
        return newList
    }

    if (eventList.first().startTime != startDateTime) {
        newList.add(FreeEvent(startDateTime, eventList.first().startTime, false))
    }

    newList.add(eventList.first())

    for (i in 1 until eventList.size) {
        if (eventList[i].startTime != eventList[i - 1].endTime) {
            newList.add(FreeEvent(eventList[i - 1].endTime, eventList[i].startTime, false))
        }

        newList.add(eventList[i])
    }

    if (eventList.last().endTime != endDateTime) {
        newList.add(FreeEvent(eventList.last().endTime, endDateTime, false))
    }

    return newList
}