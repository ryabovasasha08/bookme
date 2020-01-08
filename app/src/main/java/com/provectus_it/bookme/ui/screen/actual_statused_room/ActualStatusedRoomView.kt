package com.provectus_it.bookme.ui.screen.actual_statused_room

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.provectus_it.bookme.entity.StatusedRoom

@StateStrategyType(AddToEndSingleStrategy::class)
interface ActualStatusedRoomView : MvpView {
    fun onActualStatusedRoomUpdate(statusedRoom: StatusedRoom)
}