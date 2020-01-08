package com.provectus_it.bookme.ui.screen.room_list

import androidx.recyclerview.widget.DiffUtil
import com.provectus_it.bookme.entity.StatusedRoom

class StatusedRoomDiffUtilCallback(
        private val oldList: List<StatusedRoom>,
        private val newList: List<StatusedRoom>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition].room.id == newList[newItemPosition].room.id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition] == newList[newItemPosition]
}