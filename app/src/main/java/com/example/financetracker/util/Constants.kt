package com.example.financetracker.util

object Constants {
    const val SMS_PERMISSION_REQUEST_CODE = 101
    const val NOTIFICATION_PERMISSION_REQUEST_CODE = 102

    const val FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID = "FinanceTrackerServiceChannel"
    const val FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_NAME = "Finance Tracker Service"
    const val FOREGROUND_SERVICE_NOTIFICATION_ID = 1

    const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
    const val ACTION_PROCESS_INCOMING_SMS = "ACTION_PROCESS_INCOMING_SMS"
    const val ACTION_PROCESS_PAST_SMS = "ACTION_PROCESS_PAST_SMS" // Action to trigger initial sync

    const val EXTRA_SMS_MESSAGES = "EXTRA_SMS_MESSAGES"

    // --- IMPORTANT ---
    // Define known bank sender IDs (add more as needed). These vary greatly!
    // This is crucial for filtering.
    val BANK_SENDER_IDENTIFIERS = setOf(
        "KOTAKB", // Kotak Bank (e.g., VM-KOTAKB, BP-KOTAKB) - Check actual sender IDs on your phone
        "HDFCBK", // HDFC Bank
        "ICICIB", // ICICI Bank
        "SBIBNK", // SBI Bank
        "AXISBK", // Axis Bank
        "IDFCFB", // IDFC First Bank
        // Add generic keywords if sender ID is just a number but contains bank name
        "ALERT",  // Some banks use generic words with bank names in body
        "UPDATE",
        // It's better to use the specific Header ID (like VM-KOTAKB) if available
    )

    // Keywords to identify transaction messages (case-insensitive)
    val TRANSACTION_KEYWORDS = setOf(
        "sent", "debited", "spent", "paid",
        "credited", "received", "added",
        "txn", "transaction", "payment",
        "a/c", "account",
        "rs.", "inr", // Currency symbols/codes
        "upi ref"
    )
    // Keywords indicating debit/outgoing
    val DEBIT_KEYWORDS = setOf("sent", "debited", "spent", "paid", "purchase")
    // Keywords indicating credit/incoming
    val CREDIT_KEYWORDS = setOf("credited", "received", "added", "refund")
}