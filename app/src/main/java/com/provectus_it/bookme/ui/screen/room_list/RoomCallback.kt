package com.provectus_it.bookme.ui.screen.room_list

interface RoomCallback {
    fun onRoomListItemClick(roomId: String, roomName: String, newSelectedPosition: Int)
}