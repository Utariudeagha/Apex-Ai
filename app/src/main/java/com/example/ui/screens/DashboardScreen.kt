package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MarketDataEngine
import com.example.model.ApexSignal
import com.example.model.Instrument
import com.example.model.SignalDirection
import com.example.model.SignalState
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.DataFreshnessBadge
import com.example.ui.components.SessionClockBar
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
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val instruments by viewModel.instruments.collectAsState()
    val signals by viewModel.signals.collectAsState()
    val consecutiveLosses by viewModel.consecutiveLosses.collectAsState()

    val confirmedSignals = signals.filter { it.state == SignalState.CONFIRMED }
    val potentialSignals = signals.filter { it.state == SignalState.POTENTIAL || it.state == SignalState.WAITING_FOR_CONFIRMATION }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Institutional Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "APEX SNIPER AI",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Multi-Asset Intelligence • Risk-First Execution",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                DataFreshnessBadge(isRealTime = true, latencyMs = 38)
            }

            Spacer(modifier = Modifier.height(10.dp))
            SessionClockBar()
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Consecutive Loss / Cooldown Alert if active
        if (consecutiveLosses >= 2) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = BearishRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = BearishRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "COOLDOWN SAFEGUARD ACTIVE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BearishRed
                            )
                            Text(
                                text = "2 consecutive losses logged. Risk lock engages a mandatory pause to eliminate tilt & revenge trading.",
                                fontSize = 10.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Core Mission Rule Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CORE RULE: NO VALID SETUP = NO SIGNAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            letterSpacing = 0.8.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyanAccent.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${confirmedSignals.size} Qualified",
                                fontSize = 10.sp,
                                color = CyanAccent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Apex Sniper AI evaluates 6 scoring categories and enforces 8 mandatory hard filters. If a setup fails net R, spread, news risk or structural invalidation, it is strictly rejected.",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        lineHeight = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Live Market Marquee (Horizontal scroll of quotes)
        item {
            Text(
                text = "INSTITUTIONAL MARKET FEED (L2)",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(instruments.take(10)) { inst ->
                    MarketQuoteCard(
                        instrument = inst,
                        onClick = {
                            viewModel.selectInstrument(inst)
                            viewModel.navigateTo(AppScreen.CHART)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Command Navigation Grid
        item {
            Text(
                text = "TERMINAL MODULES",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickNavButton(
                    label = "Scanner",
                    icon = Icons.Default.Search,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.SCANNER) }
                )
                QuickNavButton(
                    label = "Charts",
                    icon = Icons.Default.ShowChart,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.CHART) }
                )
                QuickNavButton(
                    label = "Risk Engine",
                    icon = Icons.Default.Calculate,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.RISK_CALC) }
                )
                QuickNavButton(
                    label = "Academy",
                    icon = Icons.Default.MenuBook,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.ACADEMY) }
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Confirmed Signals Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CONFIRMED HIGH-PROBABILITY SETUPS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyanAccent,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${confirmedSignals.size} Live",
                    fontSize = 11.sp,
                    color = BullishGreen
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (confirmedSignals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceDark)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "NO QUALIFYING SETUPS DETECTED",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Current market conditions do not meet 85-point threshold and hard filters. Capital preservation mode.",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                confirmedSignals.forEach { sig ->
                    SignalCard(
                        signal = sig,
                        onInspect = { viewModel.selectSignal(sig) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Developing / Potential Setups Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DEVELOPING / PENDING CONFIRMATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${potentialSignals.size} Monitored",
                    fontSize = 11.sp,
                    color = WarningAmber
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            potentialSignals.take(4).forEach { sig ->
                SignalCard(
                    signal = sig,
                    onInspect = { viewModel.selectSignal(sig) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MarketQuoteCard(
    instrument: Instrument,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(135.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = instrument.symbol,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${if (instrument.changePercent24h >= 0) "+" else ""}${instrument.changePercent24h}%",
                    fontSize = 10.sp,
                    color = if (instrument.changePercent24h >= 0) BullishGreen else BearishRed
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = instrument.formattedPrice,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = CyanAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Sprd: ${instrument.formattedSpread}",
                fontSize = 9.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun QuickNavButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = CyanAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SignalCard(
    signal: ApexSignal,
    onInspect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLong = signal.direction == SignalDirection.LONG
    val stateColor = when (signal.state) {
        SignalState.CONFIRMED, SignalState.ACTIVE -> BullishGreen
        SignalState.WAITING_FOR_CONFIRMATION, SignalState.POTENTIAL -> WarningAmber
        else -> BearishRed
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onInspect),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Symbol, Direction, Setup Type, State
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = signal.symbol,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isLong) BullishGreen.copy(alpha = 0.15f) else BearishRed.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isLong) "LONG / BUY" else "SHORT / SELL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLong) BullishGreen else BearishRed
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(stateColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = signal.state.label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = stateColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = signal.setupType.title,
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metrics Row: Score, Net R, Entry, Stop, TP1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceVariantDark, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("SCORE", fontSize = 8.sp, color = TextMuted)
                    Text("${signal.scoreBreakdown.totalScore}/100", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                }
                Column {
                    Text("NET R", fontSize = 8.sp, color = TextMuted)
                    Text("${String.format("%.2f", signal.netRewardToRisk)}R", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BullishGreen)
                }
                Column {
                    Text("ENTRY", fontSize = 8.sp, color = TextMuted)
                    Text(formatPrice(signal.entryPrice), fontSize = 11.sp, color = TextPrimary)
                }
                Column {
                    Text("STOP LOSS", fontSize = 8.sp, color = TextMuted)
                    Text(formatPrice(signal.stopLoss), fontSize = 11.sp, color = BearishRed)
                }
                Column {
                    Text("TP1", fontSize = 8.sp, color = TextMuted)
                    Text(formatPrice(signal.takeProfit1), fontSize = 11.sp, color = BullishGreen)
                }
            }
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
