package com.provectus_it.bookme.ui.screen.event_list

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.provectus_it.bookme.R
import com.provectus_it.bookme.entity.Event
import com.provectus_it.bookme.entity.FreeEvent
import com.provectus_it.bookme.util.inflateChildView

class EventAdapter(private val shouldShowPointer: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val events = mutableListOf<EventObject>()

    var eventCallback: EventCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            EVENT_TYPE -> if (shouldShowPointer) {
                EventViewHolder(shouldShowPointer, parent.inflateChildView(R.layout.item_event_for_start_screen))
            } else {
                EventViewHolder(shouldShowPointer, parent.inflateChildView(R.layout.item_event))
            }

            FREE_EVENT_TYPE -> if (shouldShowPointer) {
                FreeEventViewHolder(shouldShowPointer, parent.inflateChildView(R.layout.item_free_event_on_start_screen))
            } else {
                FreeEventViewHolder(shouldShowPointer, parent.inflateChildView(R.layout.item_free_event))
            }

            else -> throw IllegalArgumentException("Unacceptable view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val event = events[position]

        when (holder.itemViewType) {
            EVENT_TYPE -> {
                (holder as EventViewHolder).bind(event as Event)
            }
            FREE_EVENT_TYPE -> {
                (holder as FreeEventViewHolder).bind(event as FreeEvent, eventCallback)
            }
        }
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (events[position]) {
            is Event -> EVENT_TYPE
            is FreeEvent -> FREE_EVENT_TYPE
            else -> throw IllegalArgumentException("Unacceptable view type")
        }
    }

    fun setData(newData: List<EventObject>) {
        val eventDiffUtilCallback = EventDiffUtilCallback(events, newData)
        val diffResult = DiffUtil.calculateDiff(eventDiffUtilCallback)
        events.clear()
        events.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }

    companion object {
        const val EVENT_TYPE = 0
        const val FREE_EVENT_TYPE = 1
    }

}