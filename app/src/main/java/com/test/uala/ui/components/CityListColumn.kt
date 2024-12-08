package com.test.uala.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.test.uala.ui.locationModel.LocationModel


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
                        }
                        .testTag("CityItem_${city.id}"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("CityList")
                    ) {
                        Text(
                            text = "${city.name}, ${city.country}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("CityItem_${city.name}"),
                            style = MaterialTheme.typography.bodyMedium,
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
                            modifier = Modifier.testTag("CityInfoButton_${city.id}")
                        )
                    }
                    IconButton(
                        onClick = { onFavoriteClick(city) }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            contentDescription = "Add to Favorites",
                            modifier = Modifier.testTag("CityFavoriteButton_${city.id}"),
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