package com.provectus_it.bookme.ui.screen.add_event

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.provectus_it.bookme.Constants.DURATIONS
import org.threeten.bp.Duration

@StateStrategyType(AddToEndSingleStrategy::class)
interface AddEventView : MvpView {
    fun setFromDateText(text: String)
    fun setFromTimeText(text: String)
    fun setToDateText(text: String)
    fun setToTimeText(text: String)
    fun setRoomName(roomName: String)
    fun setAdapter(availableRoomAdapter: AvailableRoomAdapter)
    fun setNoRoomsTextViewVisibility(visibility: Int)
    fun setAvailableRoomsRecyclerViewVisibility(visibility: Int)
    fun scrollToCurrentEventPosition(position: Int)
    fun setDoneButtonEnabled(isEnabled: Boolean = true)

    fun setChipDuration(
            firstDuration: Duration = DURATIONS[DURATIONS.lastIndex - 1],
            secondDuration: Duration = DURATIONS[DURATIONS.lastIndex - 2],
            thirdDuration: Duration = DURATIONS[DURATIONS.lastIndex - 3]
    )

    fun setChipChecked(
            firstIsChecked: Boolean = false,
            secondIsChecked: Boolean = false,
            thirdIsChecked: Boolean = false
    )

    fun setChipEnabled(
            firstIsEnabled: Boolean = false,
            secondIsEnabled: Boolean = false,
            thirdIsEnabled: Boolean = false
    )

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showDateTimePickerDialog(
            currentDateTime: Long,
            minDate: Long,
            maxDate: Long,
            dialogTitleId: Int,
            singleDateAndTimePickerDialogListener: SingleDateAndTimePickerDialog.Listener
    )

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showErrorDialog(message: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showRoomIsBookedDialog()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showUnexpectedErrorDialog(messageId: Int)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun dismiss()
}