package com.test.uala.ui.locationModel


data class LocationModel(
    val id: Int,
    val country: String,
    val name: String,
    val lon: Double,
    val lat: Double,
    var isFav: Boolean = false
)