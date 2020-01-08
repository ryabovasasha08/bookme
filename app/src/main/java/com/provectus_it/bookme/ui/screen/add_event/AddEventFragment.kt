package com.provectus_it.bookme.ui.screen.add_event

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.provectus_it.bookme.R
import com.provectus_it.bookme.ui.custom_view.DurationChip
import com.provectus_it.bookme.ui.fragment.BaseFragment
import com.provectus_it.bookme.util.DateTimeFormatters.Companion.SIMPLE_DATE_FORMATTER
import kotlinx.android.synthetic.main.fragment_add_event.*
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import java.util.*

class AddEventFragment : BaseFragment(), AddEventView {

    @InjectPresenter
    lateinit var addEventPresenter: AddEventPresenter

    @ProvidePresenter
    fun provideAddEventPresenter() = get<AddEventPresenter> { parametersOf(eventStartTime, eventEndTime, defaultRoomRemainingTime, defaultRoomId) }

    private val eventStartTime: LocalDateTime
        get() = arguments!!.getSerializable(ARG_EVENT_START_TIME) as LocalDateTime

    private val eventEndTime: LocalDateTime
        get() = arguments!!.getSerializable(ARG_EVENT_END_TIME) as LocalDateTime

    private val defaultRoomRemainingTime: Long
        get() = arguments!!.getLong(ARG_DEFAULT_ROOM_REMAINING_TIME)

    private val defaultRoomId: String
        get() = arguments!!.getString(ARG_DEFAULT_ROOM_ID)!!

    override fun getLayoutResId(): Int = R.layout.fragment_add_event

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fromDateMaterialTextView.setOnClickListener(onFromViewsClickListener)
        fromTimeMaterialTextView.setOnClickListener(onFromViewsClickListener)
        toDateMaterialTextView.setOnClickListener(onToViewsClickListener)
        toTimeMaterialTextView.setOnClickListener(onToViewsClickListener)

        dismissFragmentImageButton.setOnClickListener { dismiss() }
        bookRoomImageButton.setOnClickListener { addEventPresenter.notifyBookEventClick() }
        outsideRelativeLayout.setOnClickListener { dismiss() }

        durationSelectorChipGroup.setOnCheckedChangeListener(onCheckedChangeListener)

        availableRoomsRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun showDateTimePickerDialog(
            currentDateTime: Long,
            minDate: Long,
            maxDate: Long,
            dialogTitleId: Int,
            singleDateAndTimePickerDialogListener: SingleDateAndTimePickerDialog.Listener
    ) {
        SingleDateAndTimePickerDialog.Builder(context)
                .mainColor(resources.getColor(R.color.mariner))
                .minDateRange(Date(minDate))
                .maxDateRange(Date(maxDate))
                .defaultDate(Date(currentDateTime))
                .minutesStep(1)
                .setDayFormatter(SIMPLE_DATE_FORMATTER)
                .title(getString(dialogTitleId))
                .listener(singleDateAndTimePickerDialogListener)
                .displayListener { picker ->
                    picker.setOnTouchListener { _, _ ->
                        activity?.onUserInteraction()
                        true
                    }
                }
                .display()
    }

    override fun setFromDateText(text: String) {
        fromDateMaterialTextView.text = text
    }

    override fun setFromTimeText(text: String) {
        fromTimeMaterialTextView.text = text
    }

    override fun setToDateText(text: String) {
        toDateMaterialTextView.text = text
    }

    override fun setToTimeText(text: String) {
        toTimeMaterialTextView.text = text
    }

    override fun setRoomName(roomName: String) {
        selectedRoomMaterialTextView.text = roomName
    }

    override fun dismiss() {
        fragmentManager!!.popBackStack()
    }

    override fun showErrorDialog(message: String) {
        showDialog(message)
    }

    override fun showRoomIsBookedDialog() {
        showDialog(context!!.getString(R.string.this_room_is_booked))
    }

    override fun showUnexpectedErrorDialog(messageId: Int) {
        showDialog(getString(messageId))
    }

    override fun setAdapter(availableRoomAdapter: AvailableRoomAdapter) {
        availableRoomsRecyclerView.adapter = availableRoomAdapter
    }

    override fun setNoRoomsTextViewVisibility(visibility: Int) {
        noRoomsTextView.visibility = visibility
    }

    override fun setAvailableRoomsRecyclerViewVisibility(visibility: Int) {
        availableRoomsRecyclerView.visibility = visibility
    }

    override fun setDoneButtonEnabled(isEnabled: Boolean) {
        bookRoomImageButton.isEnabled = isEnabled
    }

    override fun setChipDuration(firstDuration: Duration, secondDuration: Duration, thirdDuration: Duration) {
        firstTimeDurationChip.duration = firstDuration
        secondTimeDurationChip.duration = secondDuration
        thirdTimeDurationChip.duration = thirdDuration
    }

    override fun setChipChecked(firstIsChecked: Boolean, secondIsChecked: Boolean, thirdIsChecked: Boolean) {
        firstTimeDurationChip.isChecked = firstIsChecked
        secondTimeDurationChip.isChecked = secondIsChecked
        thirdTimeDurationChip.isChecked = thirdIsChecked
    }

    override fun setChipEnabled(firstIsEnabled: Boolean, secondIsEnabled: Boolean, thirdIsEnabled: Boolean) {
        firstTimeDurationChip.isEnabled = firstIsEnabled
        secondTimeDurationChip.isEnabled = secondIsEnabled
        thirdTimeDurationChip.isEnabled = thirdIsEnabled
    }

    override fun scrollToCurrentEventPosition(position: Int) {
        (availableRoomsRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
    }

    private fun showDialog(message: String) {
        MaterialAlertDialogBuilder(context, R.style.CustomMaterialDialog)
                .setMessage(message)
                .setTitle(R.string.oops_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    private val onFromViewsClickListener = View.OnClickListener { addEventPresenter.notifyFromViewClick() }

    private val onToViewsClickListener = View.OnClickListener { addEventPresenter.notifyToViewClick() }

    private val onCheckedChangeListener = ChipGroup.OnCheckedChangeListener { _, checkedId ->
        view!!.findViewById<DurationChip>(checkedId)?.apply {
            addEventPresenter.notifyDurationChipSelected(duration)
        }
    }

    companion object {
        fun newInstance(eventStartTime: LocalDateTime, eventEndTime: LocalDateTime, remainingTime: Long, defaultRoomId: String): AddEventFragment {
            val args = Bundle()
            args.putSerializable(ARG_EVENT_START_TIME, eventStartTime)
            args.putSerializable(ARG_EVENT_END_TIME, eventEndTime)
            args.putSerializable(ARG_DEFAULT_ROOM_REMAINING_TIME, remainingTime)
            args.putString(ARG_DEFAULT_ROOM_ID, defaultRoomId)

            val fragment = AddEventFragment()
            fragment.arguments = args

            return fragment
        }

        const val ARG_EVENT_START_TIME = "ARG_EVENT_START_TIME"
        const val ARG_EVENT_END_TIME = "ARG_EVENT_END_TIME"
        const val ARG_DEFAULT_ROOM_REMAINING_TIME = "ARG_DEFAULT_ROOM_REMAINING_TIME"
        const val ARG_DEFAULT_ROOM_ID = "ARG_DEFAULT_ROOM_ID"
    }

}
