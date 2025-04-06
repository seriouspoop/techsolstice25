package com.example.financetracker.util

import timber.log.Timber // Using Timber implementation

// Simple wrapper or direct Timber usage
object Logger {
    // Initialize Timber in your Application class (FinanceTrackerApp)

    fun v(message: String, vararg args: Any?) = Timber.v(message, *args)
    fun d(message: String, vararg args: Any?) = Timber.d(message, *args)
    fun i(message: String, vararg args: Any?) = Timber.i(message, *args)
    fun w(message: String, vararg args: Any?) = Timber.w(message, *args)
    fun e(message: String, throwable: Throwable? = null, vararg args: Any?) = Timber.e(throwable, message, *args)
    fun wtf(message: String, vararg args: Any?) = Timber.wtf(message, *args) // What a Terrible Failure
}