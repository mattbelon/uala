package com.test.uala.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: List<LocationRoom>)

    @Query("SELECT * FROM location_room WHERE name LIKE '%' || :query || '%'")
    fun searchLocations(query: String): Flow<List<LocationRoom>>

    @Query("SELECT * FROM location_room")
    fun getAllLocationsFlow(): Flow<List<LocationRoom>>

    @Query("SELECT * FROM location_room")
    fun getAllLocations(): List<LocationRoom>

    @Query("SELECT * FROM location_room WHERE isFav = 1")
    fun getFavoriteLocations(): Flow<List<LocationRoom>>

    @Query("SELECT * FROM location_room WHERE id = :id")
    fun getLocationById(id: Int): Flow<LocationRoom?>
}