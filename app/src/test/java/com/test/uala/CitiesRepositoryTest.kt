package com.test.uala


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.test.uala.data.local.room.AppDatabase
import com.test.uala.data.local.room.LocationDao
import com.test.uala.data.local.room.LocationMapper
import com.test.uala.data.local.room.LocationRoom
import com.test.uala.data.models.Coord
import com.test.uala.data.models.Location
import com.test.uala.data.remote.api.ApiService
import com.test.uala.repository.CitiesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import retrofit2.Response


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class CitiesRepositoryTest {

    private lateinit var citiesRepository: CitiesRepository
    private lateinit var db: AppDatabase
    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var locationMapper: LocationMapper

    @Mock
    private lateinit var locationDao: LocationDao

    @Mock
    private lateinit var apiService: ApiService

    @get:Rule
    var instantExecutorRule =
        InstantTaskExecutorRule()

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        locationDao = db.locationDao()
        locationMapper = LocationMapper()

        citiesRepository = CitiesRepository(locationDao)

        MockitoAnnotations.openMocks(this)
        citiesRepository = CitiesRepository(locationDao)
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `test loadAndFetchLocations when local data is empty`() = runBlockingTest {
        `when`(locationDao.checkData()).thenReturn(emptyList())

        val remoteLocations = listOf(
            Location(
                id = 1,
                name = "Test Location",
                country = "Test Country",
                coord = Coord(lat = 1.12, lon = 2.12)
            )
        )
        `when`(apiService.getMapList()).thenReturn(Response.success(remoteLocations))

        doNothing().`when`(locationDao)
            .insertLocation(remoteLocations.map { locationMapper.ParseToRoom(it) })

        verify(locationDao).checkData()
        verify(apiService).getMapList()
        verify(locationDao).insertLocation(remoteLocations.map { locationMapper.ParseToRoom(it) })
    }

    @Test
    fun `test getFavoriteLocations returns mapped locations`() = runBlockingTest {
        val locationRooms = listOf(
            LocationRoom(
                id = 1,
                name = "Location 1",
                country = "Country 1",
                lon = 1.0,
                lat = 1.0,
                isFav = true
            )
        )
        `when`(locationDao.getFavoriteLocationsFlow()).thenReturn(flowOf(locationRooms))

        val favoriteLocations = citiesRepository.getFavoriteLocations()

        favoriteLocations.collect { locations ->
            assertEquals(1, locations.size)
            assertEquals("Location 1", locations[0].name)
            assertEquals("Country 1", locations[0].country)
        }
    }

    @Test
    fun `test updateFavoriteStatus updates correctly`() = runBlocking {
        val id = 1
        val isFavorite = true

        val location = LocationRoom(
            id = id,
            country = "Test",
            name = "Test Location",
            lon = 0.0,
            lat = 0.0,
            isFav = !isFavorite
        )

        locationDao.insertLocation(listOf(location))

        citiesRepository.updateFavoriteStatus(id, isFavorite)

        val updatedLocation = locationDao.getLocationById(id)

        updatedLocation?.let {
            if (isFavorite) {
                assertTrue("isFav should be true", it.isFav)
            } else {
                assertFalse("isFav should be false", it.isFav)
            }
        } ?: throw AssertionError("Location with id $id was not found.")
    }

    @Test
    fun `test getLocationsPaginated returns paginated locations`() {
        val locationRooms = listOf(
            LocationRoom(
                id = 1,
                name = "Location 1",
                country = "Country 1",
                lon = 1.0,
                lat = 1.0,
                isFav = false,
            ),
            LocationRoom(
                id = 2,
                name = "Location 2",
                country = "Country 2",
                lon = 1.0,
                lat = 1.0,
                isFav = false,
            ),
            LocationRoom(
                id = 3,
                name = "Location 3",
                country = "Country 3",
                lon = 1.0,
                lat = 1.0,
                isFav = false,
            )

        )
        `when`(locationDao.getLocationsPaginated(10, 0)).thenReturn(locationRooms)

        val paginatedLocations = citiesRepository.getLocationsPaginated(10, 0)

        assertEquals(1, paginatedLocations.size)
        assertEquals("Location 1", paginatedLocations[0].name)
    }

    @Test
    fun `test searchLocations finds matching locations`() = runBlocking {
        val locationRooms = listOf(
            LocationRoom(
                id = 1,
                name = "Test Location 1",
                country = "Country 1",
                lon = 1.0,
                lat = 1.0,
                isFav = false
            ),
            LocationRoom(
                id = 2,
                name = "Test Location 2",
                country = "Country 2",
                lon = 1.0,
                lat = 1.0,
                isFav = false
            )
        )
        locationDao.insertLocation(locationRooms)

        val searchQuery = "%Location%"
        val results = locationDao.searchLocations(searchQuery)

        assertEquals(2, results.size)
        assertTrue(results.any { it.name.contains("Location") })
    }

    @Test
    fun `test searchLocations returns empty list when no match found`() = runBlocking {
        val locationRooms = listOf(
            LocationRoom(
                id = 1,
                name = "Test Location 1",
                country = "Country 1",
                lon = 1.0,
                lat = 1.0,
                isFav = false
            ),
            LocationRoom(
                id = 2,
                name = "Test Location 2",
                country = "Country 2",
                lon = 1.0,
                lat = 1.0,
                isFav = false
            )
        )
        locationDao.insertLocation(locationRooms)

        val searchQuery = "%NonExistent%"
        val results = locationDao.searchLocations(searchQuery)

        assertTrue(results.isEmpty())
    }

}
