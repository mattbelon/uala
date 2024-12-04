package com.test.uala

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class UalaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
