package com.test.uala.ui

import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.test.uala.MainViewModel
import com.test.uala.data.models.Coord
import com.test.uala.data.models.Location


@Composable
fun HomeScreen(mainViewModel: MainViewModel) {
    val allCities by mainViewModel.allCities.collectAsState(initial = emptyList())

    val selectedCity by mainViewModel.selectedCity.collectAsState(initial = null)
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val filteredCities = remember(query, allCities) {
        allCities.filter { it.name.startsWith(query, ignoreCase = true)}.sortedBy { it.name }
    }

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
}