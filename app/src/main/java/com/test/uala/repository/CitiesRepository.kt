package com.test.uala.repository

import com.test.uala.data.models.Location
import com.test.uala.data.remote.api.ApiService
import com.test.uala.utils.BaseRemoteResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CitiesRepository: BaseRemoteResponse() {
    private val apiService: ApiService = ApiService.createApiService()
    suspend fun getList(): List<Location> = withContext(Dispatchers.IO) {
        return@withContext safeApiCall { apiService.getMapList() }
    }
}