package com.example.model

enum class SwingType { HIGH, LOW }

data class SwingPoint(
    val index: Int,
    val time: Long,
    val price: Double,
    val type: SwingType,
    val isConfirmed: Boolean = true,
    val label: String = if (type == SwingType.HIGH) "SH" else "SL"
)

data class StructuralBreak(
    val index: Int,
    val time: Long,
    val brokenLevel: Double,
    val isBullish: Boolean,
    val isChoch: Boolean, // True = Change of Character, False = Break of Structure (BOS)
    val description: String
)

data class FairValueGap(
    val id: String,
    val startIndex: Int,
    val upperBoundary: Double,
    val lowerBoundary: Double,
    val isBullish: Boolean,
    val isPartiallyFilled: Boolean = false,
    val isMitigated: Boolean = false,
    val creationTime: Long
) {
    val midpoint: Double get() = (upperBoundary + lowerBoundary) / 2.0
}

data class OrderBlock(
    val id: String,
    val startIndex: Int,
    val high: Double,
    val low: Double,
    val isBullish: Boolean,
    val isMitigated: Boolean = false,
    val displacementATRRatio: Double = 2.4
)

data class LiquiditySweep(
    val priceLevel: Double,
    val sweepExtremePrice: Double,
    val isSweepOfHigh: Boolean,
    val rejectionConfirmed: Boolean,
    val sweptLabel: String // e.g. "Previous Daily High Swept", "Equal Highs Swept"
)

data class DealingRange(
    val rangeHigh: Double,
    val rangeLow: Double
) {
    val equilibrium: Double get() = (rangeHigh + rangeLow) / 2.0
    val premiumZoneBottom: Double get() = equilibrium
    val discountZoneTop: Double get() = equilibrium

    fun isPriceInDiscount(price: Double): Boolean = price < equilibrium
    fun isPriceInPremium(price: Double): Boolean = price > equilibrium
}

enum class DirectionalBias { BULLISH, BEARISH, NEUTRAL, CONFLICTED }
enum class MarketRegime { TRENDING_EXPANSION, RANGING_CONSOLIDATION, REGIME_TRANSITION }

data class TimeframeAnalysis(
    val timeframe: Timeframe,
    val bias: DirectionalBias,
    val regime: MarketRegime,
    val recentSwingHigh: Double,
    val recentSwingLow: Double,
    val lastStructuralBreak: StructuralBreak?,
    val activeFVGs: List<FairValueGap>,
    val activeOrderBlocks: List<OrderBlock>,
    val liquiditySweeps: List<LiquiditySweep>,
    val candleCloseConfirmed: Boolean,
    val agreesWithTrade: Boolean,
    val evidenceExplanation: String
)
