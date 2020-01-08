package com.provectus_it.bookme.entity.request_body

import com.google.gson.annotations.SerializedName

class AvailableRoomsRequestBody(
        @SerializedName("startDtm") val startTime: Long,
        @SerializedName("endDtm") val endTime: Long
)