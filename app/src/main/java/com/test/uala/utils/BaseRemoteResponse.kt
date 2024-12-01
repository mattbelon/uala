package com.test.uala.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response

open class BaseRemoteResponse {

    suspend fun <T : Any> safeApiCall(
        call: suspend () -> Response<T>
    ): T {
        val result: Result<T> = safeApiResult(call)
        var data: T? = null

        when (result) {
            is Result.Success ->
                data = result.data

            is Result.Error -> {
                throw result.exception
            }
        }
        return data
    }

    private suspend fun <T : Any> safeApiResult(
        call: suspend () -> Response<T>
    ): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = call.invoke()
                if (response.isSuccessful) {
                    Result.Success(response.body()!!)
                } else {
                    val message = getErrorBody(response.errorBody())
                    Result.Error(Exception(message))
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    private fun getErrorBody(errorBody: ResponseBody?): String {
        return try {
            errorBody?.string().orEmpty()
        } catch (e: Exception) {
            ""
        }
    }
}
sealed class Result<out T : Any> {
    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}