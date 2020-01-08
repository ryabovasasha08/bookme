package com.provectus_it.bookme.ui.screen.event_list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.provectus_it.bookme.R
import kotlinx.android.synthetic.main.item_event.view.*
import kotlinx.android.synthetic.main.item_event_for_start_screen.view.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

abstract class BaseEventViewHolder(
        private val shouldShowPointer: Boolean,
        itemView: View
) : RecyclerView.ViewHolder(itemView) {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    protected fun bind(eventObject: EventObject) {
        val contextResources = itemView.context.resources
        val bookStartTime = convertTime(eventObject.startTime)
        val bookEndTime = convertTime(eventObject.endTime)
        val bookIntervalString = contextResources.getString(R.string.book_interval, bookStartTime, bookEndTime)
        itemView.bookIntervalTextView.text = bookIntervalString
        itemView.isSelected = eventObject.isCurrent
        if (shouldShowPointer) showPointer(eventObject)
    }

    private fun convertTime(localDateTime: LocalDateTime) = dateTimeFormatter.format(localDateTime)

    private fun showPointer(eventObject: EventObject) {
        if (eventObject.isCurrent) {
            itemView.pointerImageView.visibility = View.VISIBLE
        } else {
            itemView.pointerImageView.visibility = View.INVISIBLE
        }
    }

}