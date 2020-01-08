package com.provectus_it.bookme.ui.screen.event_list

import android.view.View
import com.provectus_it.bookme.R
import com.provectus_it.bookme.entity.Event
import kotlinx.android.synthetic.main.item_event.view.*

class EventViewHolder(shouldShowPointer: Boolean, itemView: View) : BaseEventViewHolder(shouldShowPointer, itemView) {

    internal fun bind(booking: Event) {
        super.bind(booking)

        val contextResources = itemView.context.resources
        val bookedByString = if (booking.role == ROLE_TABLET || booking.role == ROLE_TABLET_RO) {
            booking.displayName
        } else {
            contextResources.getString(R.string.booked_by, booking.userFirstName, booking.userLastName)
        }
        itemView.bookedByTextView.text = bookedByString
    }

    companion object {
        const val ROLE_TABLET = "tablet"
        const val ROLE_TABLET_RO = "tablet_ro"
    }

}