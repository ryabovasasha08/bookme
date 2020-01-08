package com.provectus_it.bookme.ui.screen.event_list

import org.threeten.bp.LocalDateTime

interface EventObject {
    val startTime: LocalDateTime
    val endTime: LocalDateTime
    var isCurrent: Boolean

    fun copyAsEventObject(isCurrent: Boolean): EventObject
}