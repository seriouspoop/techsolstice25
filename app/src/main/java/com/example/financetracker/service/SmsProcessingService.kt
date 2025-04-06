package com.example.financetracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.financetracker.MainActivity // Your main activity
import com.example.financetracker.R // For notification icon
import com.example.financetracker.data.repository.TransactionRepository
import com.example.financetracker.util.Constants
import com.example.financetracker.util.Logger
import com.example.financetracker.util.SmsParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SmsProcessingService : LifecycleService() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var smsParser: SmsParser

    private var isServiceRunning = false
    private var processingJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Logger.i("SmsProcessingService: onCreate")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId) // Important for LifecycleService
        Logger.i("SmsProcessingService: onStartCommand - Action: ${intent?.action}")

        val notification = createNotification()

        // Promote to foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                Constants.FOREGROUND_SERVICE_NOTIFICATION_ID,
                notification,
                // Specify foreground service type for Android 14+ targeting API 34+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                } else {
                    0 // Type is mandatory for Q+ but only restricted further on U+
                }
            )
        } else {
            startForeground(Constants.FOREGROUND_SERVICE_NOTIFICATION_ID, notification)
        }

        isServiceRunning = true

        // Handle different actions
        when (intent?.action) {
            Constants.ACTION_START_SERVICE -> {
                Logger.i("Service started explicitly.")
                // Optionally trigger past SMS processing if not done yet
                // processPastSmsIfNotDone() // Example call
            }
            Constants.ACTION_PROCESS_INCOMING_SMS -> {
                Logger.i("Processing incoming SMS intent.")
                val messagesData = intent.getSerializableExtra(Constants.EXTRA_SMS_MESSAGES) as? ArrayList<Map<String, Any>>
                messagesData?.let {
                    processSmsList(it)
                } ?: Logger.w("No message data found in incoming SMS intent.")
            }
            Constants.ACTION_PROCESS_PAST_SMS -> {
                Logger.i("Processing past SMS intent.")
                processPastSms() // Trigger bulk processing
            }
            else -> {
                Logger.w("Unknown or null action received: ${intent?.action}")
                // Decide if you want to stop the service if started without specific action
                // stopSelf() // Example: stop if no valid action
            }
        }

        // If the service is killed, restart it with the last intent
        return START_STICKY
    }

    private fun processSmsList(messagesData: List<Map<String, Any>>) {
        if (processingJob?.isActive == true) {
            Logger.d("Processing job already active, skipping new SMS list processing for now.")
            // Optionally queue the work or handle concurrent processing carefully
            return
        }
        processingJob = lifecycleScope.launch(Dispatchers.IO) { // Use IO dispatcher for DB/parsing
            Logger.i("Starting processing of ${messagesData.size} messages.")
            var processedCount = 0
            messagesData.forEach { data ->
                try {
                    val sender = data["sender"] as? String
                    val body = data["body"] as? String
                    val timestamp = data["timestamp"] as? Long

                    if (sender != null && body != null && timestamp != null) {
                        val transaction = smsParser.parseSms(sender, body, timestamp)
                        if (transaction != null) {
                            if (transactionRepository.saveTransaction(transaction)) {
                                processedCount++
                            }
                        }
                    } else {
                        Logger.w("Invalid data format in message map: $data")
                    }
                } catch (e: Exception) {
                    Logger.e("Error processing single SMS data: $data", e)
                }
            }
            Logger.i("Finished processing batch. Saved $processedCount new transactions.")
        }
        processingJob?.invokeOnCompletion {
            Logger.d("SMS List processing job completed.")
            processingJob = null // Reset job reference
        }
    }


    private fun processPastSms() {
        if (processingJob?.isActive == true) {
            Logger.d("Processing job already active, skipping past SMS processing.")
            return
        }
        processingJob = lifecycleScope.launch(Dispatchers.IO) {
            Logger.i("Starting processing of past SMS messages...")
            val contentResolver = applicationContext.contentResolver
            val uri: Uri = Telephony.Sms.CONTENT_URI // Use Inbox, Sent, or All
            val cursor: Cursor? = contentResolver.query(
                uri,
                arrayOf(
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                null, // Add selection criteria if needed (e.g., only specific senders, after a certain date)
                null,
                Telephony.Sms.DEFAULT_SORT_ORDER // Or sort by Date DESC
            )

            var count = 0
            var savedCount = 0
            cursor?.use { // Ensure cursor is closed
                if (it.moveToFirst()) {
                    val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                    val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                    val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                    do {
                        val sender = it.getString(addressIndex)
                        val body = it.getString(bodyIndex)
                        val timestamp = it.getLong(dateIndex)

                        // Filter by bank sender ID here as well
                        if (sender != null && Constants.BANK_SENDER_IDENTIFIERS.any { id -> sender.contains(id, ignoreCase = true) }) {
                            try {
                                val transaction = smsParser.parseSms(sender, body, timestamp)
                                if (transaction != null) {
                                    if (transactionRepository.saveTransaction(transaction)) {
                                        savedCount++
                                    }
                                }
                            } catch (e: Exception) {
                                Logger.e("Error parsing/saving past SMS from $sender", e)
                            }
                        }
                        count++
                        // Optional: Add a small delay or check for cancellation if processing thousands
                        // if (count % 100 == 0) kotlinx.coroutines.delay(50)
                    } while (it.moveToNext())
                }
            }
            Logger.i("Finished processing past SMS. Checked $count messages, saved $savedCount new transactions.")
        }
        processingJob?.invokeOnCompletion {
            Logger.d("Past SMS processing job completed.")
            processingJob = null // Reset job reference
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID,
                Constants.FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Low importance to be less intrusive
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            Logger.i("Notification channel created.")
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use a proper icon
        val notificationIcon = R.drawable.ic_launcher_foreground // Replace with your notification icon

        return NotificationCompat.Builder(this, Constants.FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Finance Tracker Active")
            .setContentText("Monitoring bank SMS messages...")
            .setSmallIcon(notificationIcon)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Makes the notification non-dismissible
            .setSilent(true) // Reduce noise if notification updates frequently
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE) // Show immediately
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        processingJob?.cancel() // Cancel any ongoing work
        Logger.i("SmsProcessingService: onDestroy")
        // Consider removing notification if service is intentionally stopped
        // val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // manager.cancel(Constants.FOREGROUND_SERVICE_NOTIFICATION_ID)
    }

    // Binder is null for non-binding services
    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent) // Important for LifecycleService
        return null
    }
}