package com.provectus_it.bookme.entity.response_body

import com.google.gson.annotations.SerializedName
import com.provectus_it.bookme.entity.Event
import com.provectus_it.bookme.entity.Room

data class RoomResponseBody(
        @SerializedName("roomId") val id: String,
        @SerializedName("name") val name: String,
        @SerializedName("floor") val floor: Int,
        @SerializedName("security") val isSecure: Boolean,
        @SerializedName("capacity") val capacity: String,
        @SerializedName("tv") val hasTv: Boolean,
        @SerializedName("events") val todayEvents: List<EventResponseBody>
) {

    fun toRoom(): Room {
        return Room(
                id,
                name.removeSuffix(SUFFIX_TO_REMOVE),
                floor,
                isSecure,
                capacity,
                hasTv
        )
    }

    fun toEventList(): List<Event> {
        return todayEvents
                .map { it.toEvent(id) }
                .toList()
    }

    companion object {
        const val SUFFIX_TO_REMOVE = " conference room"
    }
}