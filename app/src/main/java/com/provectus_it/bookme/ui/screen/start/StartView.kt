package com.provectus_it.bookme.ui.screen.start

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.provectus_it.bookme.entity.Room

@StateStrategyType(AddToEndSingleStrategy::class)
interface StartView : MvpView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun scrollToCurrentEvent()

    fun setRoomData(room: Room)
    fun setTimeAndDate(time: String, date: String)
    fun setScheduleHeader(roomName: String)
    fun showBackSwipeButton()
    fun hideBackSwipeButton()
    fun resetEventListState()
}