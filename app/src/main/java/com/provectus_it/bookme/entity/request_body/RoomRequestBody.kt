package com.provectus_it.bookme.entity.request_body

import com.google.gson.annotations.SerializedName

class RoomRequestBody(
    @SerializedName("date") val startTime: Long
)