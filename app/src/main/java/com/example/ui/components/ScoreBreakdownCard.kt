package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApexSignal
import com.example.model.HardFilterCheck
import com.example.model.ScoreBreakdown
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
fun ScoreBreakdownCard(
    signal: ApexSignal,
    modifier: Modifier = Modifier
) {
    val score = signal.scoreBreakdown
    val isPassing = score.isScorePassing && signal.allHardFiltersPassed

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with Total Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "100-POINT SCORING AUDIT",
                        fontSize = 12.sp,
                        color = CyanAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Qualification Threshold: 85 / 100",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPassing) BullishGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${score.totalScore} / 100",
                        fontSize = 16.sp,
                        color = if (isPassing) BullishGreen else WarningAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Score Category Progress Bars
            ScoreBarRow("Higher-Timeframe Alignment", score.htfAlignment, 25)
            ScoreBarRow("Structure & Price Action Quality", score.structureQuality, 20)
            ScoreBarRow("Liquidity Behavior & Confirmation", score.liquidityConfirmation, 20)
            ScoreBarRow("Entry Zone Quality & Trigger", score.entryZoneTrigger, 15)
            ScoreBarRow("Risk/Reward & Stop Placement", score.riskRewardStop, 10)
            ScoreBarRow("Fundamentals, Session & Volatility", score.fundamentalsSession, 10)

            Spacer(modifier = Modifier.height(16.dp))

            // Hard Filters Checklist Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MANDATORY HARD FILTERS",
                    fontSize = 11.sp,
                    color = CyanAccent,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = if (signal.allHardFiltersPassed) "ALL PASSED" else "REJECTED BY RULES",
                    fontSize = 10.sp,
                    color = if (signal.allHardFiltersPassed) BullishGreen else BearishRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Checklist Items
            signal.hardFilters.forEach { filter ->
                HardFilterItem(filter)
            }
        }
    }
}

@Composable
private fun ScoreBarRow(label: String, points: Int, maxPoints: Int) {
    val progress = (points.toFloat() / maxPoints.toFloat()).coerceIn(0f, 1f)

    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 10.sp, color = TextSecondary)
            Text(text = "$points / $maxPoints", fontSize = 10.sp, color = TextPrimary)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (progress >= 0.8f) CyanAccent else WarningAmber,
            trackColor = BorderDark
        )
    }
}

@Composable
private fun HardFilterItem(check: HardFilterCheck) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(SurfaceVariantDark, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (check.passed) BullishGreen.copy(alpha = 0.2f) else BearishRed.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (check.passed) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (check.passed) BullishGreen else BearishRed,
                modifier = Modifier.width(12.dp).height(12.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(text = check.filterName, fontSize = 10.sp, color = TextPrimary)
            Text(text = check.detail, fontSize = 9.sp, color = TextMuted)
        }
    }
}
