package com.test.uala.data.local.room

import com.test.uala.data.models.Coord
import com.test.uala.data.models.Location

class LocationMapper() {

    fun ParseToRoom(location: Location): LocationRoom {
        return LocationRoom(
            id = location.id,
            country = location.country,
            name = location.name,
            lon = location.coord.lon,
            lat = location.coord.lat,
            isFav = false
        )
    }
}