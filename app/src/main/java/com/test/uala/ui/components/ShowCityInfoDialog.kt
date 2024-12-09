package com.test.uala.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.test.uala.ui.locationModel.LocationModel


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