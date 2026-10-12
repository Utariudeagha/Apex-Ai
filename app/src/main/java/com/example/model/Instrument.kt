package com.example.model

enum class AssetCategory(val displayName: String) {
    FOREX("Forex"),
    METALS("Precious Metals"),
    INDICES("Equity Indices"),
    ENERGY("Energy Commodities"),
    CRYPTO("Cryptocurrency")
}

enum class TradingSession(val displayName: String, val statusLabel: String) {
    ASIAN_TOKYO("Tokyo / Asian", "Active - Moderate Volatility"),
    LONDON("London Session", "Active - High Liquidity"),
    NEW_YORK("New York Session", "Active - Peak Volume"),
    LONDON_NY_OVERLAP("London / NY Overlap", "Peak Cross-Liquidity"),
    OFF_HOURS("Off-Market Hours", "Low Liquidity - Spreads Elevated")
}

data class Instrument(
    val symbol: String,
    val name: String,
    val category: AssetCategory,
    val baseCurrency: String,
    val quoteCurrency: String,
    val pipSize: Double,
    val tickValue: Double,
    val contractSize: Double,
    val spreadPips: Double,
    val lastPrice: Double,
    val bidPrice: Double,
    val askPrice: Double,
    val changePercent24h: Double,
    val high24h: Double,
    val low24h: Double,
    val session: TradingSession,
    val isMarketOpen: Boolean = true,
    val isFavorite: Boolean = false,
    val dataQuality: String = "Institutional Grade - 99.98% Tick Sync",
    val isRealTime: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val formattedSpread: String
        get() = String.format("%.1f pips", spreadPips)

    val formattedPrice: String
        get() = when {
            lastPrice >= 1000.0 -> String.format("%,.2f", lastPrice)
            pipSize == 0.001 || pipSize == 0.01 -> String.format("%.3f", lastPrice)
            else -> String.format("%.5f", lastPrice)
        }
}
