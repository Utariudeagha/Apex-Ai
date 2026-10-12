package com.example.engine

import com.example.model.Candle
import com.example.model.DealingRange
import com.example.model.FairValueGap
import com.example.model.LiquiditySweep
import com.example.model.OrderBlock
import com.example.model.StructuralBreak
import com.example.model.SwingPoint
import com.example.model.SwingType
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object PriceActionEngine {

    /**
     * Calculates True Range and Average True Range (ATR) over given period
     */
    fun calculateATR(candles: List<Candle>, period: Int = 14): List<Double> {
        if (candles.isEmpty()) return emptyList()
        val trList = mutableListOf<Double>()
        for (i in candles.indices) {
            val c = candles[i]
            if (i == 0) {
                trList.add(c.high - c.low)
            } else {
                val prevClose = candles[i - 1].close
                val tr = max(c.high - c.low, max(abs(c.high - prevClose), abs(c.low - prevClose)))
                trList.add(tr)
            }
        }
        val atr = mutableListOf<Double>()
        var runningSum = 0.0
        for (i in trList.indices) {
            runningSum += trList[i]
            if (i >= period) {
                runningSum -= trList[i - period]
                atr.add(runningSum / period)
            } else {
                atr.add(runningSum / (i + 1))
            }
        }
        return atr
    }

    /**
     * Identifies confirmed Swing Highs and Swing Lows with lookback/lookahead confirmation
     */
    fun detectSwingPoints(candles: List<Candle>, window: Int = 3): List<SwingPoint> {
        val points = mutableListOf<SwingPoint>()
        if (candles.size < window * 2 + 1) return points

        for (i in window until (candles.size - window)) {
            val current = candles[i]
            var isHigh = true
            var isLow = true

            for (j in 1..window) {
                if (candles[i - j].high >= current.high || candles[i + j].high > current.high) {
                    isHigh = false
                }
                if (candles[i - j].low <= current.low || candles[i + j].low < current.low) {
                    isLow = false
                }
            }

            if (isHigh) {
                points.add(SwingPoint(i, current.time, current.high, SwingType.HIGH, true, "SH"))
            } else if (isLow) {
                points.add(SwingPoint(i, current.time, current.low, SwingType.LOW, true, "SL"))
            }
        }
        return points
    }

    /**
     * Identifies Break of Structure (BOS) and Change of Character (CHoCH)
     */
    fun detectStructuralBreaks(candles: List<Candle>, swings: List<SwingPoint>): List<StructuralBreak> {
        val breaks = mutableListOf<StructuralBreak>()
        if (swings.isEmpty() || candles.isEmpty()) return breaks

        var lastBullishTrend = true

        for (i in 1 until swings.size) {
            val currentSwing = swings[i]
            val prevSwing = swings[i - 1]

            // Look for subsequent candles closing beyond the swing level
            for (cIndex in (currentSwing.index + 1) until candles.size) {
                val candle = candles[cIndex]

                if (currentSwing.type == SwingType.HIGH && candle.close > currentSwing.price) {
                    val isChoch = !lastBullishTrend
                    lastBullishTrend = true
                    breaks.add(
                        StructuralBreak(
                            index = cIndex,
                            time = candle.time,
                            brokenLevel = currentSwing.price,
                            isBullish = true,
                            isChoch = isChoch,
                            description = if (isChoch) "Bullish CHoCH: Shift from lower highs" else "Bullish BOS: Trend continuation"
                        )
                    )
                    break // break inner loop for this swing once broken
                } else if (currentSwing.type == SwingType.LOW && candle.close < currentSwing.price) {
                    val isChoch = lastBullishTrend
                    lastBullishTrend = false
                    breaks.add(
                        StructuralBreak(
                            index = cIndex,
                            time = candle.time,
                            brokenLevel = currentSwing.price,
                            isBullish = false,
                            isChoch = isChoch,
                            description = if (isChoch) "Bearish CHoCH: Shift from higher lows" else "Bearish BOS: Trend continuation"
                        )
                    )
                    break
                }
            }
        }
        return breaks
    }

    /**
     * Identifies Fair Value Gaps (FVG) across 3-candle sequences
     */
    fun detectFairValueGaps(candles: List<Candle>): List<FairValueGap> {
        val fvgs = mutableListOf<FairValueGap>()
        if (candles.size < 3) return fvgs

        for (i in 0 until (candles.size - 2)) {
            val c1 = candles[i]
            val c2 = candles[i + 1]
            val c3 = candles[i + 2]

            // Bullish FVG: c1.high < c3.low and c2 is strong displacement up
            if (c3.low > c1.high && c2.isBullish) {
                val gapSize = c3.low - c1.high
                if (gapSize > 0) {
                    // Check if subsequent candles have mitigated it
                    var isFilled = false
                    var isPartial = false
                    val latestPrice = candles.last().close
                    if (latestPrice <= c1.high) isFilled = true
                    else if (latestPrice <= (c3.low + c1.high) / 2.0) isPartial = true

                    fvgs.add(
                        FairValueGap(
                            id = "FVG-BULL-${c2.time}",
                            startIndex = i + 1,
                            upperBoundary = c3.low,
                            lowerBoundary = c1.high,
                            isBullish = true,
                            isPartiallyFilled = isPartial,
                            isMitigated = isFilled,
                            creationTime = c2.time
                        )
                    )
                }
            }
            // Bearish FVG: c3.high < c1.low and c2 is strong displacement down
            else if (c3.high < c1.low && !c2.isBullish) {
                val gapSize = c1.low - c3.high
                if (gapSize > 0) {
                    var isFilled = false
                    var isPartial = false
                    val latestPrice = candles.last().close
                    if (latestPrice >= c1.low) isFilled = true
                    else if (latestPrice >= (c1.low + c3.high) / 2.0) isPartial = true

                    fvgs.add(
                        FairValueGap(
                            id = "FVG-BEAR-${c2.time}",
                            startIndex = i + 1,
                            upperBoundary = c1.low,
                            lowerBoundary = c3.high,
                            isBullish = false,
                            isPartiallyFilled = isPartial,
                            isMitigated = isFilled,
                            creationTime = c2.time
                        )
                    )
                }
            }
        }
        return fvgs
    }

    /**
     * Identifies Order Blocks: The origin opposing candle before rapid displacement that caused a structural break
     */
    fun detectOrderBlocks(candles: List<Candle>, atrs: List<Double>): List<OrderBlock> {
        val blocks = mutableListOf<OrderBlock>()
        if (candles.size < 4) return blocks

        for (i in 0 until (candles.size - 3)) {
            val c0 = candles[i]
            val c1 = candles[i + 1]
            val atr = if (i < atrs.size) atrs[i] else 1.0

            // Bullish OB: Bearish candle followed by explosive displacement up (> 1.8 ATR)
            if (!c0.isBullish && (c1.close - c1.open) > (atr * 1.5)) {
                val latest = candles.last().close
                val isMitigated = latest < c0.low
                blocks.add(
                    OrderBlock(
                        id = "OB-BULL-${c0.time}",
                        startIndex = i,
                        high = c0.high,
                        low = c0.low,
                        isBullish = true,
                        isMitigated = isMitigated,
                        displacementATRRatio = (c1.close - c1.open) / max(atr, 0.0001)
                    )
                )
            }
            // Bearish OB: Bullish candle followed by explosive displacement down (> 1.8 ATR)
            else if (c0.isBullish && (c1.open - c1.close) > (atr * 1.5)) {
                val latest = candles.last().close
                val isMitigated = latest > c0.high
                blocks.add(
                    OrderBlock(
                        id = "OB-BEAR-${c0.time}",
                        startIndex = i,
                        high = c0.high,
                        low = c0.low,
                        isBullish = false,
                        isMitigated = isMitigated,
                        displacementATRRatio = (c1.open - c1.close) / max(atr, 0.0001)
                    )
                )
            }
        }
        return blocks
    }

    /**
     * Detects Liquidity Sweeps: Penetration of a swing extreme with a close back inside
     */
    fun detectLiquiditySweeps(candles: List<Candle>, swings: List<SwingPoint>): List<LiquiditySweep> {
        val sweeps = mutableListOf<LiquiditySweep>()
        if (swings.isEmpty() || candles.isEmpty()) return sweeps

        for (swing in swings) {
            for (cIndex in (swing.index + 1) until candles.size) {
                val c = candles[cIndex]
                if (swing.type == SwingType.HIGH) {
                    // Penetrated high but closed back inside
                    if (c.high > swing.price && c.close < swing.price) {
                        sweeps.add(
                            LiquiditySweep(
                                priceLevel = swing.price,
                                sweepExtremePrice = c.high,
                                isSweepOfHigh = true,
                                rejectionConfirmed = true,
                                sweptLabel = "Swing High Liquidity Absorbed at ${String.format("%.4f", swing.price)}"
                            )
                        )
                    }
                } else if (swing.type == SwingType.LOW) {
                    // Penetrated low but closed back inside
                    if (c.low < swing.price && c.close > swing.price) {
                        sweeps.add(
                            LiquiditySweep(
                                priceLevel = swing.price,
                                sweepExtremePrice = c.low,
                                isSweepOfHigh = false,
                                rejectionConfirmed = true,
                                sweptLabel = "Swing Low Liquidity Absorbed at ${String.format("%.4f", swing.price)}"
                            )
                        )
                    }
                }
            }
        }
        return sweeps
    }

    /**
     * Computes the Dealing Range and Premium/Discount Zones
     */
    fun calculateDealingRange(swings: List<SwingPoint>): DealingRange {
        if (swings.isEmpty()) return DealingRange(1.0, 0.0)
        val highs = swings.filter { it.type == SwingType.HIGH }.map { it.price }
        val lows = swings.filter { it.type == SwingType.LOW }.map { it.price }
        val maxHigh = highs.maxOrNull() ?: 1.0
        val minLow = lows.minOrNull() ?: 0.0
        return DealingRange(maxHigh, minLow)
    }
}
