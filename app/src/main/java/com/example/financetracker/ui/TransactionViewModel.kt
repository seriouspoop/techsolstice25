package com.example.financetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    transactionRepository: TransactionRepository
) : ViewModel() {

    // Expose transactions as StateFlow for Compose UI
    val transactions: StateFlow<List<Transaction>> = transactionRepository.allTransactions
        .stateIn(
            scope = viewModelScope,
            // WhileSubscribed(5000) is recommended over Eagerly to save resources
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList() // Start with an empty list
        )

    // You could add functions here to trigger specific actions in the service if needed,
    // e.g., force a re-scan of past messages.
    // fun triggerPastSmsScan(context: Context) {
    //     val serviceIntent = Intent(context, SmsProcessingService::class.java).apply {
    //         action = Constants.ACTION_PROCESS_PAST_SMS
    //     }
    //     ContextCompat.startForegroundService(context, serviceIntent)
    // }
}