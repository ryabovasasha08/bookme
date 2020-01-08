package com.provectus_it.bookme.entity.response_body

import com.google.gson.annotations.SerializedName

data class ApiError(
        @SerializedName("message") val message: String
)