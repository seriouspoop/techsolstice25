package com.example.financetracker

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.financetracker.ui.theme.FinanceTrackerTheme
import timber.log.Timber


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Request runtime permissions for SMS reading and receiving
        requestPermissions()

        val g = Gemma()
        g.initLlm(applicationContext)
        val x = g.inferFromString("Hello how are you?")
        Timber.tag("Gemma").d(x)


        setContent {
            FinanceTrackerTheme {
                Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    TransactionScreen()
                }
            }
        }
    }

    private fun requestPermissions() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Log the permission results
            permissions.entries.forEach {
                Timber.d("Permission ${it.key} granted: ${it.value}")
            }
        }
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS
            )
        )
    }
}
