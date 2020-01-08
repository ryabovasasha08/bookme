package com.provectus_it.bookme.entity.response_body

import com.google.gson.annotations.SerializedName
import com.provectus_it.bookme.entity.Event
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit

data class EventResponseBody(
        @SerializedName("eventId") val id: Int,
        @SerializedName("user") val user: User,
        @SerializedName("startDtm") val startTime: LocalDateTime,
        @SerializedName("endDtm") val endTime: LocalDateTime,
        @SerializedName("displayName") val displayName: String
) {
    //TODO:ask backend to truncate events and stop doing it here
    fun toEvent(roomId: String): Event {
        return Event(
                id,
                user.firstName,
                user.lastName,
                displayName,
                user.role,
                startTime.truncatedTo(ChronoUnit.MINUTES),
                endTime.truncatedTo(ChronoUnit.MINUTES),
                roomId
        )
    }
}