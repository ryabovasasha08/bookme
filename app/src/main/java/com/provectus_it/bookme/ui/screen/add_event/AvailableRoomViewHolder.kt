package com.provectus_it.bookme.ui.screen.add_event

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.provectus_it.bookme.entity.Room
import com.provectus_it.bookme.util.bindRoomNameAndAttributes
import kotlinx.android.synthetic.main.item_available_free_room.view.*

class AvailableRoomViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    internal fun bind(room: Room, availableRoomCallback: AvailableRoomCallback, isSelected: Boolean) {
        bindRoomNameAndAttributes(
                itemView.roomNameTextView,
                itemView.roomCapacityTextView,
                itemView.roomFloorTextView,
                itemView.tvTextView,
                itemView.hiddenStatusView,
                room
        )

        itemView.isSelected = isSelected

        itemView.outsideContainer.setOnClickListener {
            availableRoomCallback.onAvailableRoomListItemClick(room.id, room.name, layoutPosition)
        }
    }

}