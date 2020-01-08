package com.provectus_it.bookme.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "room")
data class Room(
        @PrimaryKey
        @ColumnInfo(name = "id")
        @SerializedName("roomId")
        val id: String,

        @ColumnInfo(name = "name")
        @SerializedName("name")
        val name: String,

        @ColumnInfo(name = "floor")
        @SerializedName("floor")
        val floor: Int,

        @ColumnInfo(name = "isSecure")
        @SerializedName("security")
        val isSecure: Boolean,

        @ColumnInfo(name = "capacity")
        @SerializedName("capacity")
        val capacity: String,

        @ColumnInfo(name = "hasTv")
        @SerializedName("tv")
        val hasTv: Boolean
)