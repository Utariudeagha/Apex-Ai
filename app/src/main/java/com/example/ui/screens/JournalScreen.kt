package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TradeJournalEntry
import com.example.ui.MainViewModel
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun JournalScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.journalEntries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Performance Stats Calculations
    val totalTrades = entries.size
    val winningTrades = entries.count { it.outcome == "WIN" }
    val winRate = if (totalTrades > 0) (winningTrades.toDouble() / totalTrades) * 100.0 else 0.0
    val totalPnl = entries.sumOf { it.pnlDollars }
    val totalNetR = entries.sumOf { it.netRMultiple }
    val avgRating = if (totalTrades > 0) entries.map { it.ruleComplianceRating }.average() else 5.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Journal Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TRADING JOURNAL & DISCIPLINE AUDIT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Persistent execution log & psychological tilt prevention",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Entry", tint = CyanAccent)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Performance Statistics Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PORTFOLIO PERFORMANCE METRICS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatBox("TOTAL TRADES", "$totalTrades", CyanAccent)
                        StatBox("WIN RATE", "${Math.round(winRate)}%", if (winRate >= 50) BullishGreen else WarningAmber)
                        StatBox("NET R", "${String.format("%.1f", totalNetR)}R", if (totalNetR >= 0) BullishGreen else BearishRed)
                        StatBox("NET PNL", "$${String.format("%,.0f", totalPnl)}", if (totalPnl >= 0) BullishGreen else BearishRed)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariantDark, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Average Rule Adherence:", fontSize = 11.sp, color = TextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format("%.1f", avgRating)} / 5.0",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Journal Entries Section
        item {
            Text(
                text = "EXECUTION LOG (${entries.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (entries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No trades logged yet. Tap '+' or 'Paper Trade' from a signal.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(entries) { trade ->
                TradeJournalCard(
                    trade = trade,
                    onDelete = { viewModel.deleteTrade(trade) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Add Trade Modal Dialog
    if (showAddDialog) {
        AddTradeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newTrade ->
                viewModel.logTrade(newTrade)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun StatBox(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Column {
        Text(text = label, fontSize = 8.sp, color = TextMuted)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun TradeJournalCard(
    trade: TradeJournalEntry,
    onDelete: () -> Unit
) {
    val isWin = trade.outcome == "WIN"
    val outcomeColor = when (trade.outcome) {
        "WIN" -> BullishGreen
        "LOSS" -> BearishRed
        "OPEN" -> CyanAccent
        else -> WarningAmber
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = trade.symbol, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(outcomeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = trade.outcome, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = outcomeColor)
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = trade.setupType, fontSize = 10.sp, color = TextSecondary)

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceVariantDark, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Lots: ${trade.lotSize}", fontSize = 10.sp, color = TextPrimary)
                Text("Net R: ${String.format("%.1f", trade.netRMultiple)}R", fontSize = 10.sp, color = CyanAccent)
                Text("PnL: $${trade.pnlDollars.toInt()}", fontSize = 10.sp, color = outcomeColor)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Psychology: ${trade.emotionState} • Rule Adherence: ${trade.ruleComplianceRating}/5★",
                fontSize = 10.sp,
                color = WarningAmber
            )

            if (trade.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = trade.notes,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun AddTradeDialog(
    onDismiss: () -> Unit,
    onConfirm: (TradeJournalEntry) -> Unit
) {
    var symbol by remember { mutableStateOf("EURUSD") }
    var direction by remember { mutableStateOf("LONG") }
    var outcome by remember { mutableStateOf("WIN") }
    var lotSize by remember { mutableStateOf("1.0") }
    var pnl by remember { mutableStateOf("500.0") }
    var netR by remember { mutableStateOf("2.0") }
    var notes by remember { mutableStateOf("Followed strict 8-timeframe confluence rule.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Log Trade to Journal", color = CyanAccent, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it },
                    label = { Text("Instrument Symbol") },
                    colors = TextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
                OutlinedTextField(
                    value = outcome,
                    onValueChange = { outcome = it },
                    label = { Text("Outcome (WIN / LOSS / OPEN)") },
                    colors = TextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
                OutlinedTextField(
                    value = pnl,
                    onValueChange = { pnl = it },
                    label = { Text("Net PnL ($)") },
                    colors = TextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
                OutlinedTextField(
                    value = netR,
                    onValueChange = { netR = it },
                    label = { Text("Net R Multiple") },
                    colors = TextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Journal Notes & Mindset") },
                    colors = TextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entry = TradeJournalEntry(
                        symbol = symbol,
                        direction = direction,
                        setupType = "Apex SMC Trigger",
                        entryPrice = 1.0934,
                        exitPrice = 1.0964,
                        stopLoss = 1.0919,
                        takeProfit = 1.0964,
                        lotSize = lotSize.toDoubleOrNull() ?: 1.0,
                        pnlDollars = pnl.toDoubleOrNull() ?: 0.0,
                        netRMultiple = netR.toDoubleOrNull() ?: 2.0,
                        ruleComplianceRating = 5,
                        emotionState = "Disciplined",
                        notes = notes,
                        timestamp = System.currentTimeMillis(),
                        outcome = outcome,
                        isPaperTrade = true
                    )
                    onConfirm(entry)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Save Entry", color = SurfaceDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
