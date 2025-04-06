package com.example.financetracker

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class TransactionRepository(private val context: Context) {

    private val SMS_INBOX_URI: Uri = Uri.parse("content://sms/inbox")

    // Reads SMS inbox and filters for bank transaction messages.
    fun getBankTransactions(): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val resolver: ContentResolver = context.contentResolver
        val projection = arrayOf("_id", "address", "body", "date")
        val cursor: Cursor? = resolver.query(SMS_INBOX_URI, projection, null, null, "date DESC")

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow("_id"))
                val address = it.getString(it.getColumnIndexOrThrow("address"))
                val body = it.getString(it.getColumnIndexOrThrow("body"))
                val dateMillis = it.getLong(it.getColumnIndexOrThrow("date"))
                val date = SimpleDateFormat("dd-MM-yy", Locale.getDefault()).format(Date(dateMillis))

                // Filter messages by sender (e.g. “VM-KOTAKB” or any address that contains “KOTAK”)
                if (address.contains("KOTAK", ignoreCase = true)) {
                    val keywords = listOf("sent", "receive", "debited", "credited")
                    if (keywords.any { keyword -> body.contains(keyword, ignoreCase = true) }) {
                        // Determine transaction type based on keyword occurrence
                        val type = when {
                            body.contains("sent", ignoreCase = true) -> "sent"
                            body.contains("receive", ignoreCase = true) -> "received"
                            body.contains("debited", ignoreCase = true) -> "debited"
                            body.contains("credited", ignoreCase = true) -> "credited"
                            else -> "unknown"
                        }
                        // Extract the amount from the SMS using a simple regex (adjust as needed)
                        val amountRegex = Regex("""Rs\.?[\s]*([\d,]+\.\d{2})""")
                        val amountMatch = amountRegex.find(body)
                        val amount = amountMatch?.groups?.get(1)?.value ?: "N/A"

                        transactions.add(
                            Transaction(
                                id = id,
                                amount = amount,
                                type = type,
                                date = date,
                                details = body
                            )
                        )
                    }
                }
            }
        } ?: Timber.e("Cursor is null. Unable to read SMS messages.")

        return transactions
    }
}
