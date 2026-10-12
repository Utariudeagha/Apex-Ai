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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.model.ApexSignal
import com.example.model.AssetCategory
import com.example.model.SignalDirection
import com.example.model.SignalState
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
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
fun ScannerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val signals by viewModel.signals.collectAsState()
    val instruments by viewModel.instruments.collectAsState()

    var selectedCategory by remember { mutableStateOf<AssetCategory?>(null) }
    var filterQualifiedOnly by remember { mutableStateOf(false) } // >= 85 score
    var selectedDirection by remember { mutableStateOf<SignalDirection?>(null) }

    // Filter signals
    val filteredSignals = signals.filter { sig ->
        val inst = instruments.find { it.symbol == sig.symbol }
        val categoryMatch = selectedCategory == null || inst?.category == selectedCategory
        val scoreMatch = !filterQualifiedOnly || sig.scoreBreakdown.totalScore >= 85
        val directionMatch = selectedDirection == null || sig.direction == selectedDirection
        categoryMatch && scoreMatch && directionMatch
    }.sortedByDescending { it.scoreBreakdown.totalScore }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Scanner Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MULTI-ASSET MARKET SCANNER",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Scanning 25 institutional instruments across 8 timeframes",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
            IconButton(onClick = { viewModel.generateSignals() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rescan",
                    tint = CyanAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Asset Class Filters
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("ALL (${signals.size})", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent,
                        selectedLabelColor = SurfaceDark,
                        containerColor = SurfaceDark,
                        labelColor = TextSecondary
                    )
                )
            }
            items(AssetCategory.values()) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                    label = { Text(cat.displayName, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanAccent,
                        selectedLabelColor = SurfaceDark,
                        containerColor = SurfaceDark,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Secondary Filters: Score & Direction
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterQualifiedOnly,
                onClick = { filterQualifiedOnly = !filterQualifiedOnly },
                label = { Text("Score >= 85 Only", fontSize = 10.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BullishGreen,
                    selectedLabelColor = SurfaceDark,
                    containerColor = SurfaceDark,
                    labelColor = TextSecondary
                )
            )

            FilterChip(
                selected = selectedDirection == SignalDirection.LONG,
                onClick = {
                    selectedDirection = if (selectedDirection == SignalDirection.LONG) null else SignalDirection.LONG
                },
                label = { Text("Longs Only", fontSize = 10.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BullishGreen,
                    selectedLabelColor = SurfaceDark,
                    containerColor = SurfaceDark,
                    labelColor = TextSecondary
                )
            )

            FilterChip(
                selected = selectedDirection == SignalDirection.SHORT,
                onClick = {
                    selectedDirection = if (selectedDirection == SignalDirection.SHORT) null else SignalDirection.SHORT
                },
                label = { Text("Shorts Only", fontSize = 10.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BearishRed,
                    selectedLabelColor = SurfaceDark,
                    containerColor = SurfaceDark,
                    labelColor = TextSecondary
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scanner Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(SurfaceDark)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("INSTRUMENT / PRICE", fontSize = 9.sp, color = TextMuted, modifier = Modifier.weight(1.3f))
            Text("STATUS", fontSize = 9.sp, color = TextMuted, modifier = Modifier.weight(1.0f))
            Text("SCORE", fontSize = 9.sp, color = TextMuted, modifier = Modifier.weight(0.7f))
            Text("NET R", fontSize = 9.sp, color = TextMuted, modifier = Modifier.weight(0.7f))
            Text("SPREAD", fontSize = 9.sp, color = TextMuted, modifier = Modifier.weight(0.7f))
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Scanner Results List
        if (filteredSignals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "NO QUALIFYING SETUP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The engine rejects setups that fail selectivity filters. No forced trades.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredSignals) { sig ->
                    val inst = instruments.find { it.symbol == sig.symbol }
                    ScannerRowItem(
                        signal = sig,
                        instrument = inst,
                        onClick = { viewModel.selectSignal(sig) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScannerRowItem(
    signal: ApexSignal,
    instrument: com.example.model.Instrument?,
    onClick: () -> Unit
) {
    val isLong = signal.direction == SignalDirection.LONG
    val stateColor = when (signal.state) {
        SignalState.CONFIRMED, SignalState.ACTIVE -> BullishGreen
        SignalState.WAITING_FOR_CONFIRMATION, SignalState.POTENTIAL -> WarningAmber
        else -> BearishRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Column 1: Symbol & Price
            Column(modifier = Modifier.weight(1.3f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = signal.symbol,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isLong) "▲" else "▼",
                        fontSize = 10.sp,
                        color = if (isLong) BullishGreen else BearishRed
                    )
                }
                Text(
                    text = instrument?.formattedPrice ?: String.format("%.4f", signal.currentPrice),
                    fontSize = 10.sp,
                    color = CyanAccent
                )
            }

            // Column 2: Status
            Column(modifier = Modifier.weight(1.0f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(stateColor.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = signal.state.label.take(9),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = stateColor,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = signal.setupType.code,
                    fontSize = 8.sp,
                    color = TextMuted
                )
            }

            // Column 3: Score
            Column(modifier = Modifier.weight(0.7f)) {
                Text(
                    text = "${signal.scoreBreakdown.totalScore}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (signal.scoreBreakdown.totalScore >= 85) BullishGreen else WarningAmber
                )
                Text(
                    text = "/100",
                    fontSize = 8.sp,
                    color = TextMuted
                )
            }

            // Column 4: Net R
            Column(modifier = Modifier.weight(0.7f)) {
                Text(
                    text = "${String.format("%.1f", signal.netRewardToRisk)}R",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BullishGreen
                )
                Text(
                    text = "net",
                    fontSize = 8.sp,
                    color = TextMuted
                )
            }

            // Column 5: Spread
            Column(modifier = Modifier.weight(0.7f)) {
                Text(
                    text = "${instrument?.spreadPips ?: 1.2}p",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Text(
                    text = "live",
                    fontSize = 8.sp,
                    color = TextMuted
                )
            }
        }
    }
}
