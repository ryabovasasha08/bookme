package com.provectus_it.bookme.ui.screen.main

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.provectus_it.bookme.entity.Event
import com.provectus_it.bookme.util.amplitude.ViewRoomContainerAction
import org.threeten.bp.LocalDateTime

@StateStrategyType(AddToEndSingleStrategy::class)
interface MainView : MvpView {
    fun setCurrentPage(position: Int, smoothScroll: Boolean, action: ViewRoomContainerAction = ViewRoomContainerAction.SWIPE, isUserAction: Boolean = true)
    fun openDevSettingsPanel()
    fun hideNavigationPanel()
    fun hideNavigationUI()
    fun showAddedEventCountdownSnackbar()
    fun showCheckoutCountdownSnackbar(checkoutEvent: Event, checkoutDateTime: LocalDateTime)
    fun dismissCountdownSnackbar()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun recreateActivity()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showCountdownDialog()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun blurActivity()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun deleteActivityBlur()
}