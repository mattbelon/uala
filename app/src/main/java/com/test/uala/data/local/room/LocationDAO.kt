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

    @Query("SELECT * FROM location_room WHERE name LIKE :query || '%' ORDER BY name")
    fun searchLocations(query: String): List<LocationRoom>

    @Query("SELECT * FROM location_room ORDER BY name LIMIT 5")
    fun getAllLocationsFlow(): Flow<List<LocationRoom>>

    @Query("SELECT * FROM location_room ORDER BY name LIMIT :limit OFFSET :offset")
    fun getLocationsPaginated(limit: Int, offset: Int): List<LocationRoom>

    @Query("SELECT * FROM location_room")
    fun getAllLocations(): List<LocationRoom>

    @Query("SELECT * FROM location_room WHERE isFav = 1")
    fun getFavoriteLocationsFlow(): Flow<List<LocationRoom>>

    @Query("SELECT * FROM location_room WHERE id = :id")
    fun getLocationById(id: Int): Flow<LocationRoom?>

    @Query("UPDATE location_room SET isFav = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)

    @Query("SELECT * FROM location_room Limit 1")
    fun checkData(): List<LocationRoom>
}