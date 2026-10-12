package com.example.model

enum class Timeframe(
    val code: String,
    val minutes: Int,
    val roleDescription: String,
    val hierarchyWeight: Int
) {
    MONTHLY("1M", 43200, "Broad Macro Context & Long-Term Structure", 25),
    WEEKLY("1W", 10080, "Swing Structure & Major Liquidity Pools", 20),
    DAILY("1D", 1440, "Dealing Range, Daily Bias & Key S/R", 20),
    H4("4H", 240, "Actionable Swings, Displacement & Zones", 15),
    H1("1H", 60, "Structure Refinement & Reaction Areas", 10),
    M30("30m", 30, "Intraday Structure & Intermediate Confluence", 8),
    M15("15m", 15, "Entry Setup & Order Flow / FVG Validation", 6),
    M5("5m", 5, "Execution Trigger & Precise SL Invalidation", 4)
}

data class Candle(
    val time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
) {
    val isBullish: Boolean get() = close >= open
    val bodySize: Double get() = Math.abs(close - open)
    val upperWick: Double get() = high - Math.max(open, close)
    val lowerWick: Double get() = Math.min(open, close) - low
    val totalRange: Double get() = high - low
}
