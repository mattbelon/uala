package com.test.uala

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.test.uala.ui.CityListColumn
import com.test.uala.ui.HomeScreen
import com.test.uala.ui.HomeUiState
import com.test.uala.ui.ShowCityInfoDialog
import com.test.uala.ui.locationModel.LocationModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun myTest() {

        composeTestRule.setContent {
            Text("You can set any Compose content!")
        }
    }
}

@RunWith(RobolectricTestRunner::class)
class CityListColumnTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `clicking city triggers onCityClick`() {

        val city = LocationModel(1, "FR", "Paris", 12.34, 56.78, false)
        var selectedCity: LocationModel? = null

        composeTestRule.setContent {
            CityListColumn(
                filteredCities = listOf(city),
                query = "",
                onQueryChange = {},
                onCityClick = { selectedCity = it },
                onInfoClick = {},
                onFavoriteClick = {},
                onLoadMore = {},
                modifier = Modifier
            )
        }


        composeTestRule.onNodeWithText("Paris, FR").performClick()


        assert(selectedCity == city)
    }
}

@RunWith(RobolectricTestRunner::class)
class HomeScreenNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun navigatesToMapWhenCitySelected() {

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())

        val testState = HomeUiState(
            filteredCities = listOf(LocationModel(1, "ES", "Madrid", 40.4168, -3.7038, false)),
            query = "",
            selectedCity = LocationModel(1, "ES", "Madrid", 40.4168, -3.7038, false),
            infoCity = null,
            isLoading = false
        )

        composeTestRule.setContent {
            CompositionLocalProvider {
                HomeScreen(
                    state = testState,
                    homeEvent = {},
                    navController
                )
            }
        }

        composeTestRule.onNodeWithText("Madrid, ES").assertExists().performClick()
        composeTestRule.onRoot().printToLog("TAG")

        val currentRoute = navController.currentBackStackEntry?.destination?.route
        assert(currentRoute == "map/{lat}/{lon}/{cityName}") {
            "La navegación no ocurrió correctamente, ruta actual: $currentRoute"
        }

    }
}

@RunWith(RobolectricTestRunner::class)
class ShowCityInfoDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `dialog shows city information`() {
        val city = LocationModel(1, "Us", "Miami", 12.34, 56.78, false)

        composeTestRule.setContent {
            ShowCityInfoDialog(infoCity = city) {}
        }

        composeTestRule.onNodeWithText("City Information").assertExists()
        composeTestRule.onNodeWithText("Title: Miami").assertExists()
        composeTestRule.onNodeWithText(
            "Coordinates: Lat %.2f, Lng %.2f".format(city.lat, city.lon),
            ignoreCase = true
        ).assertExists()
    }

}