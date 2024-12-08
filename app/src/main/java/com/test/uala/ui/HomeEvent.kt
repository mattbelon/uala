package com.test.uala.ui

import com.test.uala.ui.locationModel.LocationModel

sealed interface HomeEvent {
    data object LoadCities : HomeEvent
    data object LoadMore : HomeEvent
    data class SelectedCity(val selected: LocationModel) : HomeEvent
    data class InfoLocation(val selected: LocationModel?) : HomeEvent
    data class AddFav(val location: LocationModel) : HomeEvent
    data class UpdateSearchQuery(val query: String) : HomeEvent

}