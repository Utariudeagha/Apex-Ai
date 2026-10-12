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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SignalDirection
import com.example.model.SignalState
import com.example.model.TradeJournalEntry
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.ExplanationPanel
import com.example.ui.components.ScoreBreakdownCard
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
fun SignalDetailsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSignal by viewModel.selectedSignal.collectAsState()

    if (selectedSignal == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Text("No signal selected for detailed inspection.", color = TextMuted)
        }
        return
    }

    val sig = selectedSignal!!
    val isLong = sig.direction == SignalDirection.LONG

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Back Navigation Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.SCANNER) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = CyanAccent
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "${sig.symbol} SETUP AUDIT",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "ID: ${sig.id} • Strategy: ${sig.strategyId}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Setup Highlights Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isLong) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isLong) "LONG / BUY" else "SHORT / SELL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLong) BullishGreen else BearishRed
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = sig.state.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sig.state == SignalState.CONFIRMED) BullishGreen else WarningAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = sig.setupType.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Execution Target Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariantDark, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ENTRY PRICE", fontSize = 9.sp, color = TextMuted)
                            Text(formatPrice(sig.entryPrice), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }
                        Column {
                            Text("STOP LOSS", fontSize = 9.sp, color = TextMuted)
                            Text(formatPrice(sig.stopLoss), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BearishRed)
                        }
                        Column {
                            Text("NET REWARD:RISK", fontSize = 9.sp, color = TextMuted)
                            Text("${String.format("%.2f", sig.netRewardToRisk)}R", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BullishGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Targets breakdown
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TP1: ${formatPrice(sig.takeProfit1)} (2.2R)", fontSize = 10.sp, color = BullishGreen)
                        Text("TP2: ${formatPrice(sig.takeProfit2)} (3.5R)", fontSize = 10.sp, color = BullishGreen)
                        Text("TP3: ${formatPrice(sig.takeProfit3)} (5.0R)", fontSize = 10.sp, color = BullishGreen)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Structural Invalidation Note
                    Text(
                        text = "Invalidation Level: ${formatPrice(sig.invalidationPrice)} (Order cancelled if breached before fill)",
                        fontSize = 10.sp,
                        color = WarningAmber
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Action Buttons: Sizing Calculator & Paper Log
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.RISK_CALC) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = SurfaceDark, modifier = Modifier.padding(end = 4.dp))
                    Text("Position Sizer", fontSize = 11.sp, color = SurfaceDark, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val entry = TradeJournalEntry(
                            symbol = sig.symbol,
                            direction = sig.direction.name,
                            setupType = sig.setupType.title,
                            entryPrice = sig.entryPrice,
                            exitPrice = sig.takeProfit1,
                            stopLoss = sig.stopLoss,
                            takeProfit = sig.takeProfit1,
                            lotSize = sig.recommendedLots,
                            pnlDollars = 1100.0,
                            netRMultiple = sig.netRewardToRisk,
                            ruleComplianceRating = 5,
                            emotionState = "Disciplined & Patient",
                            notes = "Paper executed on confirmed ${sig.setupType.code} trigger. All 8 hard filters passed.",
                            timestamp = System.currentTimeMillis(),
                            outcome = "OPEN",
                            isPaperTrade = true
                        )
                        viewModel.logTrade(entry)
                        viewModel.navigateTo(AppScreen.JOURNAL)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, tint = CyanAccent, modifier = Modifier.padding(end = 4.dp))
                    Text("Paper Trade", fontSize = 11.sp, color = TextPrimary)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 100-Point Scoring Audit & Hard Filters Checklist
        item {
            ScoreBreakdownCard(signal = sig)
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 12-Point Institutional Explanation Panel (A - L)
        item {
            ExplanationPanel(explanation = sig.explanation)
        }
    }
}

private fun formatPrice(price: Double): String {
    return when {
        price >= 1000.0 -> String.format("%,.2f", price)
        price >= 100.0 -> String.format("%.3f", price)
        else -> String.format("%.5f", price)
    }
}
