package com.provectus_it.bookme.ui.screen.event_list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.provectus_it.bookme.R
import com.provectus_it.bookme.entity.Event
import com.provectus_it.bookme.entity.FreeEvent
import com.provectus_it.bookme.repository.EventRepository
import com.provectus_it.bookme.repository.RoomRepository
import com.provectus_it.bookme.ui.screen.main.MainPagerAdapter.Companion.POSITION_MAIN_CONTAINER
import com.provectus_it.bookme.util.amplitude.AddMeetingSource.*
import com.provectus_it.bookme.util.endOfDay
import com.provectus_it.bookme.util.isDurationLessThanFiveMins
import com.provectus_it.bookme.util.toEpochMilliAtDefaultZone
import com.provectus_it.bookme.util.update.MidnightUpdateManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber

@InjectViewState
class EventListPresenter(
        private val midnightUpdateManager: MidnightUpdateManager,
        private val eventRepository: EventRepository,
        private val roomRepository: RoomRepository,
        private var eventAdapter: EventAdapter,
        private val shouldShowPointer: Boolean,
        private val currentRoomId: String,
        private val currentDate: LocalDate,
        private val isSingle: Boolean
) : MvpPresenter<EventListView>() {

    private val compositeDisposable = CompositeDisposable()

    private var currentEventPosition: Int? = null

    private var subscribeForDataDisposable: Disposable? = null

    private var currentEventList: List<EventObject>? = null

    private var displayedEventList: List<EventObject>? = null

    private var previousEventsAreHidden = true

    var canPreviousEventButtonBeShown: Boolean = false
        set(value) {
            field = value
            setupShowHideElements()
        }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        eventAdapter = EventAdapter(shouldShowPointer)
        eventAdapter.eventCallback = eventCallback
        viewState.setAdapter(eventAdapter)
        subscribeForData(currentDate)
        subscribeForRefreshEvents()
    }

    private fun subscribeForRefreshEvents() {
        if (!isSingle) return

        val disposable = midnightUpdateManager.subscribeForMidnightUpdate()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { subscribeForData(LocalDate.now()) },
                        { Timber.e(it, "Failed to subscribe on refreshing events at midnight") }
                )

        compositeDisposable.add(disposable)
    }

    private fun subscribeForData(currentDateTime: LocalDate) {
        subscribeForDataDisposable?.dispose()

        subscribeForDataDisposable = eventRepository.getEventList(currentRoomId, currentDateTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { setEventList(it) },
                        { Timber.e(it, "Failed to get room events list") }
                )

        compositeDisposable.add(subscribeForDataDisposable!!)
    }

    private fun setEventList(events: List<EventObject>) {
        currentEventList = events

        previousEventsAreHidden = true

        if (isToday(currentDate)) {
            displayedEventList = currentEventList!!.subList(currentEventList!!.indexOfFirst { it.isCurrent }, currentEventList!!.size)
            setupShowHideElements()
        } else {
            displayedEventList = currentEventList!!
            disableShowHideButton()
        }

        eventAdapter.setData(displayedEventList!!)

        if (isToday(currentDate)) {
            val currentEventIndex = getCurrentEventPosition()

            if (currentEventPosition == null) {
                viewState.scrollToCurrentEventPosition(currentEventIndex!!)
            }

            currentEventPosition = currentEventIndex
        }
    }

    private fun getTimeStartToday(eventList: List<EventObject>): LocalDateTime {
        val currentEvent = eventList.find { it.isCurrent }!!
        val currentDateTime = LocalDateTime.now()
        return if ((currentEvent is FreeEvent) && !isDurationLessThanFiveMins(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), currentEvent.endTime)) {
            LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        } else {
            getClosestNextFreeTimeOrRoundHour(eventList, currentDateTime)
        }
    }

    private fun getClosestNextFreeTimeOrRoundHour(eventList: List<EventObject>, currentDateTime: LocalDateTime): LocalDateTime {
        val nextFreeEvent = roomRepository.getFirstNextFreeEvent(eventList, currentDateTime)
        return when {
            (nextFreeEvent == null) -> LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.HOURS)
            !isDurationLessThanFiveMins(nextFreeEvent.startTime, nextFreeEvent.endTime) -> nextFreeEvent.startTime
            else -> getClosestNextFreeTimeOrRoundHour(eventList, nextFreeEvent.endTime)
        }
    }

    private fun getTimeForCurrentDate(defaultRoomId: String) {
        val disposable = eventRepository.getEventListOnce(currentRoomId, LocalDate.now())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { calculateTimeAndOpenAddMeeting(it, defaultRoomId) },
                        { Timber.e(it, "Failed to get room events list") }
                )

        compositeDisposable.add(disposable)
    }

    private fun getTimeStartAfterToday(eventList: List<EventObject>): LocalDateTime {
        return when (val firstFreeTime = eventList.find { it is FreeEvent }?.startTime) {
            null -> LocalDateTime.of(currentDate, LocalTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(1))
            else -> firstFreeTime
        }
    }

    private fun getTimeStart(): LocalDateTime {
        return when {
            isToday(currentDate) -> getTimeStartToday(currentEventList!!)
            else -> getTimeStartAfterToday(currentEventList!!)
        }
    }

    private fun getTimeEnd(startTime: LocalDateTime): LocalDateTime {
        return when {
            isLastFreeEvent(LocalDateTime.now(), currentEventList!!) -> getLastTimeEnd(startTime, currentEventList!!)
            else -> getEndTime(startTime, currentEventList!!)
        }
    }

    fun scrollToCurrentEvent() {
        if (currentEventPosition != null) viewState.scrollToCurrentEventPosition(currentEventPosition!!)
    }

    fun resetEventListState() {
        if (currentEventList != null && currentEventList!!.isNotEmpty() && isToday(currentDate)) {
            previousEventsAreHidden = true
            canPreviousEventButtonBeShown = false
            viewState.showPreviousEventButton()
            hidePreviousEvents()
            setupShowHideElements()
            eventAdapter.setData(displayedEventList!!)
            scrollToCurrentEvent()
        }
    }

    private fun isToday(currentDate: LocalDate): Boolean {
        return currentDate == LocalDateTime.now().toLocalDate()
    }

    private fun isBeforeToday(currentDate: LocalDate): Boolean {
        return currentDate.isBefore(LocalDate.now())
    }

    private fun isLastFreeEvent(startTime: LocalDateTime, eventList: List<EventObject>): Boolean {
        val lastElement = eventList.last()

        return lastElement is FreeEvent && isBetweenEventTime(lastElement, startTime)
    }

    private fun getLastTimeEnd(startTime: LocalDateTime, eventList: List<EventObject>): LocalDateTime {
        val lastElementEndTime = eventList.last().endTime

        return when {
            startTime.plusHours(1).isBefore(lastElementEndTime) -> startTime.plusHours(1)
            else -> lastElementEndTime
        }
    }

    private fun getEndTime(startTime: LocalDateTime, eventList: List<EventObject>): LocalDateTime {
        val currentFreeEvent = eventList.lastOrNull { (it is FreeEvent) && isBetweenEventTime(it, startTime) }

        return when {
            currentFreeEvent == null -> startTime.plusHours(1)
            startTime.plusHours(1).isAfter(currentFreeEvent.endTime) -> currentFreeEvent.endTime
            else -> startTime.plusHours(1)
        }
    }

    private fun calculateTimeAndOpenAddMeeting(eventList: List<EventObject>, defaultRoomId: String) {
        val eventStartTime = getTimeStartToday(eventList).truncatedTo(ChronoUnit.MINUTES)
        val eventEndTime = getEndTime(eventStartTime, eventList).truncatedTo(ChronoUnit.MINUTES)
        val remainingTime = getRemainingTime(eventStartTime, eventEndTime, eventList)

        viewState.openAddMeetingDialog(
                if (shouldShowPointer) DEFAULT else ROOM_INFO,
                eventStartTime,
                eventEndTime,
                remainingTime,
                defaultRoomId
        )
    }

    private fun getRemainingTime(eventStartTime: LocalDateTime, eventEndTime: LocalDateTime, eventList: List<EventObject>): Long {
        val timeBetweenStartAndFinish = getRemainingTimeWithPredefinedPeriod(eventStartTime, eventEndTime)

        return when (val currentEvent = eventList.find { it.isCurrent }) {
            null -> timeBetweenStartAndFinish
            else -> {
                val remainingTime = if (currentEvent is FreeEvent) {
                    currentEvent.endTime.truncatedTo(ChronoUnit.MINUTES).toEpochMilliAtDefaultZone() - LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toEpochMilliAtDefaultZone()
                } else {
                    when (val nextFreeEvent = roomRepository.getFirstNextFreeEvent(eventList, LocalDateTime.now())) {
                        null -> LocalDateTime.now().endOfDay().toEpochMilliAtDefaultZone()
                        else -> nextFreeEvent.endTime.truncatedTo(ChronoUnit.MINUTES).toEpochMilliAtDefaultZone() - nextFreeEvent.startTime.truncatedTo(ChronoUnit.MINUTES).toEpochMilliAtDefaultZone()
                    }
                }
                if (remainingTime > timeBetweenStartAndFinish) remainingTime else timeBetweenStartAndFinish
            }
        }
    }

    private fun getRemainingTimeWithPredefinedPeriod(eventStartTime: LocalDateTime, eventEndTime: LocalDateTime): Long {
        return eventEndTime.toEpochMilliAtDefaultZone() - eventStartTime.toEpochMilliAtDefaultZone()
    }

    private fun getTimeAndOpenAddMeeting(defaultRoomId: String) {
        if (isBeforeToday(currentDate)) {
            getTimeForCurrentDate(defaultRoomId)
        } else {
            val eventStartTime = getTimeStart().truncatedTo(ChronoUnit.MINUTES)
            val eventEndTime = getTimeEnd(eventStartTime).truncatedTo(ChronoUnit.MINUTES)
            val remainingTime = getRemainingTime(eventStartTime, eventEndTime, currentEventList!!)

            viewState.openAddMeetingDialog(
                    if (shouldShowPointer) DEFAULT else ROOM_INFO,
                    eventStartTime,
                    eventEndTime,
                    remainingTime,
                    defaultRoomId
            )
        }
    }

    private fun isBetweenEventTime(eventObject: EventObject, dateTime: LocalDateTime): Boolean {
        return eventObject.run {
            (startTime.isEqual(dateTime) || startTime.isBefore(dateTime)) && (endTime.isEqual(dateTime) || endTime.isAfter(startTime))
        }
    }

    private fun getMinutesBetweenDateTime(startDateTime: LocalDateTime, endDateTime: LocalDateTime): Long {
        return ChronoUnit.MINUTES.between(startDateTime, endDateTime)
    }

    private fun getEventStartDateTime(startDateTime: LocalDateTime, endDateTime: LocalDateTime): LocalDateTime {
        return if (isCurrentFreeEvent(startDateTime, endDateTime)) {
            getTimeStartToday(currentEventList!!)
        } else {
            startDateTime
        }
    }

    private fun getEventEndDateTime(startToBeShownDateTime: LocalDateTime, startEventDateTime: LocalDateTime, endEventDateTime: LocalDateTime): LocalDateTime {
        return if (isCurrentFreeEvent(startEventDateTime, endEventDateTime)) {
            if (isDurationLessThanFiveMins(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES), endEventDateTime)) {
                getEndTime(startToBeShownDateTime, currentEventList!!)
            } else {
                getEndTime(startToBeShownDateTime, currentEventList!!)
            }
        } else {
            getEndTimeForFutureFreeEvent(startToBeShownDateTime, endEventDateTime)
        }
    }

    private fun isCurrentFreeEvent(startDateTime: LocalDateTime, endDateTime: LocalDateTime): Boolean {
        return LocalDateTime.now().run { isAfter(startDateTime) && isBefore(endDateTime) }
    }

    private fun getEndTimeForFutureFreeEvent(startDateTime: LocalDateTime, endDateTime: LocalDateTime): LocalDateTime {
        return if (getMinutesBetweenDateTime(startDateTime, endDateTime) > MINUTES_IN_HOUR) {
            startDateTime.plusHours(1)
        } else {
            endDateTime
        }
    }

    private fun addMeetingWithPredefinedPeriod(startEventDateTime: LocalDateTime, endEventDateTime: LocalDateTime) {
        val startToBeShownDateTime = getEventStartDateTime(startEventDateTime, endEventDateTime)
        val endToBeShownDateTime = getEventEndDateTime(startToBeShownDateTime, startEventDateTime, endEventDateTime)
        val remainingTime = getRemainingTimeWithPredefinedPeriod(startToBeShownDateTime, endToBeShownDateTime)

        viewState.openAddMeetingDialog(
                if (shouldShowPointer) FREE_ITEM_DEFAULT else FREE_ITEM,
                startToBeShownDateTime,
                endToBeShownDateTime,
                remainingTime,
                currentRoomId
        )
    }

    fun addMeeting() = getTimeAndOpenAddMeeting(currentRoomId)

    fun notifyShowHideEventsButtonClick() {
        if (previousEventsAreHidden) {
            showPreviousEvents()
        } else {
            hidePreviousEvents()
        }

        eventAdapter.setData(displayedEventList!!)
        previousEventsAreHidden = !previousEventsAreHidden
    }

    fun notifyCheckoutBooking(checkoutDateTime: LocalDateTime, checkoutEvent: Event) {
        eventRepository.localCheckoutBooking(checkoutDateTime, checkoutEvent)
    }

    private fun hidePreviousEvents() {
        displayedEventList = currentEventList!!.subList(currentEventList!!.indexOfFirst { it.isCurrent }, currentEventList!!.size)
        currentEventPosition = getCurrentEventPosition()
        viewState.apply {
            setShowHideEventsButtonIcon(R.drawable.ic_arrow_upward_24px)
            setShowHideEventsButtonText(R.string.show_previous_events)
        }
    }

    private fun showPreviousEvents() {
        displayedEventList = currentEventList!!
        currentEventPosition = getCurrentEventPosition()
        viewState.apply {
            setShowHideEventsButtonIcon(R.drawable.ic_arrow_downward_24px)
            setShowHideEventsButtonText(R.string.hide_previous_events)
            scrollToCurrentEvent()
        }
    }

    private fun getCurrentEventPosition() = displayedEventList?.indexOf(displayedEventList?.first { it.isCurrent })

    private fun setupShowHideElements() {
        if (previousEventButtonCanBeShown()) {
            disableShowHideButton()
            viewState.setEventRecyclerViewPaddingTop(0)
        } else {
            enableShowHideButton()
            viewState.setEventRecyclerViewPaddingTop(if (shouldShowPointer) START_EVENT_LIST_PADDING_TOP else EVENT_LIST_PADDING_TOP)
        }
    }

    private fun previousEventButtonCanBeShown(): Boolean {
        return currentEventList != null
                && currentEventList?.size == 1
                || currentEventList?.first()!!.isCurrent
                || !canPreviousEventButtonBeShown
                || (currentEventList!![SECOND_EVENT_INDEX].isCurrent && currentEventList?.first() is FreeEvent)
    }

    private fun disableShowHideButton() {
        viewState.apply {
            setShowHideEventsButtonEnabled(false)
            setShowHideEventsButtonVisibility(View.GONE)
        }
    }

    private fun enableShowHideButton() {
        viewState.apply {
            setShowHideEventsButtonEnabled(true)
            setShowHideEventsButtonVisibility(View.VISIBLE)
        }
    }

    fun notifyScrollStateChanged(newScrollState: Int) {
        viewState.apply {
            if (newScrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
                if (isToday(currentDate) && !canPreviousEventButtonBeShown) canPreviousEventButtonBeShown = true

                hideFAB()
            } else if (newScrollState == RecyclerView.SCROLL_STATE_IDLE) {
                showFAB()
            }
        }
    }

    private val eventCallback: EventCallback = object : EventCallback {
        override fun onEventListFreeItemClick(startDateTime: LocalDateTime, endDateTime: LocalDateTime) {
            if (shouldShowPointer) viewState.setCurrentViewPagerPage(POSITION_MAIN_CONTAINER)

            addMeetingWithPredefinedPeriod(startDateTime, endDateTime)
        }
    }

    companion object {
        const val MINUTES_IN_HOUR = 60
        const val START_EVENT_LIST_PADDING_TOP = 108
        const val EVENT_LIST_PADDING_TOP = 88
        const val SECOND_EVENT_INDEX = 1
    }

}