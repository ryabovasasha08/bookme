package com.provectus_it.bookme.ui.screen.room_list

import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface RoomListView : MvpView {
    fun setAdapter(roomListAdapter: RecyclerView.Adapter<*>)
    fun chooseRoom(roomId: String, roomName: String)
    fun scrollToPosition(position: Int)
}