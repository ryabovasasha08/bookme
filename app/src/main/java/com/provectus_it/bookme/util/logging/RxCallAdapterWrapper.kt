package com.provectus_it.bookme.util.logging

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import com.provectus_it.bookme.R
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class RxCallAdapterWrapper(private val wrapped: CallAdapter<R, Any>) : CallAdapter<R, Any> {

    override fun adapt(call: Call<R>): Any {
        return when (val adaptedByWrapped = wrapped.adapt(call)) {
            is Single<*> -> adaptedByWrapped.onErrorResumeNext { Single.error(convertToApiExceptionIfNeeded(it)) }
            is Completable -> adaptedByWrapped.onErrorResumeNext { Completable.error(convertToApiExceptionIfNeeded(it)) }
            else -> throw IllegalStateException("Unable to adapt $javaClass")
        }
    }

    override fun responseType(): Type = wrapped.responseType()

    private fun convertToApiExceptionIfNeeded(e: Throwable): Throwable {
        if (e is HttpException) {
            return ApiException.from(e)
        }

        return e
    }

}