package com.example.financetracker.util

import com.example.financetracker.data.model.Transaction
import com.example.financetracker.data.model.TransactionType
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsParser @Inject constructor() {

    // --- VERY BASIC Regex Patterns - NEEDS SIGNIFICANT IMPROVEMENT ---
    // Regex to find amounts like Rs. XXX.XX, Rs XXX, INR XXX.XX etc.
    private val amountPattern = Pattern.compile(
        """(?:(?:Rs|INR)\.?\s?)(\d{1,3}(?:,\d{3})*(?:\.\d{1,2})?|\d+(?:\.\d{1,2})?)""",
//        """Rs\.\s*(\d+\.\d+)""",
        Pattern.CASE_INSENSITIVE
    )

    // Regex for UPI reference numbers
    private val upiRefPattern = Pattern.compile(
        """UPI Ref(?: No)?[:\s]*(\d+)""",
        Pattern.CASE_INSENSITIVE
    )

    // Regex for dates like dd-mm-yy, dd/mm/yyyy, dd MMM yy etc. (Needs refinement)
    private val datePattern = Pattern.compile(
        """(\d{1,2}[-/]\d{1,2}[-/]\d{2,4}|\d{1,2}\s(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s\d{2,4})""",
        Pattern.CASE_INSENSITIVE
    )

    // Regex to find potential recipient/sender info (like email-style UPI IDs or account numbers)
    // This is highly variable and difficult to generalize reliably.
    private val recipientPattern = Pattern.compile(
        """(?:to|from)\s+([a-zA-Z0-9._-]+@[a-zA-Z]+|VPA\s+[a-zA-Z0-9._-]+@\w+|A/c\s+(?:X+|ending\s+in)\s*\d+|[a-zA-Z\s]+)""",
        Pattern.CASE_INSENSITIVE
    )


    /**
     * Parses an SMS message body to extract transaction details.
     * Returns a Transaction object or null if parsing fails or it's not a transaction.
     */
    fun parseSms(sender: String, body: String, timestamp: Long): Transaction? {
        Logger.v("Parsing SMS from $sender: \"$body\"")

        // 1. Basic Keyword Check - Is it likely a transaction message?
        if (!Constants.TRANSACTION_KEYWORDS.any { body.contains(it, ignoreCase = true) }) {
            Logger.v("SMS does not contain transaction keywords. Skipping.")
            return null
        }

        // 2. Extract Amount
        val amountMatcher = amountPattern.matcher(body)
        val amount = if (amountMatcher.find()) {
            amountMatcher.group(1)?.replace(",", "")?.toDoubleOrNull()
        } else {
            null
        }

        if (amount == null || amount <= 0) {
            Logger.v("Could not parse a valid amount. Skipping.")
            return null // If no amount found, likely not a standard transaction SMS
        }

        // 3. Determine Transaction Type (Debit/Credit)
        val lowerCaseBody = body.toLowerCase()
        val transactionType = when {
            Constants.DEBIT_KEYWORDS.any { lowerCaseBody.contains(it) } -> TransactionType.DEBIT
            Constants.CREDIT_KEYWORDS.any { lowerCaseBody.contains(it) } -> TransactionType.CREDIT
            else -> TransactionType.UNKNOWN // Could still be a transaction (e.g., balance inquiry)
        }

        // If type is Unknown, maybe we don't want to store it? Depends on requirements.
        // Let's allow UNKNOWN for now, but it might indicate parsing needs improvement.
        // if (transactionType == TransactionType.UNKNOWN) {
        //    Logger.v("Could not determine transaction type (Debit/Credit). Skipping.")
        //    return null
        // }


        // 4. Extract UPI Reference (Optional)
        val upiMatcher = upiRefPattern.matcher(body)
        val upiRef = if (upiMatcher.find()) upiMatcher.group(1) else null

        // 5. Extract Date (Optional) - SMS timestamp is usually more reliable
        val dateMatcher = datePattern.matcher(body)
        val parsedDate = if (dateMatcher.find()) dateMatcher.group(1) else null

        // 6. Extract Recipient/Source (Highly Experimental)
        val recipientMatcher = recipientPattern.matcher(body)
        val recipientOrSource = if (recipientMatcher.find()) recipientMatcher.group(1)?.trim() else null


        Logger.i("Parsed Transaction: Amount=$amount, Type=$transactionType, Ref=$upiRef, ParsedDate=$parsedDate, Recipient/Source=$recipientOrSource")

        return Transaction(
            senderAddress = sender,
            body = body,
            amount = amount,
            type = transactionType,
            timestamp = timestamp,
            parsedDate = parsedDate,
            reference = upiRef,
            recipientOrSource = recipientOrSource
        )
    }
}