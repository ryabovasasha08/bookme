package com.provectus_it.bookme.ui.screen.room_list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.provectus_it.bookme.R
import com.provectus_it.bookme.entity.StatusedRoom
import com.provectus_it.bookme.util.bindRoomNameAndAttributes
import kotlinx.android.synthetic.main.item_room.view.*
import kotlinx.android.synthetic.main.item_room.view.hiddenStatusView
import kotlinx.android.synthetic.main.item_room.view.roomCapacityTextView
import kotlinx.android.synthetic.main.item_room.view.roomFloorTextView
import kotlinx.android.synthetic.main.item_room.view.roomNameTextView
import kotlinx.android.synthetic.main.item_room.view.tvTextView
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class RoomViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    internal fun bind(statusedRoom: StatusedRoom, roomCallback: RoomCallback, isSelected: Boolean) {
        bindRoomNameAndAttributes(
                itemView.roomNameTextView,
                itemView.roomCapacityTextView,
                itemView.roomFloorTextView,
                itemView.tvTextView,
                itemView.hiddenStatusView,
                statusedRoom.room
        )

        if (statusedRoom.availabilityInfo.isFree) {
            itemView.roomStatusTextView.setText(R.string.free_until)
            itemView.roomItemCircleView.setImageResource(R.drawable.room_item_green_circle)
        } else {
            itemView.roomStatusTextView.setText(R.string.busy_until)
            itemView.roomItemCircleView.setImageResource(R.drawable.room_item_red_circle)
        }

        itemView.timeUntilTextView.text = convertLocalDateTime(statusedRoom.availabilityInfo.timeUntil)

        val contextResources = itemView.context.resources
        val timeLeftHours = statusedRoom.availabilityInfo.hoursLeft.toString()
        val timeLeftMinutes = statusedRoom.availabilityInfo.minutesLeft.toString()
        itemView.timeLeftTextView.text = contextResources.getString(R.string.time_left, timeLeftHours, timeLeftMinutes)

        itemView.isSelected = isSelected

        itemView.outsideContainer.setOnClickListener {
            roomCallback.onRoomListItemClick(statusedRoom.room.id, statusedRoom.room.name, layoutPosition)
        }
    }

    private fun convertLocalDateTime(localDateTime: LocalDateTime): String {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        return dateTimeFormatter.format(localDateTime)
    }

}