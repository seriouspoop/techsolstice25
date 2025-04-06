package com.example.financetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository(application.applicationContext)

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    init {
        fetchTransactions()
    }

    // Fetch transactions from the SMS inbox and update the UI state.
    fun fetchTransactions() {
        viewModelScope.launch {
            Timber.d("Fetching transactions from SMS inbox")
            val transactionsList = repository.getBankTransactions()
            _transactions.value = transactionsList
            Timber.d("Fetched ${transactionsList.size} transactions")
        }
    }
}
