package com.provectus_it.bookme.entity.request_body

import com.google.gson.annotations.SerializedName

class EventCreateRequestBody(
        //TODO remove superEventId field
        //TODO remove space in subject string
        @SerializedName("attendees") val attendees: List<String> = mutableListOf(),
        @SerializedName("description") val description: String = " ",
        @SerializedName("endDtm") val endTime: Long,
        @SerializedName("regular") val regular: String = "daily",
        @SerializedName("roomId") val roomId: String,
        @SerializedName("startDtm") val startTime: Long,
        @SerializedName("subject") val subject: String = " ",
        @SerializedName("superEventId") val superEventId: Int? = null,
        @SerializedName("type") val type: String = "MEETING"
)