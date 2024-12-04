package com.test.uala.data.local.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "location_room",
    indices = [Index(value = ["id","name", "country"])]
)
data class LocationRoom(
    @PrimaryKey val id: Int,
    val country: String,
    val name: String,
    val lon: Double,
    val lat: Double,
    var isFav: Boolean = false
)