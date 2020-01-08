package com.provectus_it.bookme.ui.screen.event_list

import androidx.recyclerview.widget.DiffUtil
import com.provectus_it.bookme.entity.Event
import com.provectus_it.bookme.entity.FreeEvent

class EventDiffUtilCallback(private val oldList: List<EventObject>, private val newList: List<EventObject>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldEvent = oldList[oldItemPosition]
        val newEvent = newList[newItemPosition]

        if (oldEvent is Event && newEvent is Event)
            return oldEvent.id == newEvent.id

        if (oldEvent is FreeEvent && newEvent is FreeEvent)
            return oldEvent.startTime == newEvent.startTime

        return false
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldEvent = oldList[oldItemPosition]
        val newEvent = newList[newItemPosition]
        return oldEvent == newEvent
    }

}