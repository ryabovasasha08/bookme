package com.provectus_it.bookme.database.type_converter

import androidx.room.TypeConverter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId


class LocalDateTimeConverter {

    private val zoneId = ZoneId.of("Europe/Kiev")

    @TypeConverter
    fun fromLocalDateTime(localDateTime: LocalDateTime): Long {
        return localDateTime.atZone(zoneId).toEpochSecond()
    }

    @TypeConverter
    fun toLocalDateTime(timestamp: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), zoneId)
    }

}