package com.provectus_it.bookme.ui.screen.add_event

import androidx.recyclerview.widget.DiffUtil
import com.provectus_it.bookme.entity.Room

class AvailableRoomDiffUtilCallback(
        private val oldList: List<Room>,
        private val newList: List<Room>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition] == newList[newItemPosition]
}