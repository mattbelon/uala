package com.test.uala.data.remote.api

import com.squareup.moshi.Moshi
import com.test.uala.Constants
import com.test.uala.data.models.Location
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

interface ApiService {
    @GET(Constants.CITIESENDPOINT)
    suspend fun getMapList(): Response<List<Location>>

    companion object {
        fun createApiService(): ApiService {
            val moshi = Moshi.Builder().build()
            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASEURL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}