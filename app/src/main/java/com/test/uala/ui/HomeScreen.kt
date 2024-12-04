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
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
    val allCities by mainViewModel.allCities.collectAsState(initial = emptyList())
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val selectedCity by mainViewModel.selectedCity.collectAsState(initial = null)
    var query by remember { mutableStateOf("") }
    val filteredCities = remember(query, allCities) {
        allCities.filter { it.name.startsWith(query, ignoreCase = true) }.sortedBy { it.name }
    }
    Log.d("TESTING", "ciudades : " + allCities)

    val cantidadFavs = allCities.filter { it.isFav }
    val cantidadFavsFiltrados = filteredCities.filter { it.isFav }
    Log.d("TESTING", "cantidad de favoritos: " + cantidadFavs)
    Log.d("TESTING", "cantidadFavsFiltrados: " + cantidadFavsFiltrados)

    LaunchedEffect(Unit) {
        mainViewModel.loadCities()
    }

    Log.d("CityListScreen", "isLandscape: $isLandscape")

    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CityListColumn(
                    filteredCities = filteredCities,
                    query = query,
                    onQueryChange = { query = it },
                    onCityClick = { mainViewModel.selectedLocation(it) },
                    onFavoriteClick = { city -> mainViewModel.addToFavorites(city) },
                    modifier = Modifier.weight(1f)
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
            }

        }

        Configuration.ORIENTATION_PORTRAIT -> {
            Column(modifier = Modifier.fillMaxSize()) {
                CityListColumn(
                    filteredCities = filteredCities,
                    query = query,
                    onQueryChange = { query = it },
                    onCityClick = { city -> onCitySelected(city) },
                    onFavoriteClick = { city -> mainViewModel.addToFavorites(city) },
                    modifier = Modifier.fillMaxWidth()
                )
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
    onFavoriteClick: (LocationModel) -> Unit,
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            placeholder = { Text("Buscar ciudades") }
        )

        LazyColumn {
            items(filteredCities) { city ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onCityClick(city) },
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
/*
@Composable
fun CityListColumn(
    filteredCities: List<City>,
    query: String,
    onQueryChange: (String) -> Unit,
    onCityClick: (City) -> Unit,
    onFavoriteClick: (City) -> Unit
) {
    Column(
        modifier = Modifier
            .weight(0.4f)
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            placeholder = { Text("Buscar ciudades") }
        )

        LazyColumn {
            items(filteredCities) { city ->
                val isFav = remember { mutableStateOf(city.isFav) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onCityClick(city) },
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
                            text = "${city.coord.lat}, ${city.coord.lon}",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    IconButton(
                        onClick = { onFavoriteClick(city) }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            contentDescription = "Add to Favorites",
                            tint = if (isFav.value) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun HomeScreen(mainViewModel: MainViewModel) {
    val allCities by mainViewModel.allCities.collectAsState(initial = emptyList())

    val selectedCity by mainViewModel.selectedCity.collectAsState(initial = null)
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val filteredCities = remember(query, allCities) {
        allCities.filter { it.name.startsWith(query, ignoreCase = true)}.sortedBy { it.name }
    }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(Unit) {
        mainViewModel.loadCitiesList()
    }
    Row(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                placeholder = { Text("Buscar ciudades") }
            )

            LazyColumn {
                items(filteredCities) { city ->
                    val isFav = remember { mutableStateOf(city.isFav) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { mainViewModel.selectedLocation(city) },
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
                                text = "${city.coord.lat}, ${city.coord.lon}",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        IconButton(
                            onClick = {
                                mainViewModel.addToFavorites(city)
                                Toast.makeText(context, "CLICKEADO", Toast.LENGTH_LONG).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Favorite,
                                contentDescription = "Add to Favorites",
                                tint = if (isFav.value) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }
                }
            }


        }

        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            GoogleMapView(selectedCity)
        }
    }
}

@Composable
fun GoogleMapView(selectedCity: Location?) {
    val context = LocalContext.current
    val mapView = remember { MapView(context).apply { onCreate(null) } }

    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize(),
        update = { mapView ->
            mapView.getMapAsync { googleMap ->
                googleMap.uiSettings.isZoomControlsEnabled = true
                selectedCity?.let { city ->
                    val latLng = getLatLngForCity(city.coord)
                    googleMap.addMarker(MarkerOptions().position(latLng).title("Marker in ${city.name}").snippet("Coords -> Lat:${city.coord.lat} ${city.coord.lon}"))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                }
            }
        }
    )
}

fun getLatLngForCity(city:Coord):LatLng{
    return LatLng(city.lat, city.lon)
}*/
