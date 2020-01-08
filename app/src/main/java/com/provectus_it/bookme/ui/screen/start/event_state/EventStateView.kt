package com.provectus_it.bookme.ui.screen.start.event_state

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.provectus_it.bookme.entity.Event
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime

@StateStrategyType(AddToEndSingleStrategy::class)
interface EventStateView : MvpView {
    fun updateBackSwipeButtonState()
    fun setStateText(stringResId: Int)
    fun setStateTimeText(stringResId: Int, timeLeft: String)
    fun setStateColor(colorResId: Int)
    fun showCheckinCheckoutMaterialButton()
    fun hideCheckinCheckoutMaterialButton()
    fun setCheckinCheckoutMaterialButtonText(stringResId: Int)
    fun setCountdownMax(max: Int)
    fun setCountdownProgress(progress: Int)
    fun setCountdownText(text: String)
    fun setButtonDuration(firstDuration: Duration, secondDuration: Duration, thirdDuration: Duration)
    fun setButtonEnabled(firstIsEnabled: Boolean = false, secondIsEnabled: Boolean = false, thirdIsEnabled: Boolean = false)
    fun setRoomName(name: String)
    fun notifyCheckoutBooking(checkoutDateTime: LocalDateTime, checkoutEvent: Event)
}