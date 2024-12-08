package com.test.uala.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.test.uala.ui.locationModel.LocationModel

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