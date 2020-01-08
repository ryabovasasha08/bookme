package com.provectus_it.bookme.ui.screen.add_event

import android.view.View.GONE
import android.view.View.VISIBLE
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.provectus_it.bookme.Constants
import com.provectus_it.bookme.R
import com.provectus_it.bookme.entity.request_body.EventCreateRequestBody
import com.provectus_it.bookme.entity.response_body.RoomResponseBody
import com.provectus_it.bookme.repository.EventRepository
import com.provectus_it.bookme.repository.RoomRepository
import com.provectus_it.bookme.ui.screen.room_info.CurrentDateModel
import com.provectus_it.bookme.ui.screen.room_list.SelectedRoomManager
import com.provectus_it.bookme.util.*
import com.provectus_it.bookme.util.DateTimeFormatters.Companion.DATE_FORMATTER
import com.provectus_it.bookme.util.DateTimeFormatters.Companion.TIME_FORMATTER
import com.provectus_it.bookme.util.logging.ApiException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber
import java.util.*

@InjectViewState
class AddEventPresenter(
        eventStartTime: LocalDateTime,
        eventEndTime: LocalDateTime,
        private val defaultRoomRemainingTime: Long,
        defaultRoomId: String,
        private val roomRepository: RoomRepository,
        private val eventRepository: EventRepository,
        private val selectedRoomManager: SelectedRoomManager,
        private val currentDateModel: CurrentDateModel
) : MvpPresenter<AddEventView>() {

    private val compositeDisposable = CompositeDisposable()

    private val availableRoomAdapter = AvailableRoomAdapter()

    private var selectedRoomId: String = defaultRoomId

    private var eventFromDateTime: LocalDateTime = eventStartTime
        set(value) {
            viewState.apply {
                setFromDateText(DATE_FORMATTER.format(value))
                setFromTimeText(TIME_FORMATTER.format(value))
            }
            field = value
        }

    private var eventToDateTime: LocalDateTime = eventEndTime
        set(value) {
            viewState.apply {
                setToDateText(DATE_FORMATTER.format(value))
                setToTimeText(TIME_FORMATTER.format(value))
            }
            field = value
        }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        availableRoomAdapter.availableRoomCallback = availableRoomCallback
        getAvailableRoomList(eventFromDateTime, eventToDateTime)

        viewState.apply {
            changePresetChipsTime(defaultRoomRemainingTime)
            setAdapter(availableRoomAdapter)
            setFromDateText(DATE_FORMATTER.format(eventFromDateTime))
            setFromTimeText(TIME_FORMATTER.format(eventFromDateTime))
            setToDateText(DATE_FORMATTER.format(eventToDateTime))
            setToTimeText(TIME_FORMATTER.format(eventToDateTime))
        }

        updateFromTimeEveryMinute()
        getRoomNameById(selectedRoomId)
        setChipEnabled(defaultRoomRemainingTime)
        checkIfDurationIsLessThanFiveMins()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    fun notifyFromViewClick() = showDateTimePickerDialog(eventFromDateTime, R.string.set_time_from, onFromDateTimeSelectedListener)

    fun notifyToViewClick() = showDateTimePickerDialog(eventToDateTime, R.string.set_time_to, onToDateTimeSelectedListener)

    private fun showDateTimePickerDialog(
            currentDateTime: LocalDateTime,
            dialogTitleId: Int,
            singleDateAndTimePickerDialogListener: SingleDateAndTimePickerDialog.Listener
    ) {
        viewState.showDateTimePickerDialog(
                convertLocalDateTimeToLong(currentDateTime),
                MIN_DATE,
                MAX_DATE,
                dialogTitleId,
                singleDateAndTimePickerDialogListener
        )
    }

    private fun updateFromTimeEveryMinute() {
        val disposable = scheduleEveryMinuteUpdate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { checkIfFromDateTimeIsCurrent() },
                        { Timber.e(it, "Failed to schedule every minute update for FROM time") }
                )

        compositeDisposable.add(disposable)
    }

    private fun checkIfFromDateTimeIsCurrent() {
        val currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)

        if (isCurrentFromDateTime(currentDateTime)) notifyFromDateTimeSelected(currentDateTime)
    }

    private fun convertLocalDateTimeToLong(localDateTime: LocalDateTime): Long {
        return DateTimeUtils.toDate(localDateTime.atZone(ZoneId.systemDefault()).toInstant()).time
    }

    private fun isCurrentFromDateTime(currentDateTime: LocalDateTime) =
            Duration.between(eventFromDateTime.truncatedTo(ChronoUnit.MINUTES), currentDateTime).toMinutes().toInt() == 1

    private fun notifyFromDateTimeSelected(dateTime: LocalDateTime) {
        eventFromDateTime = dateTime

        if (eventFromDateTime.isAfter(eventToDateTime) || equalsTruncatedToMinutes()) eventToDateTime = eventFromDateTime.plusHours(1)

        getAvailableRoomList(eventFromDateTime, eventToDateTime)
        changePresetChipsTime(eventToDateTime.toEpochMilliAtDefaultZone() - eventFromDateTime.toEpochMilliAtDefaultZone())
        checkIfDurationIsLessThanFiveMins()
    }

    private fun notifyToDateTimeSelected(date: Date) {
        eventToDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.time), ZoneId.systemDefault())

        if (eventToDateTime.isBefore(eventFromDateTime) || equalsTruncatedToMinutes()) eventFromDateTime = eventToDateTime.minusHours(1)

        getAvailableRoomList(eventFromDateTime, eventToDateTime)
        changePresetChipsTime(eventToDateTime.toEpochMilliAtDefaultZone() - eventFromDateTime.toEpochMilliAtDefaultZone())
        checkIfDurationIsLessThanFiveMins()
    }

    fun notifyBookEventClick() {
        val disposable = eventRepository.bookEvent(
                EventCreateRequestBody(
                        startTime = eventFromDateTime.toEpochMilliAtDefaultZone(),
                        roomId = selectedRoomId,
                        endTime = eventToDateTime.toEpochMilliAtDefaultZone()
                ),
                selectedRoomId
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            currentDateModel.currentDate = eventFromDateTime.toLocalDate()
                            selectedRoomManager.setSelectedRoomIndexByRoomId(selectedRoomId)
                            viewState.dismiss()
                        },
                        { showErrorDialog(it) }
                )

        compositeDisposable.add(disposable)
    }

    private fun equalsTruncatedToMinutes() = eventToDateTime.truncatedTo(ChronoUnit.MINUTES).isEqual(eventFromDateTime.truncatedTo(ChronoUnit.MINUTES))

    private fun getAvailableRoomList(startDateTime: LocalDateTime, endDateTime: LocalDateTime) {
        val disposable = roomRepository.getAvailableRoomList(
                startDateTime.toEpochMilliAtDefaultZone(),
                endDateTime.toEpochMilliAtDefaultZone())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            checkIfCurrentRoomIsFree(it)
                            setAvailableRoomAdapterData(it)
                        },
                        { Timber.e(it, "Failed to get available room list") }
                )

        compositeDisposable.add(disposable)
    }

    private fun checkIfCurrentRoomIsFree(roomList: List<RoomResponseBody>) {
        if (selectedRoomId != NOT_SELECTED_ROOM_ID && roomList.find { it.id == selectedRoomId } == null) {
            viewState.showRoomIsBookedDialog()
            viewState.setRoomName(NOT_SELECTED_ROOM_NAME)
            viewState.setDoneButtonEnabled(false)
            selectedRoomId = NOT_SELECTED_ROOM_ID
        }
    }

    private fun setAvailableRoomAdapterData(availableRoomList: List<RoomResponseBody>) {
        if (availableRoomList.isEmpty()) {
            viewState.setNoRoomsTextViewVisibility(VISIBLE)
            viewState.setAvailableRoomsRecyclerViewVisibility(GONE)
            viewState.setRoomName(NOT_SELECTED_ROOM_NAME)
            viewState.setDoneButtonEnabled(false)
            viewState.setChipDuration()
            viewState.setChipChecked()
            return
        } else {
            viewState.setNoRoomsTextViewVisibility(GONE)
            viewState.setAvailableRoomsRecyclerViewVisibility(VISIBLE)
        }

        availableRoomList.map { it.toRoom() }.toList().run {
            availableRoomAdapter.setData(this)
            if (selectedRoomId != NOT_SELECTED_ROOM_ID) {
                if (indexOfFirst { it.id == selectedRoomId } < 0) {
                    first().run {
                        selectedRoomId = id
                        viewState.setRoomName(name)
                    }
                }
            }

            availableRoomAdapter.selectRoomById(selectedRoomId)
            val selectedRoomIndex = availableRoomList.indexOfFirst { it.id == selectedRoomId }
            viewState.scrollToCurrentEventPosition(selectedRoomIndex)
        }
    }

    private fun showErrorDialog(e: Throwable) {
        if (e is ApiException) {
            viewState.showErrorDialog(e.message!!)
        } else {
            viewState.showUnexpectedErrorDialog(R.string.something_went_wrong)
            Timber.e(e, "Failed to book selected room")
        }
    }

    fun notifyDurationChipSelected(duration: Duration) {
        eventToDateTime = eventFromDateTime.plus(duration)
    }

    private fun changePresetChipsTime(remainingTime: Long) {
        val chipDurationsToBeSet = changeQuickBookingButtonsTime(remainingTime)
        val timeBetweenStartAndFinish = eventToDateTime.truncatedTo(ChronoUnit.MINUTES).toEpochMilliAtDefaultZone() - eventFromDateTime.truncatedTo(ChronoUnit.MINUTES).toEpochMilliAtDefaultZone()

        viewState.setChipDuration(chipDurationsToBeSet.component1(), chipDurationsToBeSet.component2(), chipDurationsToBeSet.component3())
        setChipChecked(chipDurationsToBeSet, timeBetweenStartAndFinish)
        setChipEnabled(remainingTime)
    }

    private fun setChipEnabled(remainingTime: Long) {
        val index = Constants.DURATIONS.indexOfFirst { it.toMillis() <= remainingTime }
        val lastIndex = Constants.DURATIONS.lastIndex

        if (!isIndexInDurationsRange(index)) {
            when (index) {
                lastIndex -> viewState.setChipEnabled(firstIsEnabled = true)
                lastIndex - 1 -> viewState.setChipEnabled(firstIsEnabled = true, secondIsEnabled = true)
                lastIndex - 2 -> viewState.setChipEnabled(firstIsEnabled = true, secondIsEnabled = true, thirdIsEnabled = true)
                else -> viewState.setChipEnabled()
            }
        } else {
            viewState.setChipEnabled(firstIsEnabled = true, secondIsEnabled = true, thirdIsEnabled = true)
        }
    }

    private fun setChipChecked(chipDurationsToBeSet: List<Duration>, remainingTime: Long) {
        when {
            remainingTime / MILLISECONDS_IN_MINUTE == chipDurationsToBeSet.component1().toMinutes() -> viewState.setChipChecked(firstIsChecked = true, secondIsChecked = false, thirdIsChecked = false)
            remainingTime / MILLISECONDS_IN_MINUTE == chipDurationsToBeSet.component2().toMinutes() -> viewState.setChipChecked(firstIsChecked = false, secondIsChecked = true, thirdIsChecked = false)
            remainingTime / MILLISECONDS_IN_MINUTE == chipDurationsToBeSet.component3().toMinutes() -> viewState.setChipChecked(firstIsChecked = false, secondIsChecked = false, thirdIsChecked = true)
            else -> viewState.setChipChecked()
        }
    }

    private fun getRoomNameById(roomId: String) {
        val disposable = roomRepository.getRoomNameById(roomId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { viewState.setRoomName(it) },
                        { Timber.e(it, "Failed to get room events list") }
                )

        compositeDisposable.add(disposable)
    }

    private fun checkIfDurationIsLessThanFiveMins() {
        if (isDurationLessThanFiveMins(eventFromDateTime, eventToDateTime)) {
            viewState.setDoneButtonEnabled(false)
        } else {
            viewState.setDoneButtonEnabled()
        }
    }

    private val onFromDateTimeSelectedListener = SingleDateAndTimePickerDialog.Listener {
        val fromDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.time), ZoneId.systemDefault())
        notifyFromDateTimeSelected(fromDateTime)
    }

    private val onToDateTimeSelectedListener = SingleDateAndTimePickerDialog.Listener { notifyToDateTimeSelected(it) }

    private val availableRoomCallback: AvailableRoomCallback = object : AvailableRoomCallback {
        override fun onAvailableRoomListItemClick(roomId: String, roomName: String, newSelectedPosition: Int) {
            selectedRoomId = roomId
            viewState.setRoomName(roomName)
            viewState.setDoneButtonEnabled()
            availableRoomAdapter.selectedPosition = newSelectedPosition
        }
    }

    companion object {
        private val MIN_DATE = Instant.EPOCH.toEpochMilli()
        private val MAX_DATE = Instant.ofEpochSecond(Integer.MAX_VALUE.toLong()).toEpochMilli()
        private const val NOT_SELECTED_ROOM_NAME = "---"
        private const val NOT_SELECTED_ROOM_ID = ""
    }

}