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

    fun searchLocations(query: String): Flow<List<LocationRoom>> {
        return locationDao.searchLocations(query)
    }

    fun getAllLocationsFlow(): Flow<List<LocationRoom>> {
        return locationDao.getAllLocationsFlow()
    }
    suspend fun LoadLocations() {
        val locationList = locationDao.getAllLocations()
        if(locationList.isEmpty()){
            val remoteList = getList()
            locationDao.insertLocation(remoteList.map { locationMapper.ParseToRoom(it) })
        }
    }

    suspend fun LoadLocationsSinFlow():List<LocationModel> {
        val locationList = locationDao.getAllLocationsSinFlow()
        return locationList.map { mapToLocationModel(it) }
    }

    suspend fun loadAndFetchLocations(): List<LocationModel> {
        // Intentamos cargar las ubicaciones desde Room
        val localList = locationDao.getAllLocationsSinFlow()
        Log.d("TESTING","local list"+ localList.size.toString())
        return if (localList.isEmpty()) {
            // Si no hay datos locales, los obtenemos de la API
            val remoteList = getList()
            Log.d("TESTING", "remote list :"+remoteList.size.toString())
            // Transformamos los datos remotos al modelo de Room
            val roomEntities = remoteList.map { locationMapper.ParseToRoom(it) }

            // Guardamos los datos en Room
            locationDao.insertLocation(roomEntities)

            // Transformamos las entidades guardadas al modelo esperado por la UI
            roomEntities.map { mapToLocationModel(it) }
        } else {
            // Si ya hay datos locales, simplemente los transformamos y los devolvemos
            localList.map { mapToLocationModel(it) }
        }
    }




    fun getLocationById(id: Int): Flow<LocationRoom?> {
        return locationDao.getLocationById(id)
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

    fun getAllLocations(): Flow<List<LocationModel>> {
        return locationDao.getAllLocationsFlow().map { locationRooms ->
            locationRooms.map { mapToLocationModel(it) }
        }
    }

    fun getFavoriteLocations(): Flow<List<LocationModel>> {
        return locationDao.getFavoriteLocations().map { locationRooms ->
            locationRooms.map { mapToLocationModel(it) }
        }
    }


}