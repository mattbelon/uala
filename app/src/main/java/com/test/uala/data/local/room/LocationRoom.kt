package com.test.uala.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_room")
data class LocationRoom(
    @PrimaryKey val id: Int,
    val country: String,
    val name: String,
    val lon: Double,
    val lat: Double,
    var isFav: Boolean = false
)