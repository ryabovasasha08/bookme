package com.provectus_it.bookme.entity.request_body

import com.google.gson.annotations.SerializedName

class EventUpdateOrDeleteRequestBody(
    @SerializedName("endDtm") val endTime: Long,
    @SerializedName("eventId") val id: Int,
    @SerializedName("removeByUser") val shouldBeDeleted: Boolean
)