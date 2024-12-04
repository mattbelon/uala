package com.test.uala.di

import android.app.Application
import androidx.room.Room
import com.test.uala.data.local.room.AppDatabase
import com.test.uala.data.local.room.LocationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideLocationDao(database: AppDatabase): LocationDao {
        return database.locationDao()
    }
}
