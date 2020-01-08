package com.provectus_it.bookme.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.provectus_it.bookme.ui.screen.event_list.EventObject
import org.threeten.bp.LocalDateTime

@Entity(tableName = "event")
data class Event @Ignore constructor(
        @PrimaryKey
        @ColumnInfo(name = "id")
        @SerializedName("eventId")
        val id: Int,

        @ColumnInfo(name = "userFirstName")
        @SerializedName("userFirstName")
        val userFirstName: String,

        @ColumnInfo(name = "userLastName")
        @SerializedName("userLastName")
        val userLastName: String,

        @ColumnInfo(name = "displayName")
        @SerializedName("displayName")
        val displayName: String,

        @ColumnInfo(name = "role")
        @SerializedName("role")
        val role: String,

        @ColumnInfo(name = "startTime")
        @SerializedName("startDtm")
        override val startTime: LocalDateTime,

        @ColumnInfo(name = "endTime")
        @SerializedName("endDtm")
        override val endTime: LocalDateTime,

        @ColumnInfo(name = "roomId")
        @SerializedName("roomId")
        val roomId: String,

        @Ignore
        override var isCurrent: Boolean
) : EventObject {

    constructor(
            id: Int,
            userFirstName: String?,
            userLastName: String?,
            displayName: String,
            role: String,
            startTime: LocalDateTime,
            endTime: LocalDateTime,
            roomId: String
    ) : this(
            id,
            userFirstName ?: "",
            userLastName ?: "",
            displayName,
            role,
            startTime,
            endTime,
            roomId,
            false
    )

    override fun copyAsEventObject(isCurrent: Boolean) = copy(isCurrent = isCurrent)

}