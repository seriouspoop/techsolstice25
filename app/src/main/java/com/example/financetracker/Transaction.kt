package com.example.financetracker

data class Transaction(
    val id: String,
    val amount: String,
    val type: String, // e.g., "sent", "received", "debited", "credited"
    val date: String,
    val details: String
)
