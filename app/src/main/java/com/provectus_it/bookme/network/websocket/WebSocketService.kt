package com.provectus_it.bookme.network.websocket

import com.provectus_it.bookme.entity.response_body.UpdateResponseBody
import com.tinder.scarlet.ws.Receive
import io.reactivex.Flowable

interface WebSocketService {

    @Receive
    fun subscribeForGoogleCalendarUpdates(): Flowable<UpdateResponseBody>

}