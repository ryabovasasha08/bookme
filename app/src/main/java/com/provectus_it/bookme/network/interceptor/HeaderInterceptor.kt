package com.provectus_it.bookme.network.interceptor

import com.provectus_it.bookme.Constants.SESSION_ID
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response

class HeaderInterceptor : Interceptor {

    override fun intercept(chain: Chain): Response {
        val request = chain.request()
                .newBuilder()
                .addHeader(HEADER_SESSION_ID, SESSION_ID)
                .build()

        return chain.proceed(request)
    }

    companion object {
        const val HEADER_SESSION_ID = "BOO-Session-ID"
    }

}