package com.example.financetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    DEBIT, CREDIT, UNKNOWN
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderAddress: String, // e.g., "VM-KOTAKB"
    val body: String,          // Original SMS body
    val amount: Double,
    val type: TransactionType,
    val timestamp: Long,       // SMS timestamp
    val parsedDate: String?,   // Date extracted from SMS body if available
    val reference: String?,    // UPI Ref or similar
    val recipientOrSource: String?, // e.g., "jargoldonline@ybl" or source account
)