package com.test.uala

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.uala.repository.CitiesRepository
import com.test.uala.ui.HomeEvent
import com.test.uala.ui.HomeUiState
import com.test.uala.ui.locationModel.LocationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val citiesRepository: CitiesRepository
) :
    ViewModel() {
    var state by mutableStateOf(HomeUiState())
        private set

    private val _favoriteLocations = MutableStateFlow<List<LocationModel>>(emptyList())
    private val _originalCities = MutableStateFlow<List<LocationModel>>(emptyList())

    private var isDataLoaded = false
    private var currentOffset = 0
    private val limit = 20
    private var isSearch = false

    init {
        viewModelScope.launch {
            citiesRepository.getFavoriteLocations()
                .collect { locations ->
                    Log.d("location", "locations fav size ${locations.size}")
                    _favoriteLocations.value = locations
                    updateFavoriteStatus()
                }
        }

        viewModelScope.launch {
            snapshotFlow { state.query }
                .collectLatest { query ->
                    applyFilter(query)
                }
        }

    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.AddFav -> {
                addToFavorites(event.location)
            }

            is HomeEvent.InfoLocation -> {
                state = state.copy(infoCity = event.selected)
            }

            HomeEvent.LoadCities -> {
                loadCities()
            }

            HomeEvent.LoadMore -> {
                loadMoreLocations()
            }

            is HomeEvent.SelectedCity -> {
                state = state.copy(selectedCity = event.selected)
            }

            is HomeEvent.UpdateSearchQuery -> {
                updateSearchQuery(event.query)
            }
        }
    }

    private fun applyFilter(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                isSearch = false
                state = state.copy(filteredCities = _originalCities.value)
            } else {
                isSearch = true
                val result = withContext(Dispatchers.IO) { citiesRepository.searchLocations(query) }
                state = state.copy(filteredCities = result)
            }
        }
    }

    private fun updateFavoriteStatus() {
        val favoriteIds = _favoriteLocations.value.map { it.id }.toSet()

        val updatedCities = state.filteredCities.map { city ->
            city.copy(isFav = favoriteIds.contains(city.id))
        }

        state = state.copy(filteredCities = updatedCities)
    }


    private fun loadCities() {
        if (isDataLoaded) return
        state = state.copy(isLoading = true)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.IO) {
                    citiesRepository.loadAndFetchLocations()
                    loadMoreLocations()
                }
                updateFavoriteStatus()
                isDataLoaded = true
            } catch (e: IOException) {
                state = state.copy(exception = e.localizedMessage)
            } catch (e: Exception) {
                state = state.copy(exception = e.localizedMessage)
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    private fun loadMoreLocations() {
        if (isSearch) {
            return
        }
        viewModelScope.launch {
            val newLocations = withContext(Dispatchers.IO) {
                citiesRepository.getLocationsPaginated(
                    limit,
                    currentOffset
                )
            }
            if (newLocations.isNotEmpty()) {
                _originalCities.value += newLocations
                state = state.copy(
                    filteredCities = state.filteredCities + newLocations
                )
                currentOffset += newLocations.size
            }
        }

    }

    private fun addToFavorites(location: LocationModel) {
        updateFavoriteStatus(location.id, !location.isFav)
    }

    private fun updateFavoriteStatus(id: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            citiesRepository.updateFavoriteStatus(id, isFavorite)
        }
    }

    private fun updateSearchQuery(query: String) {
        state = state.copy(query = query)
        applyFilter(query)
    }

}