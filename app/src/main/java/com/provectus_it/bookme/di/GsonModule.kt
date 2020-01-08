package com.provectus_it.bookme.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.provectus_it.bookme.network.type_adapter.LocalDateDeserializer
import com.provectus_it.bookme.network.type_adapter.LocalDateTimeDeserializer
import org.koin.dsl.module
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

val gsonModule = module {
    single { provideGson() }
}

fun provideGson(): Gson {
    return GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
            .registerTypeAdapter(LocalDate::class.java, LocalDateDeserializer())
            .create()
}