package com.provectus_it.bookme.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.provectus_it.bookme.Constants
import com.provectus_it.bookme.database.type_converter.LocalDateTimeConverter
import com.provectus_it.bookme.entity.Event
import com.provectus_it.bookme.entity.Room

@Database(entities = [Room::class, Event::class], version = Constants.DB_VERSION)
@TypeConverters(LocalDateTimeConverter::class)
abstract class BookMeDb : RoomDatabase() {
    abstract fun roomDao(): RoomDao
    abstract fun eventDao(): EventDao
}