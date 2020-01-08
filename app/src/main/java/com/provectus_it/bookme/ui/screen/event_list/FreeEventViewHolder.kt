package com.provectus_it.bookme.ui.screen.event_list

import android.view.View
import com.provectus_it.bookme.entity.FreeEvent
import com.provectus_it.bookme.util.isDurationLessThanFiveMins
import kotlinx.android.synthetic.main.item_free_event.view.*
import org.threeten.bp.LocalDateTime

class FreeEventViewHolder(shouldShowPointer: Boolean, itemView: View) : BaseEventViewHolder(shouldShowPointer, itemView) {

    internal fun bind(booking: FreeEvent, eventCallback: EventCallback?) {
        super.bind(booking)

        LocalDateTime.now().let {
            itemView.eventCardView.isEnabled = booking.endTime.run { isAfter(it) || isEqual(it) } &&
                    !isDurationLessThanFiveMins(booking.startTime, booking.endTime)
        }
        itemView.eventCardView.setOnClickListener {
            eventCallback?.onEventListFreeItemClick(booking.startTime, booking.endTime)
        }
    }

}