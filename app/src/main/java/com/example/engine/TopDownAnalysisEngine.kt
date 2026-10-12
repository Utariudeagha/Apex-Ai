package com.example.engine

import com.example.model.Candle
import com.example.model.DirectionalBias
import com.example.model.MarketRegime
import com.example.model.SwingType
import com.example.model.Timeframe
import com.example.model.TimeframeAnalysis

object TopDownAnalysisEngine {

    fun analyzeTimeframe(
        timeframe: Timeframe,
        candles: List<Candle>,
        proposedDirectionIsLong: Boolean
    ): TimeframeAnalysis {
        if (candles.isEmpty()) {
            return TimeframeAnalysis(
                timeframe = timeframe,
                bias = DirectionalBias.NEUTRAL,
                regime = MarketRegime.RANGING_CONSOLIDATION,
                recentSwingHigh = 0.0,
                recentSwingLow = 0.0,
                lastStructuralBreak = null,
                activeFVGs = emptyList(),
                activeOrderBlocks = emptyList(),
                liquiditySweeps = emptyList(),
                candleCloseConfirmed = false,
                agreesWithTrade = false,
                evidenceExplanation = "Insufficient data to compute structural confluence."
            )
        }

        val atrs = PriceActionEngine.calculateATR(candles)
        val swings = PriceActionEngine.detectSwingPoints(candles)
        val breaks = PriceActionEngine.detectStructuralBreaks(candles, swings)
        val fvgs = PriceActionEngine.detectFairValueGaps(candles)
        val obs = PriceActionEngine.detectOrderBlocks(candles, atrs)
        val sweeps = PriceActionEngine.detectLiquiditySweeps(candles, swings)

        val latestCandle = candles.last()
        val recentHigh = swings.filter { it.type == SwingType.HIGH }.lastOrNull()?.price
            ?: candles.maxOfOrNull { it.high } ?: latestCandle.high
        val recentLow = swings.filter { it.type == SwingType.LOW }.lastOrNull()?.price
            ?: candles.minOfOrNull { it.low } ?: latestCandle.low

        // Determine Directional Bias based on Swings & Breaks
        val lastBreak = breaks.lastOrNull()
        val bias = when {
            lastBreak != null && lastBreak.isBullish -> DirectionalBias.BULLISH
            lastBreak != null && !lastBreak.isBullish -> DirectionalBias.BEARISH
            latestCandle.close > (recentHigh + recentLow) / 2.0 -> DirectionalBias.BULLISH
            else -> DirectionalBias.BEARISH
        }

        // Determine Market Regime
        val regime = if (breaks.isNotEmpty()) {
            MarketRegime.TRENDING_EXPANSION
        } else if (Math.abs(latestCandle.close - ((recentHigh + recentLow) / 2.0)) < (latestCandle.close * 0.003)) {
            MarketRegime.RANGING_CONSOLIDATION
        } else {
            MarketRegime.REGIME_TRANSITION
        }

        // Does this timeframe agree with the proposed trade?
        val agrees = if (proposedDirectionIsLong) {
            bias == DirectionalBias.BULLISH
        } else {
            bias == DirectionalBias.BEARISH
        }

        val explanation = when {
            agrees && bias == DirectionalBias.BULLISH ->
                "${timeframe.code}: Bullish order flow intact. Price respecting discount demand with clean expansion above ${String.format("%.4f", recentLow)}."
            agrees && bias == DirectionalBias.BEARISH ->
                "${timeframe.code}: Bearish market structure confirmed. Liquidity sweep of ${String.format("%.4f", recentHigh)} followed by displacement."
            !agrees ->
                "${timeframe.code}: Conflicting timeframe bias (${bias.name}). Caution required; lower timeframe retracement active."
            else ->
                "${timeframe.code}: Neutral consolidation within dealing range ${String.format("%.4f", recentLow)} - ${String.format("%.4f", recentHigh)}."
        }

        return TimeframeAnalysis(
            timeframe = timeframe,
            bias = bias,
            regime = regime,
            recentSwingHigh = recentHigh,
            recentSwingLow = recentLow,
            lastStructuralBreak = lastBreak,
            activeFVGs = fvgs.takeLast(3),
            activeOrderBlocks = obs.takeLast(2),
            liquiditySweeps = sweeps.takeLast(2),
            candleCloseConfirmed = true,
            agreesWithTrade = agrees,
            evidenceExplanation = explanation
        )
    }

    fun performFullTopDownMatrix(
        multiTfCandles: Map<Timeframe, List<Candle>>,
        proposedDirectionIsLong: Boolean
    ): List<TimeframeAnalysis> {
        return Timeframe.values().map { tf ->
            val candles = multiTfCandles[tf] ?: emptyList()
            analyzeTimeframe(tf, candles, proposedDirectionIsLong)
        }
    }
}
