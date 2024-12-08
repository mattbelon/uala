package com.test.uala.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.test.uala.ui.locationModel.LocationModel

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