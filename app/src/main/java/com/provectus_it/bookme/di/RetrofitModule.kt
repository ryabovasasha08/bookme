package com.provectus_it.bookme.di

import com.google.gson.Gson
import com.provectus_it.bookme.Constants
import com.provectus_it.bookme.network.service.EventService
import com.provectus_it.bookme.network.service.RoomService
import com.provectus_it.bookme.util.logging.RxErrorHandlingCallAdapterFactory
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofitModule = module {
    factory { provideRxErrorHandlingCallAdapterFactory() }
    single { provideRetrofit(get(), get(), get()) }
    single { provideRoomService(get()) }
    single { provideEventService(get()) }
}

fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson, rxErrorHandlingCallAdapterFactory: RxErrorHandlingCallAdapterFactory): Retrofit {
    return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(rxErrorHandlingCallAdapterFactory)
            .build()
}

fun provideRoomService(retrofit: Retrofit): RoomService = retrofit.create(RoomService::class.java)

fun provideEventService(retrofit: Retrofit): EventService = retrofit.create(EventService::class.java)

fun provideRxErrorHandlingCallAdapterFactory() = RxErrorHandlingCallAdapterFactory()