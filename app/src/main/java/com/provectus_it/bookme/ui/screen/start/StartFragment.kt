package com.provectus_it.bookme.ui.screen.start

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.provectus_it.bookme.BuildConfig
import com.provectus_it.bookme.Constants.ROOM_ID
import com.provectus_it.bookme.R
import com.provectus_it.bookme.entity.Event
import com.provectus_it.bookme.entity.Room
import com.provectus_it.bookme.entity.StatusedRoom
import com.provectus_it.bookme.ui.custom_view.DurationMaterialButton
import com.provectus_it.bookme.ui.fragment.BaseFragment
import com.provectus_it.bookme.ui.screen.actual_statused_room.ActualStatusedRoomPresenter
import com.provectus_it.bookme.ui.screen.actual_statused_room.ActualStatusedRoomView
import com.provectus_it.bookme.ui.screen.event_list.EventListPresenter
import com.provectus_it.bookme.ui.screen.event_list.EventListView
import com.provectus_it.bookme.ui.screen.main.MainActivity
import com.provectus_it.bookme.ui.screen.start.event_state.EventStatePresenter
import com.provectus_it.bookme.ui.screen.start.event_state.EventStateView
import com.provectus_it.bookme.ui.screen.start.quick_booking.QuickBookingPresenter
import com.provectus_it.bookme.ui.screen.start.quick_booking.QuickBookingView
import com.provectus_it.bookme.util.amplitude.AddMeetingSource
import com.provectus_it.bookme.util.amplitude.ViewRoomContainerAction
import com.provectus_it.bookme.util.amplitude.logUserQuickBookTapEvent
import com.provectus_it.bookme.util.behavior.HideableMaterialScrollingButtonBehavior
import com.provectus_it.bookme.util.bindRoomNameAndAttributes
import kotlinx.android.synthetic.main.fragment_start.*
import kotlinx.android.synthetic.main.fragment_start.view.*
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class StartFragment : BaseFragment(), StartView, EventStateView, ActualStatusedRoomView, EventListView, QuickBookingView, View.OnClickListener {

    @InjectPresenter
    lateinit var startPresenter: StartPresenter

    @ProvidePresenter
    fun provideStartPresenter() = get<StartPresenter>()

    @InjectPresenter
    lateinit var eventListPresenter: EventListPresenter

    @ProvidePresenter
    fun provideEventListPresenter() =
            get<EventListPresenter> { parametersOf(true, ROOM_ID, LocalDate.now(), true) }

    @InjectPresenter
    lateinit var eventStatePresenter: EventStatePresenter

    @ProvidePresenter
    fun provideEventStatePresenter() = get<EventStatePresenter>()

    @InjectPresenter
    lateinit var quickBookingPresenter: QuickBookingPresenter

    @ProvidePresenter
    fun provideQuickBookingPresenter() = get<QuickBookingPresenter>()

    @InjectPresenter
    lateinit var actualStatusedRoomPresenter: ActualStatusedRoomPresenter

    @ProvidePresenter
    fun actualStatusedRoomPresenter() = get<ActualStatusedRoomPresenter>()

    override fun getLayoutResId(): Int = R.layout.fragment_start

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!BuildConfig.DEBUG) manageOpenDevSettingsButton(view)

        eventRecyclerView.layoutManager = LinearLayoutManager(context)

        startFragmentBackSwipeImageButton.setOnClickListener {
            (activity as MainActivity).setCurrentPage(0, true, ViewRoomContainerAction.CLICK)
        }

        bookCurrentRoomFAB.setOnClickListener {
            eventListPresenter.addMeeting()
            (activity as MainActivity).setCurrentPage(0, false, isUserAction = false)
        }

        eventRecyclerView.addOnScrollListener(onScrollListener)

        previousEventButton.setOnClickListener { eventListPresenter.notifyShowHideEventsButtonClick() }

        checkinCheckoutMaterialButton.setOnClickListener {
            eventStatePresenter.notifyCheckinCheckoutClick(checkinCheckoutMaterialButton.text, context!!)
        }

        roomNameTextView.setOnLongClickListener(roomNameTextViewListener)

        setListenerOnTimeDurationMaterialButtons()
    }

    override fun showCheckinCheckoutMaterialButton() {
        bookNowAndQuickBookingButtonsLayout.visibility = View.GONE
        checkinCheckoutLayout.visibility = View.VISIBLE
    }

    override fun hideCheckinCheckoutMaterialButton() {
        checkinCheckoutLayout.visibility = View.GONE
        bookNowAndQuickBookingButtonsLayout.visibility = View.VISIBLE
    }

    override fun setCheckinCheckoutMaterialButtonText(stringResId: Int) {
        checkinCheckoutMaterialButton.text = getString(stringResId)
    }

    private fun manageOpenDevSettingsButton(view: View) {
        view.openDevSettingsPanelButton.visibility = View.VISIBLE
        var tapCounter = ZERO_TIMES_TAPPED_DEVELOPER_SETTINGS_BUTTON
        view.openDevSettingsPanelButton.setOnClickListener {
            tapCounter += 1
            if (tapCounter == ENOUGH_TIMES_TAPPED_DEVELOPER_SETTINGS_BUTTON) {
                val drawerLayout = activity!!.findViewById(R.id.devSettingsDrawer) as DrawerLayout
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                (activity as MainActivity).openDevSettingsPanel()
                tapCounter = ZERO_TIMES_TAPPED_DEVELOPER_SETTINGS_BUTTON
            }
        }
    }

    private fun setListenerOnTimeDurationMaterialButtons() {
        firstTimeDurationMaterialButton.setOnClickListener(this)
        secondTimeDurationMaterialButton.setOnClickListener(this)
        thirdDurationMaterialButton.setOnClickListener(this)
    }

    override fun openAddMeetingDialog(
            sourceScreen: AddMeetingSource,
            eventStartTime: LocalDateTime,
            eventEndTime: LocalDateTime,
            defaultRoomRemainingTime: Long,
            defaultRoomId: String
    ) {
        (activity as MainActivity).openAddEventScreen(
                sourceScreen,
                eventStartTime,
                eventEndTime,
                defaultRoomRemainingTime,
                defaultRoomId
        )
    }

    override fun displayBookConfirmationDialog(v: View?, confirmationMessage: String) {
        MaterialAlertDialogBuilder(context)
                .setTitle(R.string.book_now)
                .setMessage(confirmationMessage)
                .setPositiveButton("OK") { _, _ -> quickBookingPresenter.bookNow((v as DurationMaterialButton).duration) }
                .setNegativeButton("CANCEL") { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
    }

    override fun onClick(v: View?) {
        logUserQuickBookTapEvent((v as DurationMaterialButton).duration.toMinutes())
        quickBookingPresenter.setBookConfirmationDialogMessage(v)
    }

    override fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        eventRecyclerView.adapter = adapter
    }

    override fun setRoomData(room: Room) {
        bindRoomNameAndAttributes(
                roomNameTextView,
                roomCapacityTextView,
                roomFloorTextView,
                tvTextView,
                hiddenStatusView,
                room
        )
    }

    override fun setScheduleHeader(roomName: String) {
        scheduleTextView.text = getString(R.string.schedule, roomName)
    }

    override fun scrollToCurrentEventPosition(position: Int) {
        (eventRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                position,
                0
        )
    }

    override fun setCurrentViewPagerPage(position: Int) {
        (activity as MainActivity).setCurrentPage(position, false, isUserAction = false)
    }

    override fun onActualStatusedRoomUpdate(statusedRoom: StatusedRoom) {
        eventStatePresenter.onActualStatusedRoomUpdate(statusedRoom)
    }

    override fun scrollToCurrentEvent() = eventListPresenter.scrollToCurrentEvent()

    override fun setTimeAndDate(time: String, date: String) {
        timeTextView.text = time
        dateTextView.text = date
    }

    override fun setStateText(stringResId: Int) {
        roomStateTextView.text = getString(stringResId)
    }

    override fun setStateTimeText(stringResId: Int, timeLeft: String) {
        stateTimeTextView.text = getString(stringResId, timeLeft)
    }

    override fun setStateColor(colorResId: Int) {
        stateContainer.setBackgroundColor(resources.getColor(colorResId))
    }

    override fun setCountdownMax(max: Int) {
        stateCountdown.max = max
    }

    override fun setCountdownProgress(progress: Int) {
        stateCountdown.progress = progress
    }

    override fun setCountdownText(text: String) {
        stateCountdown.text = text
    }

    override fun setRoomName(name: String) {
        quickBookingPresenter.getRoomName(name)
    }

    override fun setButtonDuration(
            firstDuration: Duration,
            secondDuration: Duration,
            thirdDuration: Duration
    ) {
        firstTimeDurationMaterialButton.duration = firstDuration
        secondTimeDurationMaterialButton.duration = secondDuration
        thirdDurationMaterialButton.duration = thirdDuration
    }

    override fun setButtonEnabled(
            firstIsEnabled: Boolean,
            secondIsEnabled: Boolean,
            thirdIsEnabled: Boolean
    ) {
        firstTimeDurationMaterialButton.isEnabled = firstIsEnabled
        secondTimeDurationMaterialButton.isEnabled = secondIsEnabled
        thirdDurationMaterialButton.isEnabled = thirdIsEnabled
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

    override fun setShowHideEventsButtonText(textId: Int) {
        previousEventButton.text = context?.getString(textId)
    }

    override fun setShowHideEventsButtonIcon(iconId: Int) {
        previousEventButton.icon = context?.getDrawable(iconId)
    }

    override fun showBackSwipeButton() {
        startFragmentBackSwipeImageButton.visibility = View.VISIBLE
        startFragmentBackSwipeButtonFrame.visibility = View.VISIBLE
        startFragmentLayout.setBackgroundColor(resources.getColor(R.color.white))
    }

    override fun hideBackSwipeButton() {
        startFragmentBackSwipeImageButton?.visibility = View.INVISIBLE
        startFragmentBackSwipeButtonFrame?.visibility = View.INVISIBLE
        if (stateContainer.background != null) startFragmentLayout.setBackgroundColor((stateContainer.background as ColorDrawable).color)
    }

    override fun resetEventListState() {
        eventListPresenter.resetEventListState()
    }

    override fun notifyCheckoutBooking(checkoutDateTime: LocalDateTime, checkoutEvent: Event) {
        eventListPresenter.notifyCheckoutBooking(checkoutDateTime, checkoutEvent)
        (activity as MainActivity).notifyCheckoutMeeting(checkoutEvent, checkoutDateTime)
    }

    override fun updateBackSwipeButtonState() {
        startPresenter.updateBackSwipeButtonState()
    }

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            eventListPresenter.notifyScrollStateChanged(newState)
        }
    }

    private val roomNameTextViewListener = View.OnLongClickListener { notifyRoomNameTextViewTouch() }

    private fun notifyRoomNameTextViewTouch(): Boolean {
        BookMeTeamImageView.visibility = View.VISIBLE
        Handler().postDelayed({ BookMeTeamImageView.visibility = View.GONE }, 5000)

        return true
    }

    override fun showFAB() = bookCurrentRoomFAB.show()

    override fun hideFAB() = bookCurrentRoomFAB.hide()

    companion object {
        private const val ZERO_TIMES_TAPPED_DEVELOPER_SETTINGS_BUTTON = 0
        private const val ENOUGH_TIMES_TAPPED_DEVELOPER_SETTINGS_BUTTON = 5
    }

}
