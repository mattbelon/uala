package com.test.uala.ui

import android.app.Activity
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.test.uala.MainViewModel
import com.test.uala.ui.locationModel.LocationModel

@Composable
fun HomeScreen() {
    val mainViewModel: MainViewModel = hiltViewModel()

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "city_list") {
        composable("city_list") {
            CityListScreen(
                mainViewModel = mainViewModel,
                onCitySelected = { city ->
                    navController.navigate("map/${city.lat}/${city.lon}/${city.name}")
                }
            )
            //SimpleLazy(mainViewModel = mainViewModel)
        }
        composable(
            "map/{lat}/{lon}/{cityName}",
            arguments = listOf(
                navArgument("lat") { type = NavType.FloatType },
                navArgument("lon") { type = NavType.FloatType },
                navArgument("cityName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getFloat("lat") ?: 0f
            val lon = backStackEntry.arguments?.getFloat("lon") ?: 0f
            val cityName = backStackEntry.arguments?.getString("cityName") ?: ""
            MapScreen(lat = lat, lon = lon, cityName = cityName, modifier = Modifier.fillMaxWidth())
        }
    }
}


@Composable
fun CityListScreen(
    mainViewModel: MainViewModel,
    onCitySelected: (LocationModel) -> Unit
) {
    val isRefreshing by mainViewModel.isRefreshing.collectAsState(initial = false)
    val filteredCities by mainViewModel.filteredCities.collectAsStateWithLifecycle(initialValue = emptyList())

    val configuration = LocalConfiguration.current

    val selectedCity by mainViewModel.selectedCity.collectAsState(initial = null)
    val infoCity by mainViewModel.infoCity.collectAsState(initial = null)
    var query by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mainViewModel.loadCities()
    }

    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CityListColumn(
                    filteredCities = filteredCities,
                    query = query,
                    onQueryChange = {
                        query = it
                        mainViewModel.updateSearchQuery(it)
                    },
                    onCityClick = { mainViewModel.selectedLocation(it) },
                    onInfoClick = {
                        showDialog = true
                        mainViewModel.infoLocation(it)
                    },
                    onFavoriteClick = { city -> mainViewModel.addToFavorites(city) },
                    modifier = Modifier.weight(1f),
                    onLoadMore = {mainViewModel.loadMoreLocations()}
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
                    selectedCity = selectedCity,
                    modifier = Modifier.weight(1f)
                )
                if (showDialog && infoCity != null) {
                    ShowCityInfoDialog(infoCity = infoCity!!) {
                        showDialog = false
                    }
                }
            }

        }

        Configuration.ORIENTATION_PORTRAIT -> {
            Column(modifier = Modifier.fillMaxSize()) {
                CityListColumn(
                    filteredCities = filteredCities,
                    query = query,
                    onQueryChange = {
                        query = it
                        mainViewModel.updateSearchQuery(it)
                    },
                    onCityClick = { city -> onCitySelected(city) },
                    onInfoClick = { city ->
                        showDialog = true
                        mainViewModel.infoLocation(city)
                    },
                    onFavoriteClick = { city -> mainViewModel.addToFavorites(city) },
                    modifier = Modifier.fillMaxWidth(),
                    onLoadMore = {mainViewModel.loadMoreLocations()}
                )
                if (showDialog && infoCity != null) {
                    ShowCityInfoDialog(infoCity = infoCity!!) {
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

    if (isRefreshing) {
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

@Composable
fun ShowCityInfoDialog(infoCity: LocationModel?, onDismiss: () -> Unit) {
    infoCity?.let { info ->
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(text = "City Information")
            },
            text = {
                Column {
                    Text(text = "Title: ${info.name}")
                    Text(text = "Coordinates: Lat ${info.lat}, Lng ${info.lon}")
                }
            },
            confirmButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text(text = "OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(lat: Float, lon: Float, cityName: String, modifier: Modifier) {
    Box(modifier = modifier) {
        val context = LocalContext.current
        val mapView = remember { MapView(context).apply { onCreate(null) } }
        Column {
            TopAppBar(
                title = { Text("Back") },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? Activity)?.onBackPressed()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize(),
                update = { mapView ->
                    mapView.getMapAsync { googleMap ->
                        googleMap.uiSettings.isZoomControlsEnabled = true
                        val latLng = LatLng(lat.toDouble(), lon.toDouble())
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title("Marker in $cityName")
                                .snippet("Coords -> Lat:$lat Lon:$lon")
                        )
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                    }
                }
            )
        }
    }
}

@Composable
fun MapBox(selectedCity: LocationModel?, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        GoogleMapView(selectedCity)
    }
}

@Composable
fun CityListColumn(
    filteredCities: List<LocationModel>,
    query: String,
    onQueryChange: (String) -> Unit,
    onCityClick: (LocationModel) -> Unit,
    onInfoClick: (LocationModel) -> Unit,
    onFavoriteClick: (LocationModel) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear text"
                    )
                }
            }
        },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            placeholder = { Text("Buscar ciudades") }
        )

        LazyColumn {
            items(filteredCities, key = { city -> city.id }) { city ->
                Log.d("location:Compose", "Composing city: ${city.name}, isFav: ${city.isFav}")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            onCityClick(city)
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${city.name}, ${city.country}",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${city.lat}, ${city.lon}",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(
                        onClick = { onInfoClick(city) }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "More information",
                        )
                    }
                    IconButton(
                        onClick = { onFavoriteClick(city) }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            contentDescription = "Add to Favorites",
                            tint = if (city.isFav) {
                                Color.Red
                            } else {
                                Color.Gray
                            }
                        )
                    }
                }
            }
            item {
                LaunchedEffect(filteredCities.size) {
                    if (filteredCities.isNotEmpty()) {
                        onLoadMore()
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleMapView(selectedCity: LocationModel?) {
    val context = LocalContext.current
    val mapView = remember { MapView(context).apply { onCreate(null) } }
    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize(),
        update = { mapView ->
            mapView.getMapAsync { googleMap ->
                googleMap.uiSettings.isZoomControlsEnabled = true
                selectedCity?.let { city ->
                    val latLng = LatLng(city.lat, city.lon)
                    googleMap.addMarker(
                        MarkerOptions().position(latLng).title("Marker in ${city.name}")
                            .snippet("Coords -> Lat:${city.lat} ${city.lon}")
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                }
            }
        }
    )
}