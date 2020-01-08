package com.provectus_it.bookme.network.service

import com.provectus_it.bookme.entity.request_body.AvailableRoomsRequestBody
import com.provectus_it.bookme.entity.request_body.RoomRequestBody
import com.provectus_it.bookme.entity.response_body.RoomResponseBody
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface RoomService {

    @POST("/api/room/today")
    fun getRoomList(@Body roomRequestBody: RoomRequestBody): Single<List<RoomResponseBody>>

    @POST("/api/room/free")
    fun getAvailableRoomList(@Body availableRoomsRequestBody: AvailableRoomsRequestBody): Single<List<RoomResponseBody>>

}