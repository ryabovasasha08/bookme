package com.provectus_it.bookme.ui.screen.event_list

import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.provectus_it.bookme.util.amplitude.AddMeetingSource
import org.threeten.bp.LocalDateTime

@StateStrategyType(AddToEndSingleStrategy::class)
interface EventListView : MvpView {

    fun setAdapter(adapter: RecyclerView.Adapter<*>)
    fun setShowHideEventsButtonText(textId: Int)
    fun setShowHideEventsButtonIcon(iconId: Int)
    fun setShowHideEventsButtonVisibility(visibility: Int)
    fun setShowHideEventsButtonEnabled(isEnabled: Boolean)
    fun setEventRecyclerViewPaddingTop(paddingTop: Int)
    fun showPreviousEventButton()
    fun showFAB()
    fun hideFAB()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun scrollToCurrentEventPosition(position: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun openAddMeetingDialog(
            sourceScreen: AddMeetingSource,
            eventStartTime: LocalDateTime,
            eventEndTime: LocalDateTime,
            defaultRoomRemainingTime: Long,
            defaultRoomId: String
    )

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun setCurrentViewPagerPage(position: Int) {
        // no-op
    }
}