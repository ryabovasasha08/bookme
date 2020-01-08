package com.provectus_it.bookme.ui.screen.add_event

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.provectus_it.bookme.R
import com.provectus_it.bookme.entity.Room
import com.provectus_it.bookme.util.inflateChildView

class AvailableRoomAdapter : RecyclerView.Adapter<AvailableRoomViewHolder>() {

    private val availableRoomList = mutableListOf<Room>()

    lateinit var availableRoomCallback: AvailableRoomCallback

    var selectedPosition: Int = RecyclerView.NO_POSITION
        set(value) {
            notifyItemSelected(value)
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailableRoomViewHolder {
        return AvailableRoomViewHolder(parent.inflateChildView(R.layout.item_available_free_room))
    }

    override fun onBindViewHolder(holder: AvailableRoomViewHolder, position: Int) {
        holder.bind(availableRoomList[position], availableRoomCallback, selectedPosition == position)
    }

    override fun getItemCount(): Int {
        return availableRoomList.size
    }

    fun setData(availableRoomList: List<Room>) {
        val availableRoomDiffResult = DiffUtil.calculateDiff(AvailableRoomDiffUtilCallback(this.availableRoomList, availableRoomList))
        this.availableRoomList.clear()
        this.availableRoomList.addAll(availableRoomList)
        availableRoomDiffResult.dispatchUpdatesTo(this)
    }

    private fun notifyItemSelected(newSelectedPosition: Int) {
        val oldPosition = selectedPosition
        notifyItemChanged(oldPosition)
        notifyItemChanged(newSelectedPosition)
    }

    fun selectRoomById(roomId: String) {
        selectedPosition = availableRoomList.indexOfFirst { it.id == roomId }
    }

}
