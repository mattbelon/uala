package com.test.uala.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import com.test.uala.ui.HomeEvent
import com.test.uala.ui.HomeUiState
import com.test.uala.ui.locationModel.LocationModel


@Composable
fun CityListScreen(
    homeEvent: (HomeEvent) -> Unit,
    homeState: HomeUiState,
    onCitySelected: (LocationModel) -> Unit
) {
    // Estas variables las dejo, porque como veran en el historial empece asi y luego de la charla tecnica lo pase a estados
    /*val isRefreshing by mainViewModel.isRefreshing.collectAsState(initial = false)
    val filteredCities by mainViewModel.filteredCities.collectAsStateWithLifecycle(initialValue = emptyList())
*/
    val configuration = LocalConfiguration.current

    /*val selectedCity by mainViewModel.selectedCity.collectAsState(initial = null)
    val infoCity by mainViewModel.infoCity.collectAsState(initial = null)*/
    //var query by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        homeEvent.invoke(HomeEvent.LoadCities)
    }

    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CityListColumn(
                    filteredCities = homeState.filteredCities,
                    query = homeState.query,
                    onQueryChange = {
                        homeEvent.invoke(HomeEvent.UpdateSearchQuery(it))
                    },
                    onCityClick = {
                        onCitySelected.invoke(it)
                        homeEvent.invoke(HomeEvent.SelectedCity(it))
                    },
                    onInfoClick = {
                        showDialog = true
                        homeEvent.invoke(HomeEvent.InfoLocation(it))
                    },

                    onFavoriteClick = { homeEvent.invoke(HomeEvent.AddFav(it)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("CityListColumn"),
                    onLoadMore = { homeEvent.invoke(HomeEvent.LoadMore) },
                )
                /*
                Con este código puedo mostrar el mapa en landscape unicamente si hay una ciudad seleccionada
                selectedCity?.let { city ->
                     MapScreen(
                         lat = city.coord.lat.toFloat(),
                         lon = city.coord.lon.toFloat(),
                         cityName = city.name,
                         modifier = Modifier.fillMaxWidth().weight(1f)
                     )
                 }*/
                MapBox(
                    selectedCity = homeState.selectedCity,
                    modifier = Modifier.weight(1f)
                )
                if (showDialog && homeState.infoCity != null) {
                    ShowCityInfoDialog(infoCity = homeState.infoCity!!) {
                        showDialog = false
                    }
                }
            }

        }

        Configuration.ORIENTATION_PORTRAIT -> {
            Column(modifier = Modifier.fillMaxSize()) {
                CityListColumn(
                    filteredCities = homeState.filteredCities,
                    query = homeState.query,
                    onQueryChange = {
                        homeEvent.invoke(HomeEvent.UpdateSearchQuery(it))
                    },
                    onCityClick = {
                        onCitySelected.invoke(it)
                        homeEvent.invoke(HomeEvent.SelectedCity(it))
                    },
                    onInfoClick = {
                        showDialog = true
                        homeEvent.invoke(HomeEvent.InfoLocation(it))
                    },
                    onFavoriteClick = { homeEvent.invoke(HomeEvent.AddFav(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("CityListColumn"),
                    onLoadMore = { homeEvent.invoke(HomeEvent.LoadMore) }
                )
                if (showDialog && homeState.infoCity != null) {
                    ShowCityInfoDialog(infoCity = homeState.infoCity!!) {
                        showDialog = false
                    }
                }
            }
        }

        Configuration.ORIENTATION_SQUARE -> {
            TODO()
        }

        Configuration.ORIENTATION_UNDEFINED -> {
            TODO()
        }
    }

    if (homeState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}