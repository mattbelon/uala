package com.test.uala.ui.components

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


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