package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApexSignal
import com.example.model.Candle
import com.example.model.FairValueGap
import com.example.model.OrderBlock
import com.example.model.StructuralBreak
import com.example.model.SwingPoint
import com.example.ui.theme.BearishRed
import com.example.ui.theme.BullishGreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.FvgBearishFill
import com.example.ui.theme.FvgBullishFill
import com.example.ui.theme.OrderBlockFill
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    signal: ApexSignal? = null,
    swings: List<SwingPoint> = emptyList(),
    breaks: List<StructuralBreak> = emptyList(),
    fvgs: List<FairValueGap> = emptyList(),
    orderBlocks: List<OrderBlock> = emptyList(),
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(SurfaceDark, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No candle data available for rendering", color = TextMuted)
        }
        return
    }

    var crosshairX by remember { mutableFloatStateOf(-1f) }
    var crosshairY by remember { mutableFloatStateOf(-1f) }
    var selectedCandle by remember { mutableStateOf<Candle?>(null) }
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        // Crosshair Readout Bar
        if (selectedCandle != null) {
            val c = selectedCandle!!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "O: ${formatPrice(c.open)}  H: ${formatPrice(c.high)}  L: ${formatPrice(c.low)}  C: ${formatPrice(c.close)}",
                    fontSize = 11.sp,
                    color = if (c.isBullish) BullishGreen else BearishRed
                )
                Text(
                    text = "Vol: ${c.volume.toInt()}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val latest = candles.last()
                Text(
                    text = "LIVE: ${formatPrice(latest.close)} (${if (latest.isBullish) "+" else "-"}0.42%)",
                    fontSize = 11.sp,
                    color = if (latest.isBullish) BullishGreen else BearishRed
                )
                Text(
                    text = "Tap & drag chart for institutional crosshair",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            crosshairX = offset.x
                            crosshairY = offset.y
                        },
                        onTap = {
                            crosshairX = -1f
                            crosshairY = -1f
                            selectedCandle = null
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            // keep last or clear
                        }
                    ) { change, _ ->
                        crosshairX = change.position.x
                        crosshairY = change.position.y
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val priceAxisWidth = 55.dp.toPx()
                val chartAreaWidth = canvasWidth - priceAxisWidth
                val volumeAreaHeight = canvasHeight * 0.18f
                val candleAreaHeight = canvasHeight - volumeAreaHeight

                val visibleCount = min(candles.size, 45)
                val displayCandles = candles.takeLast(visibleCount)

                val minPrice = displayCandles.minOf { it.low }
                val maxPrice = displayCandles.maxOf { it.high }
                val priceRange = max(maxPrice - minPrice, 0.0001)

                val candleSlotWidth = chartAreaWidth / visibleCount
                val candleBodyWidth = max(candleSlotWidth * 0.65f, 2f)

                fun priceToY(price: Double): Float {
                    val normalized = (maxPrice - price) / priceRange
                    return (normalized * candleAreaHeight).toFloat()
                }

                fun yToPrice(y: Float): Double {
                    val normalized = y / candleAreaHeight
                    return maxPrice - (normalized * priceRange)
                }

                // 1. Grid Lines
                val gridLinesCount = 4
                for (g in 0..gridLinesCount) {
                    val gridY = (candleAreaHeight / gridLinesCount) * g
                    val gridPrice = maxPrice - (priceRange / gridLinesCount) * g

                    drawLine(
                        color = Color(0x1FFFFFFF),
                        start = Offset(0f, gridY),
                        end = Offset(chartAreaWidth, gridY),
                        strokeWidth = 1f
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = formatPrice(gridPrice),
                        topLeft = Offset(chartAreaWidth + 4.dp.toPx(), gridY - 6.sp.toPx()),
                        style = TextStyle(color = TextMuted, fontSize = 9.sp)
                    )
                }

                // 2. Fair Value Gaps Overlay
                for (fvg in fvgs) {
                    val topY = priceToY(fvg.upperBoundary)
                    val botY = priceToY(fvg.lowerBoundary)
                    val boxColor = if (fvg.isBullish) FvgBullishFill else FvgBearishFill
                    drawRect(
                        color = boxColor,
                        topLeft = Offset(0f, min(topY, botY)),
                        size = Size(chartAreaWidth, abs(botY - topY))
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = if (fvg.isBullish) "FVG Bullish" else "FVG Bearish",
                        topLeft = Offset(8.dp.toPx(), min(topY, botY) + 2f),
                        style = TextStyle(color = if (fvg.isBullish) CyanAccent else BearishRed, fontSize = 8.sp)
                    )
                }

                // 3. Order Blocks Overlay
                for (ob in orderBlocks) {
                    val topY = priceToY(ob.high)
                    val botY = priceToY(ob.low)
                    drawRect(
                        color = OrderBlockFill,
                        topLeft = Offset(0f, min(topY, botY)),
                        size = Size(chartAreaWidth, abs(botY - topY))
                    )
                    drawLine(
                        color = CyanAccent,
                        start = Offset(0f, min(topY, botY)),
                        end = Offset(chartAreaWidth, min(topY, botY)),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                    )
                }

                // 4. Candlesticks and Volume
                val maxVolume = displayCandles.maxOfOrNull { it.volume } ?: 1.0

                for (i in displayCandles.indices) {
                    val c = displayCandles[i]
                    val xCenter = (i * candleSlotWidth) + (candleSlotWidth / 2f)
                    val color = if (c.isBullish) BullishGreen else BearishRed

                    // Wick
                    val highY = priceToY(c.high)
                    val lowY = priceToY(c.low)
                    drawLine(
                        color = color,
                        start = Offset(xCenter, highY),
                        end = Offset(xCenter, lowY),
                        strokeWidth = 1.5f
                    )

                    // Body
                    val openY = priceToY(c.open)
                    val closeY = priceToY(c.close)
                    val bodyTop = min(openY, closeY)
                    val bodyHeight = max(abs(openY - closeY), 2f)

                    drawRect(
                        color = color,
                        topLeft = Offset(xCenter - (candleBodyWidth / 2f), bodyTop),
                        size = Size(candleBodyWidth, bodyHeight)
                    )

                    // Volume Bar
                    val volHeight = (c.volume / maxVolume) * volumeAreaHeight
                    val volY = canvasHeight - volHeight.toFloat()
                    drawRect(
                        color = color.copy(alpha = 0.35f),
                        topLeft = Offset(xCenter - (candleBodyWidth / 2f), volY),
                        size = Size(candleBodyWidth, volHeight.toFloat())
                    )
                }

                // 5. Structural Breaks (BOS / CHoCH)
                for (b in breaks.takeLast(2)) {
                    val y = priceToY(b.brokenLevel)
                    drawLine(
                        color = if (b.isChoch) WarningAmber else CyanAccent,
                        start = Offset(chartAreaWidth * 0.3f, y),
                        end = Offset(chartAreaWidth, y),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = if (b.isChoch) "CHoCH" else "BOS",
                        topLeft = Offset(chartAreaWidth - 45.dp.toPx(), y - 12.sp.toPx()),
                        style = TextStyle(
                            color = if (b.isChoch) WarningAmber else CyanAccent,
                            fontSize = 9.sp
                        )
                    )
                }

                // 6. Signal Execution Lines Overlay (Entry, Stop Loss, Take Profit)
                if (signal != null && signal.isExecutable) {
                    // Entry Line
                    val entryY = priceToY(signal.entryPrice)
                    drawLine(
                        color = CyanAccent,
                        start = Offset(0f, entryY),
                        end = Offset(chartAreaWidth, entryY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "ENTRY ${formatPrice(signal.entryPrice)}",
                        topLeft = Offset(4.dp.toPx(), entryY - 10.sp.toPx()),
                        style = TextStyle(color = CyanAccent, fontSize = 9.sp)
                    )

                    // Stop Loss Line
                    val slY = priceToY(signal.stopLoss)
                    drawLine(
                        color = BearishRed,
                        start = Offset(0f, slY),
                        end = Offset(chartAreaWidth, slY),
                        strokeWidth = 2f
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "SL ${formatPrice(signal.stopLoss)}",
                        topLeft = Offset(4.dp.toPx(), slY - 10.sp.toPx()),
                        style = TextStyle(color = BearishRed, fontSize = 9.sp)
                    )

                    // Take Profit 1 Line
                    val tpY = priceToY(signal.takeProfit1)
                    drawLine(
                        color = BullishGreen,
                        start = Offset(0f, tpY),
                        end = Offset(chartAreaWidth, tpY),
                        strokeWidth = 2f
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "TP1 ${formatPrice(signal.takeProfit1)} (+${String.format("%.1f", signal.netRewardToRisk)}R)",
                        topLeft = Offset(4.dp.toPx(), tpY - 10.sp.toPx()),
                        style = TextStyle(color = BullishGreen, fontSize = 9.sp)
                    )
                }

                // 7. Interactive Crosshair
                if (crosshairX in 0f..chartAreaWidth && crosshairY in 0f..candleAreaHeight) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(crosshairX, 0f),
                        end = Offset(crosshairX, canvasHeight),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(0f, crosshairY),
                        end = Offset(chartAreaWidth, crosshairY),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    val crossPrice = yToPrice(crosshairY)
                    drawRect(
                        color = SurfaceDark,
                        topLeft = Offset(chartAreaWidth, crosshairY - 7.sp.toPx()),
                        size = Size(priceAxisWidth, 14.sp.toPx())
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = formatPrice(crossPrice),
                        topLeft = Offset(chartAreaWidth + 2.dp.toPx(), crosshairY - 6.sp.toPx()),
                        style = TextStyle(color = Color.White, fontSize = 9.sp)
                    )

                    val candleIdx = (crosshairX / candleSlotWidth).toInt().coerceIn(0, displayCandles.size - 1)
                    selectedCandle = displayCandles[candleIdx]
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
