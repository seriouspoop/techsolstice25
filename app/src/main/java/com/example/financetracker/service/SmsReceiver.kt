package com.example.financetracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import com.example.financetracker.util.Constants
import com.example.financetracker.util.Logger

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null || intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        Logger.i("SMS Received Intent detected.")

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) {
            Logger.w("No SMS messages found in intent.")
            return
        }

        // Filter messages from known bank senders
        val relevantMessages = messages.filter { message ->
            val sender = message?.originatingAddress ?: "Unknown"
            // Check if sender address contains any known identifier (case-insensitive)
            Constants.BANK_SENDER_IDENTIFIERS.any { identifier ->
                sender.contains(identifier, ignoreCase = true)
            }
        }

        if (relevantMessages.isNotEmpty()) {
            Logger.i("${relevantMessages.size} relevant bank SMS received. Starting service.")

            // Prepare data for the service
            // Note: SmsMessage objects are not directly Parcelable across processes easily.
            // It's better to extract necessary info (sender, body, timestamp) here.
            val messageData = relevantMessages.mapNotNull { msg ->
                msg?.let {
                    mapOf(
                        "sender" to it.originatingAddress,
                        "body" to it.messageBody,
                        "timestamp" to it.timestampMillis
                    )
                }
            }

            if (messageData.isNotEmpty()) {
                // Start the Foreground Service to handle processing
                val serviceIntent = Intent(context, SmsProcessingService::class.java).apply {
                    action = Constants.ACTION_PROCESS_INCOMING_SMS
                    // Pass data as ArrayList of HashMaps (Serializable)
                    putExtra(Constants.EXTRA_SMS_MESSAGES, ArrayList(messageData))
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        } else {
            Logger.d("Received SMS not identified as relevant bank transaction.")
        }
    }
}