package com.test.uala

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.uala.repository.CitiesRepository
import com.test.uala.ui.HomeUiState
import com.test.uala.ui.cities.ParserExceptions
import com.test.uala.ui.cities.ServerExceptions
import com.test.uala.ui.locationModel.LocationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val citiesRepository: CitiesRepository
) :
    ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    private val _apiState = MutableLiveData<HomeUiState>()

    private val _originalCities = MutableStateFlow<List<LocationModel>>(emptyList())
    private val _favoriteLocations = MutableStateFlow<List<LocationModel>>(emptyList())
    private val _filteredCities = MutableStateFlow<List<LocationModel>>(emptyList())
    val filteredCities: StateFlow<List<LocationModel>> = _filteredCities
    private val _searchQuery = MutableStateFlow("")

    private val _selectedCity = MutableStateFlow<LocationModel?>(null)
    val selectedCity: StateFlow<LocationModel?> = _selectedCity

    private val _infoCity = MutableStateFlow<LocationModel?>(null)
    val infoCity: StateFlow<LocationModel?> = _infoCity

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
            _searchQuery
                .debounce(500)
                .collectLatest { query ->
                    applyFilter(query)
                }
        }
    }
    fun applyFilter(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                isSearch = false
                _filteredCities.value = _originalCities.value
            } else {
                isSearch = true
                _filteredCities.value =
                    withContext(Dispatchers.IO) { citiesRepository.searchLocations(query) }
            }
        }
    }

    private fun updateFavoriteStatus() {
        val favoriteIds = _favoriteLocations.value.map { it.id }.toSet()

        val updatedCities = _filteredCities.value.map { city ->
            city.copy(isFav = favoriteIds.contains(city.id))
        }

        _filteredCities.value = updatedCities

    }

    fun loadCities() {
        if (isDataLoaded) return
        _isRefreshing.value = true
        viewModelScope.launch(Dispatchers.IO) {

            try {
                withContext(Dispatchers.IO) {
                    citiesRepository.loadAndFetchLocations()
                    loadMoreLocations()
                }
                updateFavoriteStatus()
                isDataLoaded = true
            } catch (e: IOException) {
                _apiState.value = HomeUiState.Error(ParserExceptions())
            } catch (e: Exception) {
                _apiState.value = HomeUiState.Error(ServerExceptions())
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    fun loadMoreLocations() {
        if (isSearch) {
            return
        }
        viewModelScope.launch {
            val newLocations = withContext(Dispatchers.IO){citiesRepository.getLocationsPaginated(limit, currentOffset)}
            if (newLocations.isNotEmpty()) {
                _originalCities.value += newLocations
                _filteredCities.value += newLocations
                currentOffset += newLocations.size
            }
                }

    }

    fun selectedLocation(selected: LocationModel) {
        _selectedCity.value = selected
    }

    fun infoLocation(selected: LocationModel?) {
        _infoCity.value = selected
    }

    fun addToFavorites(location: LocationModel) {
        updateFavoriteStatus(location.id, !location.isFav)
    }

    private fun updateFavoriteStatus(id: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            citiesRepository.updateFavoriteStatus(id, isFavorite)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

}