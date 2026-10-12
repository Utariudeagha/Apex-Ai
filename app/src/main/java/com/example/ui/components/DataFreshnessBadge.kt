package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.WarningAmber

@Composable
fun DataFreshnessBadge(
    isRealTime: Boolean = true,
    latencyMs: Int = 42,
    isStale: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariantDark)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isStale) WarningAmber else BullishGreen)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = if (isStale) "DATA STALE (>30s)" else if (isRealTime) "L2 LIVE (${latencyMs}ms)" else "DEMO FEED",
            fontSize = 9.sp,
            color = if (isStale) WarningAmber else CyanAccent
        )
    }
}
