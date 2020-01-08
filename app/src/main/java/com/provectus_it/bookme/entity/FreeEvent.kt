package com.provectus_it.bookme.entity

import com.provectus_it.bookme.ui.screen.event_list.EventObject
import org.threeten.bp.LocalDateTime

data class FreeEvent(
        override val startTime: LocalDateTime,
        override val endTime: LocalDateTime,
        override var isCurrent: Boolean
) : EventObject {

    override fun copyAsEventObject(isCurrent: Boolean) = copy(isCurrent = isCurrent)

}