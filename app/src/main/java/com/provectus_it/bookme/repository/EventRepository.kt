package com.provectus_it.bookme.repository

import android.annotation.SuppressLint
import com.provectus_it.bookme.Constants.ROOM_ID
import com.provectus_it.bookme.database.EventDao
import com.provectus_it.bookme.entity.Event
import com.provectus_it.bookme.entity.request_body.EventCreateRequestBody
import com.provectus_it.bookme.entity.request_body.EventUpdateOrDeleteRequestBody
import com.provectus_it.bookme.entity.request_body.RoomRequestBody
import com.provectus_it.bookme.entity.response_body.EventResponseBody
import com.provectus_it.bookme.network.service.EventService
import com.provectus_it.bookme.network.service.RoomService
import com.provectus_it.bookme.network.websocket.WebSocketService
import com.provectus_it.bookme.ui.screen.add_event.LastAddedEventManager
import com.provectus_it.bookme.ui.screen.event_list.EventObject
import com.provectus_it.bookme.util.addFreeEvents
import com.provectus_it.bookme.util.amplitude.logUserAddMeetingEvent
import com.provectus_it.bookme.util.amplitude.logUserCheckoutEvent
import com.provectus_it.bookme.util.amplitude.logUserUndoCheckoutEvent
import com.provectus_it.bookme.util.ignore_update.IgnoreUpdateStatusManager
import com.provectus_it.bookme.util.scheduleEveryMinuteUpdate
import com.provectus_it.bookme.util.toEpochMilliAtDefaultZone
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber

class EventRepository(
        private val roomService: RoomService,
        private val eventService: EventService,
        private val eventDao: EventDao,
        private val socketService: WebSocketService,
        private val lastAddedEventManager: LastAddedEventManager,
        private val ignoreUpdateStatusManager: IgnoreUpdateStatusManager
) {

    init {
        updateEventsOnCalendarChanges()
    }

    fun getEventList(roomId: String, currentDate: LocalDate): Flowable<List<EventObject>> {
        val startDateTime = currentDate.atStartOfDay()
        val endDateTime = currentDate.atStartOfDay().plusDays(1)

        return Flowable.combineLatest(
                eventDao.subscribe(roomId, startDateTime, endDateTime),
                scheduleEveryMinuteUpdate(),
                BiFunction<List<EventObject>, Unit, List<EventObject>> { s, _ -> s }
        )
                .map { addFreeEvents(it, currentDate) }
                .map { selectCurrentEvent(it) }
                .distinctUntilChanged()
    }

    fun getEventListOnce(roomId: String, currentDate: LocalDate): Single<List<EventObject>> {
        val startDateTime = currentDate.atStartOfDay()
        val endDateTime = currentDate.atStartOfDay().plusDays(1)

        return eventDao.get(roomId, startDateTime, endDateTime)
                .map { addFreeEvents(it, currentDate) }
                .map { selectCurrentEvent(it) }
    }

    @SuppressLint("CheckResult")
    fun bookEventNow(duration: Duration) {
        val currentDateTime = LocalDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.MINUTES)
        val startTime = currentDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTime = LocalDateTime.from(duration.addTo(currentDateTime)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        eventService.createEvent(
                EventCreateRequestBody(
                        startTime = startTime,
                        roomId = ROOM_ID,
                        endTime = endTime
                )
        ).subscribeOn(Schedulers.io())
                .flatMapCompletable {
                    Completable.fromAction {
                        val createdEvent = it.toEvent(ROOM_ID)
                        updateAllEventsIfRequired(currentDateTime.toLocalDate())

                        lastAddedEventManager.apply {
                            lastAddedEventEndTime = createdEvent.endTime
                            lastAddedEventId = createdEvent.id
                        }
                    }
                }
                .subscribe(
                        { logUserAddMeetingEvent(startTime, endTime, ROOM_ID) },
                        { Timber.e(it, "Failed to book new event and cache it") }
                )
    }

    @SuppressLint("CheckResult")
    fun bookEvent(eventCreateRequestBody: EventCreateRequestBody, bookedRoomId: String): Completable {
        return eventService.createEvent(eventCreateRequestBody)
                .subscribeOn(Schedulers.io())
                .flatMapCompletable {
                    Completable.fromAction {
                        val createdEvent = it.toEvent(bookedRoomId)
                        updateAllEventsIfRequired(createdEvent.startTime.toLocalDate())

                        lastAddedEventManager.apply {
                            lastAddedEventEndTime = createdEvent.endTime
                            lastAddedEventId = createdEvent.id
                        }

                        logUserAddMeetingEvent(eventCreateRequestBody.startTime, eventCreateRequestBody.endTime, eventCreateRequestBody.roomId)
                    }
                }
    }

    @SuppressLint("CheckResult")
    fun updateAllEventsIfRequired(currentDate: LocalDate) {
        ignoreUpdateStatusManager.subscribeForIgnoreUpdateStatus()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { onUpdateAllEventsIfRequiredSubscribe(it, currentDate) },
                        { Timber.e(it, "Failed to subscribe on ignoreUpdateStatusManager") }
                )
    }

    private fun onUpdateAllEventsIfRequiredSubscribe(isDatabaseDifferentFromServer: Boolean, currentDate: LocalDate) {
        if (!isDatabaseDifferentFromServer) updateAllEvents(currentDate)
    }

    @SuppressLint("CheckResult")
    private fun updateAllEvents(currentDate: LocalDate) {
        roomService.getRoomList(
                RoomRequestBody(
                        currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
        )
                .subscribeOn(Schedulers.io())
                .flatMapCompletable {
                    Completable.fromAction {
                        it.forEach {
                            eventDao.deleteAndInsert(
                                    it.toEventList(), it.id,
                                    currentDate.atStartOfDay(),
                                    currentDate.atStartOfDay().plusDays(1)
                            )
                        }
                    }
                }
                .subscribe(
                        { Unit },
                        { Timber.e(it, "Failed to get all events from com.provectus_it.bookme.network and cache them") }
                )
    }

    @SuppressLint("CheckResult")
    fun deleteEvent(eventId: Int, eventEndTime: LocalDateTime) {
        eventService.updateOrDeleteEvent(
                EventUpdateOrDeleteRequestBody(
                        eventEndTime.toEpochMilliAtDefaultZone(),
                        eventId,
                        true
                )
        ).subscribeOn(Schedulers.io())
                .subscribe(
                        { onDeleteEvent(eventEndTime) },
                        { Timber.e(it, "Failed to delete an event and cache it") }
                )
    }

    private fun onDeleteEvent(eventEndTime: LocalDateTime) {
        ignoreUpdateStatusManager.shouldIgnoreUpdateSubject.onNext(false)
        updateAllEventsIfRequired(eventEndTime.toLocalDate())
    }

    @SuppressLint("CheckResult")
    private fun updateEvent(eventId: Int, eventEndTime: LocalDateTime) {
        eventService.updateOrDeleteEvent(
                EventUpdateOrDeleteRequestBody(
                        eventEndTime.truncatedTo(ChronoUnit.MINUTES).toEpochMilliAtDefaultZone(),
                        eventId,
                        false
                )
        ).subscribeOn(Schedulers.io())
                .subscribe(
                        { onUpdateEvent(eventEndTime) },
                        { Timber.e(it, "Failed to update an event and cache it") }
                )
    }

    private fun onUpdateEvent(eventEndTime: LocalDateTime) {
        ignoreUpdateStatusManager.shouldIgnoreUpdateSubject.onNext(false)
        updateAllEventsIfRequired(eventEndTime.toLocalDate())
    }


    fun undoLocalCheckoutBooking(checkoutDateTime: LocalDateTime, checkoutEvent: Event) {
        val checkoutEventStartTime = checkoutEvent.startTime
        val checkoutEventEndTime = checkoutEvent.endTime

        if (Duration.between(checkoutEventStartTime, checkoutDateTime).toMinutes() < MIN_DURATION_OF_MEETING_IN_MINUTES) {
            createLocallyEvent(checkoutEvent)
        } else {
            updateLocallyEvent(checkoutEvent, checkoutEventEndTime)
        }

        logUserUndoCheckoutEvent()
    }

    fun localCheckoutBooking(checkoutDateTime: LocalDateTime, checkoutEvent: Event) {
        val checkoutEventStartTime = checkoutEvent.startTime

        if (Duration.between(checkoutEventStartTime, checkoutDateTime).toMinutes() < MIN_DURATION_OF_MEETING_IN_MINUTES) {
            deleteLocallyEvent(checkoutEvent.id)
        } else {
            updateLocallyEvent(checkoutEvent, checkoutDateTime)
        }

        logUserCheckoutEvent()
    }

    fun checkoutBooking(checkoutDateTime: LocalDateTime, checkoutEvent: Event) {
        val checkoutEventStartTime = checkoutEvent.startTime
        val checkoutEventEndTime = checkoutEvent.endTime
        val checkoutEventId = checkoutEvent.id

        if (Duration.between(checkoutEventStartTime, checkoutDateTime).toMinutes() < MIN_DURATION_OF_MEETING_IN_MINUTES) {
            deleteEvent(checkoutEventId, checkoutEventEndTime)
        } else {
            updateEvent(checkoutEventId, checkoutDateTime)
        }

    }

    @SuppressLint("CheckResult")
    private fun deleteLocallyEvent(eventId: Int) {
        eventDao.deleteEvent(eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    @SuppressLint("CheckResult")
    private fun updateLocallyEvent(event: Event, currentDateTime: LocalDateTime) {
        eventDao.deleteEvent(event.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {insertUpdatedItem(event, currentDateTime)}
    }

    private fun insertUpdatedItem(currentEvent: Event, currentDateTime: LocalDateTime) {
        val updatedEvent = Event(
                currentEvent.id,
                currentEvent.userFirstName,
                currentEvent.userLastName,
                currentEvent.displayName,
                currentEvent.role,
                currentEvent.startTime,
                currentDateTime.truncatedTo(ChronoUnit.MINUTES),
                currentEvent.roomId,
                currentEvent.isCurrent
        )

        eventDao.insertItem(updatedEvent)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    private fun createLocallyEvent(event: Event) {
        eventDao.insertItem(event)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    @SuppressLint("CheckResult")
    private fun updateEventsOnCalendarChanges() {
        socketService.subscribeForGoogleCalendarUpdates()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.event }
                .subscribe(
                        { onGoogleCalendarUpdate(it) },
                        { Timber.e(it, "Failed to update events on google calendar changes") }
                )
    }

    private fun onGoogleCalendarUpdate(event: EventResponseBody) {
        val startDate = event.startTime.toLocalDate()
        val endDate = event.endTime.toLocalDate()

        if (endDate != startDate) {
            updateAllEventsIfRequired(endDate)
        }

        updateAllEventsIfRequired(startDate)
    }

    private fun selectCurrentEvent(eventList: List<EventObject>): List<EventObject> {
        val newEventList = mutableListOf<EventObject>()
        eventList.forEach { setCurrentEventListItem(newEventList, it) }

        return newEventList
    }

    private fun setCurrentEventListItem(eventList: MutableList<EventObject>, eventObject: EventObject) {
        if (isBetweenEventTime(eventObject)) {
            eventList.add(eventObject.copyAsEventObject(isCurrent = true))
        } else {
            eventList.add(eventObject)
        }
    }

    private fun isBetweenEventTime(eventObject: EventObject): Boolean {
        val currentDateTime = LocalDateTime.now()
        return currentDateTime.isAfter(eventObject.startTime) && currentDateTime.isBefore(eventObject.endTime)
    }

    companion object {
        const val MIN_DURATION_OF_MEETING_IN_MINUTES = 5
    }

}