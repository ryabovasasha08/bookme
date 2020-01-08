package com.provectus_it.bookme.entity.response_body

import com.google.gson.annotations.SerializedName

data class UpdateResponseBody(
        @SerializedName("event") val event: EventResponseBody
)