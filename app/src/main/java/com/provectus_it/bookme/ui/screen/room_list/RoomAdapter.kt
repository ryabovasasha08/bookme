package com.provectus_it.bookme.ui.screen.room_list

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.provectus_it.bookme.R
import com.provectus_it.bookme.entity.StatusedRoom
import com.provectus_it.bookme.util.inflateChildView

class RoomAdapter : RecyclerView.Adapter<RoomViewHolder>() {

    private val statusedRoomList = mutableListOf<StatusedRoom>()

    lateinit var roomCallback: RoomCallback

    var selectedPosition: Int = RecyclerView.NO_POSITION
        set(value) {
            notifyItemSelected(value)
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        return RoomViewHolder(parent.inflateChildView(R.layout.item_room))
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(statusedRoomList[position], roomCallback, selectedPosition == position)
    }

    override fun getItemCount(): Int {
        return statusedRoomList.size
    }

    fun setData(statusedRoomList: List<StatusedRoom>) {
        val statusedRoomDiffResult = DiffUtil.calculateDiff(StatusedRoomDiffUtilCallback(this.statusedRoomList, statusedRoomList))
        this.statusedRoomList.clear()
        this.statusedRoomList.addAll(statusedRoomList)
        statusedRoomDiffResult.dispatchUpdatesTo(this)
    }

    private fun notifyItemSelected(newSelectedPosition: Int) {
        val oldPosition = selectedPosition
        notifyItemChanged(oldPosition)
        notifyItemChanged(newSelectedPosition)
    }

}
