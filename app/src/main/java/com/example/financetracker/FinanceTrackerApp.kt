package com.example.financetracker

import android.app.Application
import timber.log.Timber


class FinanceTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Timber for logging in debug builds
        if (true) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
