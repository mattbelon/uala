package com.test.uala

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val citiesRepository:CitiesRepository) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    private val _apiState = MutableLiveData<HomeUiState>()
    val apiState: LiveData<HomeUiState> = _apiState

    private val _allCities = MutableStateFlow<List<LocationModel>>(emptyList())
    val allCities: StateFlow<List<LocationModel>> = _allCities

    private val _selectedCity = MutableStateFlow<LocationModel?>(null)
    val selectedCity: StateFlow<LocationModel?> = _selectedCity

    private var isDataLoaded = false

    init {
        viewModelScope.launch {
            citiesRepository.getAllLocations().collect { locationList ->
                _allCities.value = locationList
            }
        }
    }

    fun loadCities() {
        viewModelScope.launch(Dispatchers.IO) {
            citiesRepository.LoadLocations()
        }
    }

    /*fun loadCitiesList() {
        if (isDataLoaded) return
        _isRefreshing.value = true

        viewModelScope.launch {
            try {
                Log.d("IO","llamando")
                val response = citiesRepository.getList()
                _allCities.value = response
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

    fun selectedLocation(selected:LocationModel){
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