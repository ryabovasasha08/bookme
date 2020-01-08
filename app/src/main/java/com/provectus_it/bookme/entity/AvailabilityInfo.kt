package com.provectus_it.bookme.entity

import com.provectus_it.bookme.ui.screen.event_list.EventObject
import org.threeten.bp.LocalDateTime

data class AvailabilityInfo(
        val roomId: String,
        val isFree: Boolean,
        val timeUntil: LocalDateTime,
        val currentEvent: EventObject,
        val hoursLeft: Int,
        val minutesLeft: Int
)