package com.provectus_it.bookme.util

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.provectus_it.bookme.entity.Room

fun bindRoomNameAndAttributes(
        roomNameTextView: TextView,
        roomCapacityTextView: TextView,
        roomFloorTextView: TextView,
        tvTextView: TextView,
        hiddenStatusView: ImageView,
        room: Room
){
    roomNameTextView.text = room.name
    roomCapacityTextView.text = room.capacity
    roomFloorTextView.text = room.floor.toString()
    hiddenStatusView.visibility = if (room.isSecure) View.VISIBLE else View.GONE
    tvTextView.visibility = if (room.hasTv) View.VISIBLE else View.GONE
}