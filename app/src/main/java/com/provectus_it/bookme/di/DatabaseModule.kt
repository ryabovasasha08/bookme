package com.provectus_it.bookme.di

import android.content.Context
import androidx.room.Room
import com.provectus_it.bookme.Constants
import com.provectus_it.bookme.database.BookMeDb
import com.provectus_it.bookme.database.EventDao
import com.provectus_it.bookme.database.RoomDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { provideDatabase(androidContext()) }
    single { provideRoomDao(get()) }
    single { provideEventDao(get()) }
}

fun provideDatabase(context: Context): BookMeDb {
    return Room.databaseBuilder(context, BookMeDb::class.java, Constants.DB_TABLE_NAME).build()
}

fun provideRoomDao(database: BookMeDb): RoomDao = database.roomDao()

fun provideEventDao(database: BookMeDb): EventDao = database.eventDao()
