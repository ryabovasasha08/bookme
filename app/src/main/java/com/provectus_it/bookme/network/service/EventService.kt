package com.provectus_it.bookme.network.service

import com.provectus_it.bookme.entity.request_body.EventCreateRequestBody
import com.provectus_it.bookme.entity.request_body.EventUpdateOrDeleteRequestBody
import com.provectus_it.bookme.entity.response_body.EventResponseBody
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface EventService {

    @POST("/api/event/create")
    fun createEvent(@Body eventCreateRequestBody: EventCreateRequestBody): Single<EventResponseBody>

    @POST("/api/event/update/short")
    fun updateOrDeleteEvent(@Body eventUpdateOrDeleteRequestBody: EventUpdateOrDeleteRequestBody): Single<EventResponseBody>

}