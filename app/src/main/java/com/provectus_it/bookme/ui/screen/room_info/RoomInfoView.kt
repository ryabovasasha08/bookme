package com.provectus_it.bookme.ui.screen.room_info

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import org.threeten.bp.LocalDate

@StateStrategyType(AddToEndSingleStrategy::class)
interface RoomInfoView : MvpView {
    fun setDate(date: String)
    fun showBackSwipeButton()
    fun hideBackSwipeButton()
    fun setFreeStateColor()
    fun setBusyStateColor()
    fun setupAdapter(localDate: LocalDate)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showDatePickerDialog(
            year: Int,
            month: Int,
            day: Int,
            minDate: Long,
            maxDate: Long
    )
}