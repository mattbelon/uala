package com.test.uala

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.test.uala.data.local.room.AppDatabase
import com.test.uala.data.local.room.LocationDao
import com.test.uala.data.local.room.LocationRoom
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test


class CityDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var cityDao: LocationDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        cityDao = db.locationDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `debería devolver las ciudades ordenadas alfabéticamente`() {
        val cities = listOf(
            LocationRoom(id = 1, name = "Zaragoza", country = "Es", lat = 24.16, lon = 44.65),
            LocationRoom(id = 2, name = "Barcelona", country = "Es", lat = 24.16, lon = 44.65),
            LocationRoom(id = 3, name = "Madrid", country = "Es", lat = 24.16, lon = 44.65)
        )

        runBlocking {
            cityDao.insertLocation(cities)
            val result = cityDao.getAllLocations()
            
            val expected = listOf("Barcelona", "Madrid", "Zaragoza")
            assertEquals(expected, result.map { it.name })
        }
    }

    @Test
    fun `debería devolver una lista vacía si no hay ciudades en la base de datos`() {
        runBlocking {
            val result = cityDao.getAllLocations()
            assertTrue(result.isEmpty())
        }
    }
}
