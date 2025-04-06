package com.example.financetracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import timber.log.Timber

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in smsMessages) {
                val address = sms.displayOriginatingAddress
                val body = sms.messageBody
                Timber.d("Received SMS from: $address, body: $body")
                // Filter SMS from bank sender (example: address contains "KOTAK")
                if (address.contains("KOTAK", ignoreCase = true)) {
                    // Check if the message body contains keywords indicating a transaction
                    val keywords = listOf("sent", "receive", "debited", "credited")
                    if (keywords.any { keyword -> body.contains(keyword, ignoreCase = true) }) {
                        // In a production app you might update a local database or notify a service here.
                        Timber.d("Bank transaction SMS detected: $body")
                    }
                }
            }
        }
    }
}
