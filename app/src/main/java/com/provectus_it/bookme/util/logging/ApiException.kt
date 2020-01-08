package com.provectus_it.bookme.util.logging

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import com.provectus_it.bookme.entity.response_body.ApiError
import okhttp3.ResponseBody
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.IOException

class ApiException(message: String) : RuntimeException(message) {

    companion object {
        fun from(httpException: HttpException): ApiException {
            val response = httpException.response()
            val responseBody = response!!.errorBody()

            var message = ""

            if (responseBody == null) {
                return ApiException(message)
            }

            var errorBody: ApiError? = null

            try {
                errorBody = getErrorBody(responseBody)
            } catch (e: IOException) {
                Timber.e(e, "Failed to convert ResponseBody to ErrorBody: code=${response.code()}, url =${response.raw().request.url}")
            }

            if (errorBody != null) {
                message = errorBody.message
            }

            return ApiException(message)
        }

        private fun getErrorBody(body: ResponseBody): ApiError {
            val converter = GsonConverterFactory.create().responseBodyConverter(ApiError::class.java, null, null)

            return converter!!.convert(body) as ApiError
        }

    }

}