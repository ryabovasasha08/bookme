package com.provectus_it.bookme.ui.screen.start.quick_booking

import android.view.View
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

@StateStrategyType(OneExecutionStateStrategy::class)
interface QuickBookingView : MvpView {
    fun displayBookConfirmationDialog(v: View?, confirmationMessage: String)
}