package com.example.financetracker.di

import android.content.Context
import androidx.room.Room
import com.example.financetracker.data.local.AppDatabase
import com.example.financetracker.data.local.TransactionDao
import com.example.financetracker.data.repository.TransactionRepository
import com.example.financetracker.util.SmsParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            // .addMigrations(...) // Add migrations if you change the schema
            .fallbackToDestructiveMigration() // Use only during development! Replace with proper migrations for production.
            .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(appDatabase: AppDatabase): TransactionDao {
        return appDatabase.transactionDao()
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(transactionDao: TransactionDao): TransactionRepository {
        return TransactionRepository(transactionDao)
    }

    @Provides
    @Singleton
    fun provideSmsParser(): SmsParser {
        // You could potentially inject bank keyword lists or regex patterns here
        // For now, just create a standard instance.
        return SmsParser()
    }

    // Provides application context wherever needed (Hilt provides this automatically too)
    // @Provides
    // @Singleton
    // fun provideContext(@ApplicationContext appContext: Context): Context {
    //     return appContext
    // }
}