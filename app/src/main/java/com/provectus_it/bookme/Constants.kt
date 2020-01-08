package com.provectus_it.bookme

import org.threeten.bp.Duration

object Constants {
    const val DB_VERSION = 1
    const val DB_TABLE_NAME = "here might be your table name"

    var ROOM_NAME = "Chicago"
    var ROOM_ID = "here might be your room id"
    var BASE_URL = "here might be your base url"
    var WEBSOCKET_URL = "here might be your websocket url"
     var SESSION_ID = "here might be your session id"

    const val AMPLITUDE_API_KEY = "here might be your amplitude key"

    const val MINUTES_IN_HOUR = 60
    val DURATIONS = arrayOf(
            Duration.ofMinutes(90),
            Duration.ofMinutes(60),
            Duration.ofMinutes(45),
            Duration.ofMinutes(30),
            Duration.ofMinutes(25),
            Duration.ofMinutes(20),
            Duration.ofMinutes(15),
            Duration.ofMinutes(10),
            Duration.ofMinutes(5)
    )
}