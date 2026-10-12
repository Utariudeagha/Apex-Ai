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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SignalExplanation
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun ExplanationPanel(
    explanation: SignalExplanation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "12-POINT INSTITUTIONAL SETUP AUDIT",
                fontSize = 12.sp,
                color = CyanAccent,
                letterSpacing = 1.sp
            )
            Text(
                text = "Deterministic Explainability Engine",
                fontSize = 10.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExplanationItem("A", "Higher-Timeframe Narrative", explanation.htfNarrative)
            ExplanationItem("B", "Current Market Regime", explanation.currentMarketRegime)
            ExplanationItem("C", "Fundamental Drivers", explanation.fundamentalDrivers)
            ExplanationItem("D", "Important Scheduled Events", explanation.scheduledEvents)
            ExplanationItem("E", "Liquidity Map", explanation.liquidityMap)
            ExplanationItem("F", "Structural Levels", explanation.structuralLevels)
            ExplanationItem("G", "Potential Bullish Scenario", explanation.bullishScenario)
            ExplanationItem("H", "Potential Bearish Scenario", explanation.bearishScenario)
            ExplanationItem("I", "Entry Trigger", explanation.entryTrigger)
            ExplanationItem("J", "Invalidation Condition", explanation.invalidationCondition)
            ExplanationItem("K", "Risk Assessment", explanation.riskAssessment)
            ExplanationItem("L", "Engine Verdict & State Rationale", explanation.engineVerdict, isHighlight = true)
        }
    }
}

@Composable
private fun ExplanationItem(
    letter: String,
    title: String,
    content: String,
    isHighlight: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isHighlight) SurfaceElevated else BorderDark.copy(alpha = 0.35f))
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isHighlight) CyanAccent else BorderDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    fontSize = 10.sp,
                    color = if (isHighlight) SurfaceDark else TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = if (isHighlight) CyanAccent else TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            fontSize = 11.sp,
            color = TextPrimary,
            lineHeight = 15.sp
        )
    }
}
