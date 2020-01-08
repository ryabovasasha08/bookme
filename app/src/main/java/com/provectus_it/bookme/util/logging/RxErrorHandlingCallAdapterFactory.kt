package com.provectus_it.bookme.util.logging

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.provectus_it.bookme.R
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.Type

class RxErrorHandlingCallAdapterFactory : CallAdapter.Factory() {

    private val rxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()

    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        return RxCallAdapterWrapper(rxJava2CallAdapterFactory.get(returnType, annotations, retrofit) as CallAdapter<R, Any>)
    }

}