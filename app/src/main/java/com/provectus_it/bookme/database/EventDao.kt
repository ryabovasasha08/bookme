package com.provectus_it.bookme.database

import androidx.room.*
import com.provectus_it.bookme.entity.Event
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.threeten.bp.LocalDateTime

@Dao
interface EventDao : BaseDao<Event> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(obj: Event): Completable

    @Query("SELECT * FROM event WHERE (startTime >= :startOfDay) AND (startTime < :endOfDay)")
    fun subscribe(startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flowable<List<Event>>

    @Query("SELECT * FROM event WHERE (roomId IS :roomId) AND (startTime >= :startOfDay) AND (startTime < :endOfDay) ORDER BY startTime")
    fun subscribe(roomId: String, startOfDay: LocalDateTime, endOfDay: LocalDateTime): Flowable<List<Event>>

    @Query("SELECT * FROM event WHERE (roomId IS :roomId) AND (startTime >= :startOfDay) AND (startTime < :endOfDay) ORDER BY startTime")
    fun get(roomId: String, startOfDay: LocalDateTime, endOfDay: LocalDateTime): Single<List<Event>>

    @Query("DELETE FROM event WHERE (roomId IS :roomId) AND (startTime >= :startOfDay) AND (startTime < :endOfDay)")
    fun delete(roomId: String, startOfDay: LocalDateTime, endOfDay: LocalDateTime)

    @Query("DELETE FROM event WHERE (id IS :eventId)")
    fun deleteEvent(eventId: Int): Completable

    @Transaction
    fun deleteAndInsert(events: List<Event>, roomId: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime) {
        delete(roomId, startDateTime, endDateTime)
        insert(events)
    }

}