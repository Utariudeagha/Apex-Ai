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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.engine.RiskCalculationResult
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
fun RiskCalculatorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val accountBalance by viewModel.accountBalance.collectAsState()
    val riskPercent by viewModel.riskPercent.collectAsState()
    val riskResult by viewModel.riskCalcResult.collectAsState()
    val selectedInstrument by viewModel.selectedInstrument.collectAsState()
    val instruments by viewModel.instruments.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Title Header
        item {
            Column {
                Text(
                    text = "INSTITUTIONAL RISK ENGINE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Exact position sizing derived from monetary risk & friction models",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Instrument Selector Bar
        item {
            Text(
                text = "SELECT ASSET FOR SIZING",
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
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Account Balance Presets
        item {
            Text(
                text = "ACCOUNT CAPITAL PRESETS",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(25000.0, 50000.0, 100000.0, 200000.0).forEach { bal ->
                    val isSelected = accountBalance == bal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CyanAccent else SurfaceDark)
                            .clickable { viewModel.updateAccountBalance(bal) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$${(bal / 1000).toInt()}k",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) SurfaceDark else TextPrimary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Risk Percentage Presets
        item {
            Text(
                text = "RISK PER TRADE (%)",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.25, 0.50, 1.00, 1.50).forEach { pct ->
                    val isSelected = riskPercent == pct
                    val isHighRisk = pct > 1.0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) (if (isHighRisk) WarningAmber else CyanAccent)
                                else SurfaceDark
                            )
                            .clickable { viewModel.updateRiskPercent(pct) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${pct}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) SurfaceDark else if (isHighRisk) WarningAmber else TextPrimary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Calculation Results Card
        if (riskResult != null) {
            val r = riskResult!!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "POSITION SIZING SPECIFICATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Big Recommended Lot Size Hero
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceElevated, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("RECOMMENDED POSITION SIZE", fontSize = 9.sp, color = TextMuted)
                                Text(
                                    text = "${r.recommendedLotSize} LOTS",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("MAX MONETARY RISK", fontSize = 9.sp, color = TextMuted)
                                Text(
                                    text = "$${String.format("%,.2f", r.monetaryRiskDollars)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BearishRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Friction Breakdown
                        MetricRow("Stop Distance", "${String.format("%.1f", r.stopDistancePips)} pips")
                        MetricRow("Estimated Round-Turn Commission", "$${String.format("%.2f", r.estimatedCommissionDollars)}")
                        MetricRow("Modeled Bid/Ask Spread Cost", "$${String.format("%.2f", r.estimatedSpreadCostDollars)}")
                        MetricRow("Total Friction Hurdle", "$${String.format("%.2f", r.totalModeledFrictionDollars)}")
                        MetricRow("Potential Net Profit (TP1)", "+$${String.format("%,.2f", r.netPotentialProfitTP1Dollars)}")
                        MetricRow("Net Estimated Reward:Risk", "${String.format("%.2f", r.netRewardToRisk)}R")
                        MetricRow("Estimated Margin Required (1:30)", "$${String.format("%,.2f", r.marginRequiredDollars)}")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Warning if Risk Exceeded
            if (r.isRiskExceededWarning && r.warningMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.width(20.dp).height(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = r.warningMessage!!,
                                fontSize = 10.sp,
                                color = WarningAmber
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
