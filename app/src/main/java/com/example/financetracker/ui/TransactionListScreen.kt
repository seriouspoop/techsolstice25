package com.example.financetracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financetracker.data.model.Transaction
import com.example.financetracker.data.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    modifier: Modifier = Modifier,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    // Collect state safely with lifecycle awareness
    val transactions by viewModel.transactions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Bank Transactions") })
        }
    ) { paddingValues ->
        if (transactions.isEmpty()) {
            Box(
                modifier = modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No transactions found or processed yet.")
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionItem(transaction = transaction)
                    HorizontalDivider() // Add a divider between items
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) } // For INR ₹

    val amountColor = when (transaction.type) {
        TransactionType.DEBIT -> Color(0xFFD32F2F) // Red for Debit
        TransactionType.CREDIT -> Color(0xFF388E3C) // Green for Credit
        TransactionType.UNKNOWN -> LocalContentColor.current // Default color
    }
    val amountPrefix = when (transaction.type) {
        TransactionType.DEBIT -> "-"
        TransactionType.CREDIT -> "+"
        TransactionType.UNKNOWN -> ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.senderAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalContentColor.current.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Displaying a snippet of the body for context
                Text(
                    text = transaction.body.take(80) + if (transaction.body.length > 80) "..." else "",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2 // Limit body display lines
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormatter.format(Date(transaction.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalContentColor.current.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "$amountPrefix${currencyFormatter.format(transaction.amount)}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = amountColor,
                fontSize = 18.sp // Make amount stand out
            )
        }
    }
}

// SimpleDateFormat and NumberFormat should ideally be managed more efficiently,
// perhaps outside the composable or using composition locals if reused heavily.
@Composable
fun remember(calculation: () -> SimpleDateFormat): SimpleDateFormat {
    return androidx.compose.runtime.remember { calculation() }
}
@Composable
fun remember(calculation: () -> NumberFormat): NumberFormat {
    return androidx.compose.runtime.remember { calculation() }
}