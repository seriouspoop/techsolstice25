package com.example.financetracker

import android.app.Application
import com.example.financetracker.util.Logger // Assuming Logger setup
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber // Using Timber for logging

@HiltAndroidApp
class FinanceTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Logging - Plant Timber tree in Debug builds only
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Logger.i("Timber logging initialized")
        }
        // You could initialize other things here if needed
    }
}