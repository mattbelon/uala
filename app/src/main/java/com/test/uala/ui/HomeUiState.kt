package com.test.uala.ui

import com.test.uala.ui.locationModel.LocationModel

data class HomeUiState(
    var filteredCities: List<LocationModel> = emptyList(),
    var selectedCity: LocationModel? = null,
    var infoCity: LocationModel? = null,
    var query: String = "",
    var showDialog: Boolean = false,
    var exception: String? = null,
    var isLoading: Boolean = false
)