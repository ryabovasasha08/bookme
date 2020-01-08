package com.provectus_it.bookme.repository

import android.annotation.SuppressLint
import com.provectus_it.bookme.Constants.MINUTES_IN_HOUR
import com.provectus_it.bookme.Constants.ROOM_ID
import com.provectus_it.bookme.database.EventDao
import com.provectus_it.bookme.database.RoomDao
import com.provectus_it.bookme.entity.*
import com.provectus_it.bookme.entity.request_body.AvailableRoomsRequestBody
import com.provectus_it.bookme.entity.request_body.RoomRequestBody
import com.provectus_it.bookme.entity.response_body.RoomResponseBody
import com.provectus_it.bookme.network.service.RoomService
import com.provectus_it.bookme.ui.screen.event_list.EventObject
import com.provectus_it.bookme.util.addFreeEvents
import com.provectus_it.bookme.util.endOfDay
import com.provectus_it.bookme.util.ignore_update.IgnoreUpdateStatusManager
import com.provectus_it.bookme.util.scheduleEveryMinuteUpdate
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Flowable.combineLatest
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber

class RoomRepository(
        private val roomService: RoomService,
        private val roomDao: RoomDao,
        private val eventDao: EventDao,
        private val ignoreUpdateStatusManager: IgnoreUpdateStatusManager
) {

    fun getStatusedRoomList(currentDateTime: LocalDateTime): Flowable<List<StatusedRoom>> {
        updateRoomListIfRequired()

        return combineLatest(
                roomDao.subscribe(),
                getAvailabilityInfoList(currentDateTime),
                BiFunction<List<Room>, List<AvailabilityInfo>, List<StatusedRoom>> { rooms, availabilityInfos -> toStatusedRoomList(availabilityInfos, rooms) }
        )
    }

    fun getRoomList(): Flowable<List<Room>> {
        updateRoomListIfRequired()
        return roomDao.subscribe()
    }

    fun getActualRoomData(): Flowable<Room> {
        updateRoomListIfRequired()

        return roomDao
                .subscribe()
                .filter { it.isNotEmpty() }
                .map { getRoomById(it) }
    }

    fun getActualStatusedRoom(currentDateTime: LocalDateTime): Flowable<StatusedRoom> {
        return getStatusedRoomList(currentDateTime)
                .filter { it.isNotEmpty() }
                .map { getStatusedRoomById(it) }
    }

    private fun getRoomById(rooms: List<Room>): Room? {
        return rooms.find { it.id == ROOM_ID }
    }

    private fun getStatusedRoomById(rooms: List<StatusedRoom>): StatusedRoom? {
        return rooms.find { it.room.id == ROOM_ID }
    }

    private fun toStatusedRoomList(availabilityInfos: List<AvailabilityInfo>, rooms: List<Room>): List<StatusedRoom> {
        val currentDateTime = LocalDateTime.now()
        val statusedRoomList = mutableListOf<StatusedRoom>()

        for (room in rooms) {
            var availabilityInfo = availabilityInfos.find { it.roomId == room.id }

            if (availabilityInfo == null) {
                val isFree = true
                val timeUntil = currentDateTime.endOfDay()
                val currentEvent = Event(
                        0,
                        "",
                        "",
                        "",
                        "",
                        currentDateTime.toLocalDate().atStartOfDay(),
                        currentDateTime.endOfDay(),
                        room.id
                )
                val timeLeft = minutesBetweenDateTimes(timeUntil, currentDateTime)
                val hoursLeft = timeLeft / MINUTES_IN_HOUR
                val minutesLeft = timeLeft.rem(MINUTES_IN_HOUR)
                availabilityInfo = AvailabilityInfo(
                        room.id,
                        isFree,
                        timeUntil,
                        currentEvent,
                        hoursLeft,
                        minutesLeft
                )
            }

            statusedRoomList.add(StatusedRoom(room, availabilityInfo))
        }

        return statusedRoomList
    }

    private fun getAvailabilityInfoList(currentDateTime: LocalDateTime): Flowable<List<AvailabilityInfo>> {
        val startDateTime = currentDateTime.toLocalDate().atStartOfDay()
        val endDateTime = currentDateTime.endOfDay()

        return combineLatest(
                eventDao.subscribe(startDateTime, endDateTime),
                scheduleEveryMinuteUpdate(),
                BiFunction<List<Event>, Unit, List<Event>> { events, _ -> events }
        )
                .map { eventList -> eventList.groupBy { it.roomId } }
                .map { eventsMap -> toAvailabilityInfo(eventsMap) }
    }

    private fun toAvailabilityInfo(eventsMap: Map<String, List<Event>>): List<AvailabilityInfo> {
        val currentDateTime = LocalDateTime.now()
        val availabilityInfos = mutableListOf<AvailabilityInfo>()

        eventsMap.forEach {
            val isFree = isRoomFree(it.value, currentDateTime)
            val timeUntil = getDateTimeUntil(it.value, isFree, currentDateTime)
            val currentEvent = getCurrentEvent(addFreeEvents(
                    getSortedByStartTimeEventList(it.value),
                    currentDateTime.toLocalDate()
            ), currentDateTime)
            val timeLeft = minutesBetweenDateTimes(timeUntil, currentDateTime)
            val hoursLeft = timeLeft / MINUTES_IN_HOUR
            val minutesLeft = timeLeft.rem(MINUTES_IN_HOUR)

            val availabilityInfo = AvailabilityInfo(
                    it.key,
                    isFree,
                    timeUntil,
                    currentEvent,
                    hoursLeft,
                    minutesLeft
            )

            availabilityInfos.add(availabilityInfo)
        }

        return availabilityInfos
    }

    private fun isRoomFree(eventList: List<Event>, currentDateTime: LocalDateTime): Boolean {
        return !eventList.any { event -> isBetweenEventTime(event, currentDateTime) }
    }

    private fun isBetweenEventTime(eventObject: EventObject, dateTime: LocalDateTime): Boolean {
        return dateTime.isAfter(eventObject.startTime) && dateTime.isBefore(eventObject.endTime)
    }

    private fun getDateTimeUntil(eventList: List<Event>, isFree: Boolean, currentDateTime: LocalDateTime): LocalDateTime {
        return if (isFree) {
            getFreeDateTimeUntil(eventList, currentDateTime)
        } else {
            getBusyDateTimeUntil(eventList, currentDateTime)
        }
    }

    private fun getBusyDateTimeUntil(eventList: List<EventObject>, currentDateTime: LocalDateTime): LocalDateTime {
        return getBusyFilteredCurrentEvent(eventList, currentDateTime)!!.endTime
    }

    private fun isThereEventAfter(event: EventObject, eventList: List<EventObject>) =
            eventList.find { nextEvent -> event.endTime == nextEvent.startTime } == null

    private fun getFreeDateTimeUntil(eventList: List<EventObject>, currentDateTime: LocalDateTime): LocalDateTime {
        return if (eventList.none { minutesBetweenDateTimes(it.startTime, currentDateTime) > 0 }) {
            currentDateTime.endOfDay()
        } else {
            getFreeFilteredCurrentEvent(eventList, currentDateTime)!!.startTime
        }
    }

    private fun minutesBetweenDateTimes(dateTime: LocalDateTime, currentDateTime: LocalDateTime): Int {
        return ChronoUnit.MINUTES.between(currentDateTime.truncatedTo(ChronoUnit.MINUTES), dateTime).toInt()
    }

    private fun cacheRooms(roomResponseBodies: List<RoomResponseBody>) {
        roomResponseBodies
                .map { it.toRoom() }
                .toList()
                .apply { roomDao.deleteAndInsert(this) }
    }

    private fun cacheTodayEvents(roomResponseBodies: List<RoomResponseBody>) {
        roomResponseBodies.forEach {
            val startDateTime = LocalDateTime.now(ZoneId.systemDefault()).toLocalDate().atStartOfDay()
            val endDateTime = LocalDateTime.now(ZoneId.systemDefault()).toLocalDate().atStartOfDay().plusDays(1)
            eventDao.deleteAndInsert(it.toEventList(), it.id, startDateTime, endDateTime)
        }
    }

    fun getRoomNameById(roomId: String) = roomDao.getName(roomId)

    fun getAvailableRoomList(startDateTime: Long, endDateTime: Long): Single<List<RoomResponseBody>> {
        return roomService.getAvailableRoomList(AvailableRoomsRequestBody(startDateTime, endDateTime))
    }

    @SuppressLint("CheckResult")
    private fun updateRoomList() {
        roomService.getRoomList(
                RoomRequestBody(
                        LocalDateTime.now().toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
        )
                .subscribeOn(Schedulers.io())
                .flatMapCompletable { Completable.fromAction { (cacheData(it)) } }
                .subscribe(
                        { Unit },
                        { Timber.e(it, "Failed to get room list from com.provectus_it.bookme.network") }
                )
    }

    @SuppressLint("CheckResult")
    fun updateRoomListIfRequired() {
        ignoreUpdateStatusManager.subscribeForIgnoreUpdateStatus()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { onUpdateRoomListIfRequiredSubscribe(it) },
                        { Timber.e(it, "Failed to subscribe on ignoreUpdateStatusManager") }
                )
    }

    private fun onUpdateRoomListIfRequiredSubscribe(isDatabaseDifferentFromServer: Boolean) {
        if (!isDatabaseDifferentFromServer) updateRoomList()
    }

    private fun cacheData(roomResponseBodyList: List<RoomResponseBody>) {
        cacheRooms(roomResponseBodyList)
        cacheTodayEvents(roomResponseBodyList)
    }

    private fun getFreeFilteredCurrentEvent(eventList: List<EventObject>, currentDateTime: LocalDateTime): EventObject? {
        return eventList
                .filter { minutesBetweenDateTimes(it.startTime, currentDateTime) > 0 }
                .minBy { event -> minutesBetweenDateTimes(event.startTime, currentDateTime) }
    }

    private fun getBusyFilteredCurrentEvent(eventList: List<EventObject>, currentDateTime: LocalDateTime): EventObject? {
        return eventList
                .filter { event -> isThereEventAfter(event, eventList) && event.endTime.isAfter(currentDateTime) }
                .minBy { it.startTime }
    }

    private fun getSortedByStartTimeEventList(eventList: List<Event>) = eventList.sortedWith(compareBy { it.startTime })

    fun getFirstNextFreeEvent(eventList: List<EventObject>, currentDateTime: LocalDateTime): EventObject? {
        return eventList
                .filter { event -> (event is FreeEvent) && minutesBetweenDateTimes(event.startTime, currentDateTime) > 0 }
                .minBy { it.startTime }
    }

    private fun getCurrentEvent(eventList: List<EventObject>, currentDateTime: LocalDateTime): EventObject {
        return eventList.first { it.startTime.isBefore(currentDateTime) && it.endTime.isAfter(currentDateTime) }
    }

}