package com.provectus_it.bookme.ui.screen.room_info

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.provectus_it.bookme.R
import com.provectus_it.bookme.entity.StatusedRoom
import com.provectus_it.bookme.ui.fragment.BaseFragment
import com.provectus_it.bookme.ui.screen.actual_statused_room.ActualStatusedRoomPresenter
import com.provectus_it.bookme.ui.screen.actual_statused_room.ActualStatusedRoomView
import com.provectus_it.bookme.ui.screen.event_list.EventListFragment
import com.provectus_it.bookme.ui.screen.main.MainActivity
import com.provectus_it.bookme.util.amplitude.ViewRoomContainerAction
import kotlinx.android.synthetic.main.fragment_room.*
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDate

class RoomInfoFragment : BaseFragment(), RoomInfoView, ActualStatusedRoomView {

    @InjectPresenter
    lateinit var roomInfoPresenter: RoomInfoPresenter

    @ProvidePresenter
    fun provideRoomPresenter() = get<RoomInfoPresenter> { parametersOf(currentRoomId) }

    @InjectPresenter
    lateinit var actualStatusedRoomPresenter: ActualStatusedRoomPresenter

    @ProvidePresenter
    fun actualStatusedRoomPresenter() = get<ActualStatusedRoomPresenter>()

    private val currentRoomId: String
        get() = arguments!!.getString(ARG_ROOM_ID)!!

    private lateinit var eventListPagerAdapter: EventListPagerAdapter

    private var currentViewPagerPosition = EventListPagerAdapter.ITEM_COUNT / 2

    override fun getLayoutResId(): Int = R.layout.fragment_room

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentRoomName = arguments!!.getString(ARG_ROOM_NAME)
        roomNameTextView.text = currentRoomName

        currentDateTextView.setOnClickListener {
            roomInfoPresenter.handleCurrentDateTextClick()
        }

        nextDayImageButton.setOnClickListener {
            setSelectedView(currentViewPagerPosition + 1)
        }

        previousDayImageButton.setOnClickListener {
            setSelectedView(currentViewPagerPosition - 1)
        }

        roomFragmentBackSwipeImageButton.setOnClickListener {
            (activity as MainActivity).setCurrentPage(1, true, ViewRoomContainerAction.CLICK)
        }

        eventListViewPager.registerOnPageChangeCallback(onPageChangeCallback)
        eventListViewPager.offscreenPageLimit = 1

        bookCurrentRoomFAB.setOnClickListener {
            (childFragmentManager.findFragmentByTag("f" + eventListPagerAdapter
                    .getItemId(eventListViewPager.currentItem)) as EventListFragment).addMeeting()
        }
    }

    override fun setDate(date: String) {
        currentDateTextView.text = date
    }

    override fun showDatePickerDialog(
            year: Int,
            month: Int,
            day: Int,
            minDate: Long,
            maxDate: Long
    ) {
        val onDateSetListener =
                DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, dayOfMonth ->
                    roomInfoPresenter.handleDataChange(selectedYear, selectedMonth, dayOfMonth)
                }

        val datePickerDialog = DatePickerDialog(
                context!!,
                onDateSetListener,
                year,
                month,
                day
        )

        datePickerDialog.datePicker.minDate = minDate
        datePickerDialog.datePicker.maxDate = maxDate

        datePickerDialog.show()
    }

    override fun setupAdapter(localDate: LocalDate) {
        eventListPagerAdapter = EventListPagerAdapter(this, currentRoomId, localDate)

        setAdapter(eventListPagerAdapter)
        setSelectedView(EventListPagerAdapter.START_POSITION, false)
    }

    override fun showBackSwipeButton() {
        roomInfoPresenter.setStateColor()
        roomFragmentBackSwipeImageButton.visibility = View.VISIBLE
        roomFragmentBackSwipeButtonFrame.visibility = View.VISIBLE
    }

    override fun hideBackSwipeButton() {
        roomFragmentBackSwipeImageButton.visibility = View.INVISIBLE
        roomFragmentBackSwipeButtonFrame.visibility = View.INVISIBLE
        roomFragmentLayout.setBackgroundColor(resources.getColor(R.color.white))
    }

    private fun setSelectedView(position: Int, smoothScroll: Boolean = true) {
        eventListViewPager.setCurrentItem(position, smoothScroll)
    }

    private fun setAdapter(eventListPagerAdapter: EventListPagerAdapter) {
        eventListViewPager.adapter = eventListPagerAdapter
    }

    override fun setFreeStateColor() {
        roomFragmentLayout.setBackgroundColor(resources.getColor(R.color.green_haze))
        roomFragmentBackSwipeButtonFrame.setImageResource(R.drawable.room_info_fragment_swipe_button_frame_green)
    }

    override fun setBusyStateColor() {
        roomFragmentLayout.setBackgroundColor(resources.getColor(R.color.my_sin))
        roomFragmentBackSwipeButtonFrame.setImageResource(R.drawable.room_info_fragment_swipe_button_frame_yellow)
    }

    override fun onActualStatusedRoomUpdate(statusedRoom: StatusedRoom) {
        roomInfoPresenter.onActualStatusedRoomUpdate(statusedRoom)
    }

    fun notifyPageSelected(position: Int) {
        roomInfoPresenter.notifyPageSelected(eventListPagerAdapter.calculateTime(position))
        currentViewPagerPosition = position
    }

    private val onPageChangeCallback: ViewPager2.OnPageChangeCallback =
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    // no-op
                }

                override fun onPageSelected(position: Int) = notifyPageSelected(position)

                override fun onPageScrollStateChanged(state: Int) {
                    // no-op
                }
            }

    fun notifyBeingShown() = childFragmentManager.fragments.forEach { (it as EventListFragment).notifyBeingShown() }

    fun hideFAB() = bookCurrentRoomFAB.hide()

    fun showFAB() = bookCurrentRoomFAB.show()

    companion object {
        fun newInstance(roomId: String, roomName: String): RoomInfoFragment {
            val args = Bundle()
            args.putString(ARG_ROOM_ID, roomId)
            args.putString(ARG_ROOM_NAME, roomName)

            val fragment = RoomInfoFragment()
            fragment.arguments = args

            return fragment
        }

        const val ARG_ROOM_ID = "ARG_ROOM_ID"
        const val ARG_ROOM_NAME = "ARG_ROOM_NAME"
    }

}