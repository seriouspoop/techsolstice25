package com.example.financetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.data.model.TransactionType // Ensure enum is accessible

// Define TypeConverter if needed (e.g., for TransactionType Enum)
class Converters {
    @androidx.room.TypeConverter
    fun fromTransactionType(value: TransactionType?): String? {
        return value?.name
    }

    @androidx.room.TypeConverter
    fun toTransactionType(value: String?): TransactionType? {
        return value?.let { TransactionType.valueOf(it) } ?: TransactionType.UNKNOWN
    }
}


@Database(entities = [Transaction::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // Add this line
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val DATABASE_NAME = "finance_tracker_db"
    }
}