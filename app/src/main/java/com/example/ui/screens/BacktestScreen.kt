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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.example.model.BacktestTrade
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
fun BacktestScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val report by viewModel.backtestReport.collectAsState()
    val isBacktesting by viewModel.isBacktesting.collectAsState()
    val selectedInstrument by viewModel.selectedInstrument.collectAsState()
    val instruments by viewModel.instruments.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Backtesting Header
        item {
            Column {
                Text(
                    text = "QUANTITATIVE BACKTEST TERMINAL",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Vectorized historical validation with spread & slippage friction",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Instrument Selector
        item {
            Text(
                text = "TARGET ASSET FOR SIMULATION",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(instruments.take(12)) { inst ->
                    val isSelected = inst.symbol == selectedInstrument.symbol
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectInstrument(inst) },
                        label = { Text(inst.symbol, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = SurfaceDark,
                            containerColor = SurfaceDark,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Run Backtest Trigger Button
        item {
            Button(
                onClick = { viewModel.runBacktestForSelected() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(8.dp),
                enabled = !isBacktesting
            ) {
                if (isBacktesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = SurfaceDark,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Running Historical Simulation...", fontSize = 12.sp, color = SurfaceDark)
                } else {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = SurfaceDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Run 180-Day Simulation (${selectedInstrument.symbol})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceDark
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Backtest Results Dashboard
        if (report != null) {
            val r = report!!
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
                            Text(
                                text = r.strategyName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                            Text(
                                text = r.testPeriod,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Metric Grid 1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ReportMetric("WIN RATE", "${r.winRatePercent}%", if (r.winRatePercent >= 50) BullishGreen else WarningAmber)
                            ReportMetric("PROFIT FACTOR", "${r.profitFactor}", if (r.profitFactor >= 1.5) BullishGreen else WarningAmber)
                            ReportMetric("AVG R", "${r.averageRMultiple}R", BullishGreen)
                            ReportMetric("NET RETURN", "+${r.netPnlPercent}%", BullishGreen)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Metric Grid 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ReportMetric("MAX DRAWDOWN", "${r.maxDrawdownPercent}%", BearishRed)
                            ReportMetric("SHARPE RATIO", "${r.sharpeRatio}", CyanAccent)
                            ReportMetric("WIN/LOSS", "${r.winningTrades}/${r.losingTrades}", TextPrimary)
                            ReportMetric("MAX STREAK", "${r.consecutiveWinsMax}W / ${r.consecutiveLossesMax}L", TextMuted)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Modeled Assumptions Note
                        Text(
                            text = "Frictions Modeled: ${r.modeledSpreadPips} pips spread + ${r.modeledSlippagePips} pips slippage + \$7/lot round commission. Zero lookahead bias.",
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Historical Trade List
            item {
                Text(
                    text = "HISTORICAL TRADES SAMPLE (${r.tradeList.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(r.tradeList) { trade ->
                BacktestTradeCard(trade)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun ReportMetric(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column {
        Text(text = label, fontSize = 8.sp, color = TextMuted)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun BacktestTradeCard(trade: BacktestTrade) {
    val isWin = trade.outcome == "WIN"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = trade.id, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = trade.direction.name, fontSize = 10.sp, color = if (trade.direction.name == "LONG") BullishGreen else BearishRed)
                }
                Text(text = trade.setupReason, fontSize = 9.sp, color = TextMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isWin) "+${trade.rMultiple}R" else "-1.0R",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isWin) BullishGreen else BearishRed
                )
                Text(
                    text = trade.outcome,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isWin) BullishGreen else BearishRed
                )
            }
        }
    }
}
