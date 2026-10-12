package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DirectionalBias
import com.example.model.TimeframeAnalysis
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
fun TimeframeConfluenceMatrix(
    analyses: List<TimeframeAnalysis>,
    onSelectTimeframe: (com.example.model.Timeframe) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedTimeframe by remember { mutableStateOf<TimeframeAnalysis?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "8-TIMEFRAME CONFLUENCE MATRIX",
                    fontSize = 12.sp,
                    color = CyanAccent,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Top-Down Hierarchy",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Timeframe Grid / Rows
            analyses.forEach { item ->
                val isSelected = expandedTimeframe?.timeframe == item.timeframe
                val biasColor = when (item.bias) {
                    DirectionalBias.BULLISH -> BullishGreen
                    DirectionalBias.BEARISH -> BearishRed
                    DirectionalBias.NEUTRAL -> WarningAmber
                    DirectionalBias.CONFLICTED -> WarningAmber
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) SurfaceElevated else SurfaceVariantDark)
                        .clickable {
                            expandedTimeframe = if (isSelected) null else item
                            onSelectTimeframe(item.timeframe)
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(22.dp)
                                .background(BorderDark, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.timeframe.code,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = item.timeframe.roleDescription,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(biasColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.bias.name,
                                fontSize = 9.sp,
                                color = biasColor
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            imageVector = if (item.agreesWithTrade) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (item.agreesWithTrade) BullishGreen else BearishRed,
                            modifier = Modifier.height(14.dp).width(14.dp)
                        )
                    }
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .background(BorderDark.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "Evidence & Order Flow:",
                                fontSize = 10.sp,
                                color = CyanAccent
                            )
                            Text(
                                text = item.evidenceExplanation,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Swing High: ${String.format("%.4f", item.recentSwingHigh)}  |  Swing Low: ${String.format("%.4f", item.recentSwingLow)}  |  Regime: ${item.regime.name}",
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
