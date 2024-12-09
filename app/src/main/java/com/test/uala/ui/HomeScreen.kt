package com.test.uala.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.test.uala.ui.components.CityList
import com.test.uala.ui.components.MapScreen

@Composable
fun HomeScreen(
    state: HomeUiState, homeEvent: (HomeEvent) -> Unit,
    navController: NavHostController
) {

    NavHost(navController = navController, startDestination = "city_list") {
        composable("city_list") {
            CityList(
                homeState = state,
                homeEvent = homeEvent,
                onCitySelected = { city ->
                    Log.d("Test", "City selected: ${city.name}, ${city.country}")
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









