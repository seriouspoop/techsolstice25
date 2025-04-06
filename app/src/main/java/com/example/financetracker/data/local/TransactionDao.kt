package com.example.financetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financetracker.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE) // Ignore if transaction already exists (based on potentially a unique constraint later)
    suspend fun insertTransaction(transaction: Transaction): Long // Returns row ID or -1 if ignored

    // Check if a message with the same timestamp and sender already exists (basic duplication check)
    @Query("SELECT COUNT(*) FROM transactions WHERE timestamp = :timestamp AND senderAddress = :senderAddress AND body = :body")
    suspend fun transactionExists(timestamp: Long, senderAddress: String, body: String): Int

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // Optional: Query to get the timestamp of the latest processed message from a specific sender
    @Query("SELECT MAX(timestamp) FROM transactions WHERE senderAddress = :senderAddress")
    suspend fun getLatestTimestampForSender(senderAddress: String): Long?

    // Optional: Query to get the timestamp of the oldest message processed
    @Query("SELECT MIN(timestamp) FROM transactions")
    suspend fun getOldestTimestamp(): Long?
}