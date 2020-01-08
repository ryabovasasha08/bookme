package com.provectus_it.bookme.ui.screen.add_event

interface AvailableRoomCallback {
    fun onAvailableRoomListItemClick(roomId: String, roomName: String, newSelectedPosition: Int)
}