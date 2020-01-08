package com.provectus_it.bookme.entity.response_body

import com.google.gson.annotations.SerializedName

class User(
        @SerializedName("firstName") val firstName: String?,
        @SerializedName("lastName") val lastName: String?,
        @SerializedName("role") val role: String
)