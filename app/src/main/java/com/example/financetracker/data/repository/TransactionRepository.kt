package com.example.financetracker.data.repository

import com.example.financetracker.data.local.TransactionDao
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun saveTransaction(transaction: Transaction): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Basic check to prevent duplicates based on timestamp, sender, and body hash/content
                val exists = transactionDao.transactionExists(
                    transaction.timestamp,
                    transaction.senderAddress,
                    transaction.body // Using full body might be slow, consider hashing or unique ID if available
                ) > 0

                if (!exists) {
                    val rowId = transactionDao.insertTransaction(transaction)
                    Logger.d("Transaction inserted with rowId: $rowId. Amount: ${transaction.amount}, Type: ${transaction.type}")
                    rowId > 0 // Return true if insertion was successful
                } else {
                    Logger.d("Transaction already exists, skipping insertion. Timestamp: ${transaction.timestamp}")
                    false // Indicate it was skipped
                }
            } catch (e: Exception) {
                Logger.e("Error saving transaction", e)
                false // Indicate failure
            }
        }
    }

    suspend fun getLatestTimestampForSender(senderAddress: String): Long? {
        return withContext(Dispatchers.IO) {
            transactionDao.getLatestTimestampForSender(senderAddress)
        }
    }

    suspend fun getOldestTimestamp(): Long? {
        return withContext(Dispatchers.IO) {
            transactionDao.getOldestTimestamp()
        }
    }
}