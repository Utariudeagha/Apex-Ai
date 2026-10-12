package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.PriceActionEngine
import com.example.model.SignalDirection
import com.example.model.Timeframe
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.CandlestickChart
import com.example.ui.components.DataFreshnessBadge
import com.example.ui.components.TimeframeConfluenceMatrix
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BorderDark
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun ChartScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedInstrument by viewModel.selectedInstrument.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val candles by viewModel.chartCandles.collectAsState()
    val instruments by viewModel.instruments.collectAsState()
    val multiTfAnalyses by viewModel.multiTfAnalyses.collectAsState()
    val signals by viewModel.signals.collectAsState()

    val currentSignal = signals.find { it.symbol == selectedInstrument.symbol }

    // Derive SMC features for the active candles
    val atrs = remember(candles) { PriceActionEngine.calculateATR(candles) }
    val swings = remember(candles) { PriceActionEngine.detectSwingPoints(candles) }
    val breaks = remember(candles, swings) { PriceActionEngine.detectStructuralBreaks(candles, swings) }
    val fvgs = remember(candles) { PriceActionEngine.detectFairValueGaps(candles) }
    val obs = remember(candles, atrs) { PriceActionEngine.detectOrderBlocks(candles, atrs) }

    var showConfluenceMatrix by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        // Instrument Selector Quick Bar
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(instruments) { inst ->
                    val isSelected = inst.symbol == selectedInstrument.symbol
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectInstrument(inst) },
                        label = {
                            Text(
                                text = inst.symbol,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = SurfaceDark,
                            containerColor = SurfaceDark,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Active Instrument Header & Quote
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedInstrument.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[${selectedInstrument.category.displayName}]",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                    Text(
                        text = "Bid: ${selectedInstrument.bidPrice}  |  Ask: ${selectedInstrument.askPrice}  |  Spread: ${selectedInstrument.formattedSpread}",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
                DataFreshnessBadge(isRealTime = true, latencyMs = 32)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Timeframe Selector Buttons (All 8 Timeframes)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Timeframe.values().forEach { tf ->
                    val isSelected = tf == selectedTimeframe
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CyanAccent else Color.Transparent)
                            .padding(horizontal = 7.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf.code,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) SurfaceDark else TextSecondary,
                            modifier = Modifier.clickable { viewModel.selectTimeframe(tf) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Interactive Candlestick Chart
        item {
            CandlestickChart(
                candles = candles,
                signal = currentSignal,
                swings = swings,
                breaks = breaks,
                fvgs = fvgs,
                orderBlocks = obs
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Chart Overlays Legend & Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendIndicator(color = CyanAccent, label = "BOS/Entry")
                    Spacer(modifier = Modifier.width(8.dp))
                    LegendIndicator(color = WarningAmber, label = "CHoCH")
                    Spacer(modifier = Modifier.width(8.dp))
                    LegendIndicator(color = BullishGreen, label = "FVG/TP")
                    Spacer(modifier = Modifier.width(8.dp))
                    LegendIndicator(color = BearishRed, label = "SL")
                }

                OutlinedButton(
                    onClick = { showConfluenceMatrix = !showConfluenceMatrix },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (showConfluenceMatrix) "Hide Matrix" else "8-TF Matrix",
                        fontSize = 10.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 8-Timeframe Matrix (Collapsible)
        if (showConfluenceMatrix) {
            item {
                TimeframeConfluenceMatrix(
                    analyses = multiTfAnalyses,
                    onSelectTimeframe = { tf -> viewModel.selectTimeframe(tf) }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // Active Setup Summary & Direct Execution / Risk Actions
        if (currentSignal != null) {
            item {
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
                            Text(
                                text = "ACTIVE ENGINE EVALUATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                            Text(
                                text = "Score: ${currentSignal.scoreBreakdown.totalScore}/100",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentSignal.scoreBreakdown.totalScore >= 85) BullishGreen else WarningAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${currentSignal.setupType.title} • State: ${currentSignal.state.label}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.selectSignal(currentSignal) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Inspect Audit", fontSize = 11.sp, color = TextPrimary)
                            }

                            Button(
                                onClick = {
                                    viewModel.selectSignal(currentSignal)
                                    viewModel.navigateTo(AppScreen.RISK_CALC)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Calculate Risk", fontSize = 11.sp, color = SurfaceDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendIndicator(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = label, fontSize = 9.sp, color = TextMuted)
    }
}
