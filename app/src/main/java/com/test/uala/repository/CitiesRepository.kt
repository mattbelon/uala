package com.test.uala.repository

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.test.uala.data.local.room.AppDatabase
import com.test.uala.data.local.room.LocationDao
import com.test.uala.data.local.room.LocationMapper
import com.test.uala.data.local.room.LocationRoom
import com.test.uala.data.models.Location
import com.test.uala.data.remote.api.ApiService
import com.test.uala.ui.locationModel.LocationModel
import com.test.uala.utils.BaseRemoteResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CitiesRepository @Inject constructor(private val locationDao: LocationDao) : BaseRemoteResponse() {
    private val apiService: ApiService = ApiService.createApiService()
    private val locationMapper = LocationMapper()
    suspend fun getList(): List<Location> = withContext(Dispatchers.IO) {
        return@withContext safeApiCall { apiService.getMapList() }
    }

    suspend fun loadAndFetchLocations() {
        val localList = locationDao.checkData()
        if (localList.isEmpty()) {
            val remoteList = getList()
            Log.d("TESTING", "remote list :"+remoteList.size.toString())
            val roomEntities = remoteList.map { locationMapper.ParseToRoom(it) }

            locationDao.insertLocation(roomEntities)
        }
    }

    private fun mapToLocationModel(locationRoom: LocationRoom): LocationModel {
        return LocationModel(
            id = locationRoom.id,
            country = locationRoom.country,
            name = locationRoom.name,
            lon = locationRoom.lon,
            lat = locationRoom.lat,
            isFav = locationRoom.isFav
        )
    }

    fun getFavoriteLocations(): Flow<List<LocationModel>> {
        return locationDao.getFavoriteLocationsFlow().map { locationRooms ->
            locationRooms.map { mapToLocationModel(it) }
        }
    }

    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean) {
        locationDao.updateFavoriteStatus(id, isFavorite)
    }

    fun getLocationsPaginated(limit: Int, offset: Int): List<LocationModel>{
        return locationDao.getLocationsPaginated(limit, offset).map { mapToLocationModel(it) }
    }
    fun searchLocations(query: String): List<LocationModel>{
        return locationDao.searchLocations(query).map { mapToLocationModel(it) }
    }

}