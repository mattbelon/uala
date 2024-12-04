package com.test.uala

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.uala.data.local.room.LocationRoom
import com.test.uala.data.models.Location
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
                                        private val savedStateHandle: SavedStateHandle,
                                        private val citiesRepository: CitiesRepository
) :
    ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    private val _apiState = MutableLiveData<HomeUiState>()
    val apiState: LiveData<HomeUiState> = _apiState

    private val _allCities = MutableStateFlow<List<LocationModel>>(emptyList())
    val allCities: StateFlow<List<LocationModel>> = _allCities

    private val _selectedCity = MutableStateFlow<LocationModel?>(null)
    val selectedCity: StateFlow<LocationModel?> = _selectedCity

    private var isDataLoaded = false

    /*init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                citiesRepository.getAllLocations().collect { locationList ->
                    _allCities.value = locationList
                }
            }
        }
    }*/

    fun loadCities() {
        if (isDataLoaded) return
        _isRefreshing.value = true
        viewModelScope.launch(Dispatchers.IO) {

            try {
                withContext(Dispatchers.IO) {
                    _allCities.value = citiesRepository.loadAndFetchLocations()
                }

                isDataLoaded = true
            } catch (e: IOException) {
                _apiState.value = HomeUiState.Error(ParserExceptions())
            } catch (e: Exception) {
                _apiState.value = HomeUiState.Error(ServerExceptions())
            } finally {
                _isRefreshing.value = false
            }
        }}

    fun loadCitiesSinFlow() {
        if (isDataLoaded) return
        _isRefreshing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = withContext(Dispatchers.IO) {
                    citiesRepository.LoadLocationsSinFlow()
                }
                _allCities.value = list

                isDataLoaded = true
            } catch (e: IOException) {
                _apiState.value = HomeUiState.Error(ParserExceptions())
            } catch (e: Exception) {
                _apiState.value = HomeUiState.Error(ServerExceptions())
            } finally {
                _isRefreshing.value = false
            }
        }}

        /*fun loadCitiesList() {
            if (isDataLoaded) return
            _isRefreshing.value = true

            viewModelScope.launch {
                try {
                    Log.d("IO","llamando")
                    val response = citiesRepository.getList()
                    _allCities.value = response.map { mapToLocationModel(it) }
                    Log.d("test",response.toString())
                    isDataLoaded = true
                } catch (e: IOException) {
                    e.message?.let { Log.e("TEST IO", it) }
                    _apiState.value = HomeUiState.Error(ParserExceptions())
                } catch (e: Exception) {
                    _apiState.value = HomeUiState.Error(ServerExceptions())
                    e.message?.let { Log.e("TEST", it) }
                } finally {
                    _isRefreshing.value = false
                }
            }
        }*/
    private fun mapToLocationModel(locationRoom: Location): LocationModel {
        return LocationModel(
            id = locationRoom.id,
            country = locationRoom.country,
            name = locationRoom.name,
            lon = locationRoom.coord.lon,
            lat = locationRoom.coord.lat,
            isFav = locationRoom.isFav
        )
    }
        fun selectedLocation(selected: LocationModel) {
            _selectedCity.value = selected
        }

        fun addToFavorites(location: LocationModel) {
            val updatedList = allCities.value.map {
                if (it.id == location.id) {
                    it.copy(isFav = !it.isFav)
                } else {
                    it
                }
            }
            _allCities.value = updatedList
        }

    }