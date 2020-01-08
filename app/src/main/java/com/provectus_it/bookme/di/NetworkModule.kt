package com.provectus_it.bookme.di

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.provectus_it.bookme.Constants.WEBSOCKET_URL
import com.provectus_it.bookme.network.interceptor.HeaderInterceptor
import com.provectus_it.bookme.network.websocket.WebSocketService
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.messageadapter.gson.GsonMessageAdapter
import com.tinder.scarlet.streamadapter.rxjava2.RxJava2StreamAdapterFactory
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module

val networkModule = module {
    single { provideOkHttpClient(get(), get(), get()) }
    single { provideHttpLoggingInterceptor() }
    single { provideStethoInterceptor() }
    single { provideHeaderInterceptor() }
    single { provideWebSocket(get(), get()) }
}

fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        stethoInterceptor: StethoInterceptor,
        headerInterceptor: HeaderInterceptor
): OkHttpClient {
    return OkHttpClient.Builder()
            .addNetworkInterceptor(stethoInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(headerInterceptor)
            .build()
}

fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

    return httpLoggingInterceptor
}

fun provideStethoInterceptor(): StethoInterceptor = StethoInterceptor()

fun provideHeaderInterceptor(): HeaderInterceptor = HeaderInterceptor()

fun provideWebSocket(okHttpClient: OkHttpClient, gson: Gson): WebSocketService {
    return Scarlet.Builder()
            .webSocketFactory(okHttpClient.newWebSocketFactory(WEBSOCKET_URL))
            .addMessageAdapterFactory(GsonMessageAdapter.Factory(gson))
            .addStreamAdapterFactory(RxJava2StreamAdapterFactory())
            .build()
            .create()
}