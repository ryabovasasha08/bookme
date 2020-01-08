package com.provectus_it.bookme.ui.screen.event_list

import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.provectus_it.bookme.R
import com.provectus_it.bookme.ui.fragment.BaseFragment
import com.provectus_it.bookme.ui.screen.main.MainActivity
import com.provectus_it.bookme.ui.screen.room_info.RoomInfoFragment
import com.provectus_it.bookme.util.amplitude.AddMeetingSource
import com.provectus_it.bookme.util.behavior.HideableMaterialScrollingButtonBehavior
import kotlinx.android.synthetic.main.fragment_event_list.*
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class EventListFragment : BaseFragment(), EventListView {

    @InjectPresenter
    lateinit var eventListPresenter: EventListPresenter

    @ProvidePresenter
    fun provideEventsListPresenter() =
            get<EventListPresenter> { parametersOf(false, currentRoomId, currentEventDate, false) }

    private val currentRoomId: String
        get() = arguments!!.getString(RoomInfoFragment.ARG_ROOM_ID)!!

    private val currentEventDate: LocalDate
        get() = arguments!!.getSerializable(ARG_EVENT_DATE) as LocalDate

    var scale: Float? = null

    override fun getLayoutResId(): Int = R.layout.fragment_event_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scale = resources.displayMetrics.density
        eventRecyclerView.layoutManager = LinearLayoutManager(context)
        eventRecyclerView.addOnScrollListener(onScrollListener)
        previousEventButton.setOnClickListener { eventListPresenter.notifyShowHideEventsButtonClick() }
    }

    override fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        eventRecyclerView.adapter = adapter
    }

    override fun scrollToCurrentEventPosition(position: Int) {
        (eventRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
    }

    override fun openAddMeetingDialog(
            sourceScreen: AddMeetingSource,
            eventStartTime: LocalDateTime,
            eventEndTime: LocalDateTime,
            defaultRoomRemainingTime: Long,
            defaultRoomId: String
    ) {
        (activity as MainActivity).openAddEventScreen(sourceScreen, eventStartTime, eventEndTime, defaultRoomRemainingTime, defaultRoomId)
    }

    override fun setShowHideEventsButtonIcon(iconId: Int) {
        previousEventButton.icon = context?.getDrawable(iconId)
    }

    override fun setShowHideEventsButtonText(textId: Int) {
        previousEventButton.text = context?.getString(textId)
    }

    override fun setShowHideEventsButtonVisibility(visibility: Int) {
        previousEventButton.visibility = visibility
    }

    override fun setShowHideEventsButtonEnabled(isEnabled: Boolean) {
        previousEventButton.isEnabled = isEnabled
    }

    override fun setEventRecyclerViewPaddingTop(paddingTop: Int) {
        eventRecyclerView.updatePadding(top = paddingTop)
    }

    override fun showPreviousEventButton() {
        ((eventRecyclerView.layoutParams as CoordinatorLayout.LayoutParams).behavior as HideableMaterialScrollingButtonBehavior).reset(previousEventButton)
    }

    override fun showFAB() = (parentFragment as RoomInfoFragment).showFAB()

    override fun hideFAB() = (parentFragment as RoomInfoFragment).hideFAB()

    fun notifyBeingShown() = eventListPresenter.resetEventListState()

    fun addMeeting() = eventListPresenter.addMeeting()

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            eventListPresenter.notifyScrollStateChanged(newState)
        }
    }

    companion object {
        fun newInstance(roomId: String, bookingsDate: LocalDate): EventListFragment {
            val args = Bundle()
            args.putString(ARG_ROOM_ID, roomId)
            args.putSerializable(ARG_EVENT_DATE, bookingsDate)

            val fragment = EventListFragment()
            fragment.arguments = args

            return fragment
        }

        private const val ARG_EVENT_DATE = "ARG_EVENT_DATE"
        private const val ARG_ROOM_ID = "ARG_ROOM_ID"
    }

}
