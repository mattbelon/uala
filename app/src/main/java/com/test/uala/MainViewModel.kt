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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.io.IOException

class MainViewModel : ViewModel() {


    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    private val _apiState = MutableLiveData<HomeUiState>()
    val apiState: LiveData<HomeUiState> = _apiState

    private val citiesListRepository: CitiesRepository = CitiesRepository()

    private val _allCities = MutableStateFlow<List<Location>>(emptyList())
    val allCities: StateFlow<List<Location>> = _allCities

    private val _selectedCity = MutableStateFlow<Location?>(null)
    val selectedCity: StateFlow<Location?> = _selectedCity

    private var isDataLoaded = false


    fun loadCitiesList() {
        if (isDataLoaded) return
        _isRefreshing.value = true

        viewModelScope.launch {
            try {
                Log.d("IO","llamando")
                val response = citiesListRepository.getList()
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
    }

    fun selectedLocation(selected:Location){
        _selectedCity.value = selected
    }


    /*fun addToFavorites(location: Location) {
        val currentList = allCities.value.toMutableList()

        currentList.find { it.id == location.id }?.apply {
            isFav = !isFav
        }

        _allCities.value = currentList
    }*/

    fun addToFavorites(location: Location) {
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