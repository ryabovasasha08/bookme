package com.provectus_it.bookme.ui.screen.event_list

import org.threeten.bp.LocalDateTime

interface EventCallback {
    fun onEventListFreeItemClick(startDateTime: LocalDateTime, endDateTime: LocalDateTime)
}